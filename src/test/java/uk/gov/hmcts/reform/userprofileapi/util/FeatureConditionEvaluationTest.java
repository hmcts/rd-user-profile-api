package uk.gov.hmcts.reform.userprofileapi.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import uk.gov.hmcts.reform.userprofileapi.controller.WelcomeController;
import uk.gov.hmcts.reform.userprofileapi.exception.ForbiddenException;
import uk.gov.hmcts.reform.userprofileapi.service.impl.FeatureToggleServiceImpl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.userprofileapi.util.FeatureConditionEvaluation.BEARER;
import static uk.gov.hmcts.reform.userprofileapi.util.FeatureConditionEvaluation.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class FeatureConditionEvaluationTest {

    FeatureToggleServiceImpl featureToggleService = mock(FeatureToggleServiceImpl.class);

    @Spy
    FeatureConditionEvaluation featureConditionEvaluation = new FeatureConditionEvaluation(featureToggleService);
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    Method method = mock(Method.class);


    @BeforeEach
    public void before() {
        when(method.getName()).thenReturn("test");
        doReturn(WelcomeController.class).when(method).getDeclaringClass();
        when(handlerMethod.getMethod()).thenReturn(method);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPreHandleValidFlag() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        String token = generateDummyS2SToken();
        when(httpRequest.getHeader(SERVICE_AUTHORIZATION)).thenReturn(BEARER + token);
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(true);
        Assertions.assertTrue(featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleInvalidFlag() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        String token = generateDummyS2SToken();
        when(httpRequest.getHeader(SERVICE_AUTHORIZATION)).thenReturn(BEARER + token);
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> featureConditionEvaluation.preHandle(httpRequest,
                httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleInvalidServletRequestAttributes() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        assertThrows(ForbiddenException.class, () -> featureConditionEvaluation.preHandle(httpRequest,
                httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleNoFlag() throws Exception {
        Assertions.assertTrue(featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleNonConfiguredValues() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("DummyController.test", "test-flag");
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        Assertions.assertTrue(featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testRemoveBearerFromToken() {
        String result = featureConditionEvaluation.removeBearerFromToken("token");
        assertThat(result).isNotNull().isEqualTo("token");
    }

    static String generateDummyS2SToken() {
        return Jwts.builder()
                .setSubject("rd_user_profile_api")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
                .compact();
    }
}
