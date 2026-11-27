package uk.gov.hmcts.reform.userprofileapi.repository;

import com.github.benmanes.caffeine.cache.Cache;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.exception.UnauthorizedException;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdamRepositoryTest {

    @Mock
    private IdamClient idamClient;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private IdamRepository idamRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetUserInfo() {
        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        Cache cache = mock(Cache.class);

        when(idamClient.getUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");

        assertNotNull(returnedUserInfo);
        verify(idamClient, times(1)).getUserInfo(any());
        verify(cacheManager, times(1)).getCache(any());
        verify(caffeineCacheMock, times(1)).getNativeCache();
        verify(cache, times(1)).estimatedSize();
    }

    @Test
    void test_getUserInfo_unAuthorizedException() {

        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        Cache cache = mock(Cache.class);

        Request request = Request.create(Request.HttpMethod.GET, "url",
                new HashMap<>(), null, new RequestTemplate());
        when(idamClient.getUserInfo(anyString()))
                .thenThrow(new FeignException
                        .Unauthorized("User is not authorized", request,null,null));


        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        doReturn(cache).when(caffeineCacheMock).getNativeCache();
        when(cache.estimatedSize()).thenReturn(anyLong());

        UnauthorizedException thrown = Assertions.assertThrows(UnauthorizedException.class, () -> {
            UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        });


        assertThat(thrown.getMessage()).contains("User is not authorized");


    }

    @Test
    void concurrentRequestsShareOneColdTokenLookup() throws Exception {
        var firstLoadStarted = new CountDownLatch(1);
        var releaseLoad = new CountDownLatch(1);
        var overlappingLoad = new CountDownLatch(1);
        var secondRequestStarted = new CountDownLatch(1);
        UserInfo userInfo = mock(UserInfo.class);
        when(idamClient.getUserInfo("Bearer same-token")).thenAnswer(invocation -> {
            if (firstLoadStarted.getCount() == 0) {
                overlappingLoad.countDown();
            } else {
                firstLoadStarted.countDown();
                assertThat(releaseLoad.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return userInfo;
        });

        try (var context = new AnnotationConfigApplicationContext();
             var requests = Executors.newFixedThreadPool(2)) {
            context.register(Caching.class, IdamRepository.class);
            context.registerBean(IdamClient.class, () -> idamClient);
            context.registerBean(CacheManager.class, () -> new CaffeineCacheManager("token"));
            context.refresh();
            IdamRepository repository = context.getBean(IdamRepository.class);
            var first = requests.submit(() -> repository.getUserInfo("same-token"));
            try {
                assertThat(firstLoadStarted.await(5, TimeUnit.SECONDS)).isTrue();
                final var second = requests.submit(() -> {
                    secondRequestStarted.countDown();
                    return repository.getUserInfo("same-token");
                });
                assertThat(secondRequestStarted.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(overlappingLoad.await(500, TimeUnit.MILLISECONDS)).isFalse();
                releaseLoad.countDown();
                assertThat(first.get(5, TimeUnit.SECONDS)).isSameAs(userInfo);
                assertThat(second.get(5, TimeUnit.SECONDS)).isSameAs(userInfo);
                verify(idamClient).getUserInfo("Bearer same-token");
            } finally {
                releaseLoad.countDown();
            }
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableCaching
    static class Caching {
    }
}
