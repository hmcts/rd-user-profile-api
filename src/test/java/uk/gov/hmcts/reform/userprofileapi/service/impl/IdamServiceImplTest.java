package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.userprofileapi.service.impl.IdamServiceImplTest.StatusCode.CREATED;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;


@RunWith(MockitoJUnitRunner.class)
public class IdamServiceImplTest {

    private final String userId = "test796-d05e-480d-bf3d-7cbfacb3ca29";
    private final String email = "test.user@test.com";
    private final Map<String, Collection<String>> headerData = new HashMap<>();

    private IdamFeignClient idamFeignClientMock = Mockito.mock(IdamFeignClient.class);

    @InjectMocks
    private IdamService sut = new IdamServiceImpl();

    Map<String, Collection<String>> header = new HashMap<>();
    Request request = mock(Request.class);
    Response responseMock = Response.builder().status(200).reason("OK").headers(header)
            .body("{\"idamId\": 1}", UTF_8).request(request).build();

    @Test
    public void test_RegisterUser() {
        IdamRegisterUserRequest dataMock = Mockito.mock(IdamRegisterUserRequest.class);
        Response responseMock = Mockito.mock(Response.class);

        when(idamFeignClientMock.createUserProfile(dataMock)).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(CREATED.getStatus());
        when(responseMock.headers()).thenReturn(headerData);

        IdamRegistrationInfo idamId = sut.registerUser(dataMock);

        assertThat(idamId.getIdamRegistrationResponse()).isNotNull();
        assertThat(idamId.getIdamRegistrationResponse().value()).isEqualTo(HttpStatus.ACCEPTED.value());

        verify(idamFeignClientMock, times(1)).createUserProfile(any());
    }

    @Test
    public void test_FetchUserById() {
        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();
        Response responseMock = Mockito.mock(Response.class);

        when(idamFeignClientMock.getUserById(userId)).thenReturn(responseMock);
        when(responseMock.headers()).thenReturn(headerData);
        when(responseMock.status()).thenReturn(StatusCode.NOT_FOUND.getStatus());
        when(responseMock.body()).thenReturn(response.body());

        IdamRolesInfo idamRolesInfo = sut.fetchUserById(userId);

        assertThat(idamRolesInfo).isNotNull();
        assertThat(idamRolesInfo.getResponseStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(idamRolesInfo.getStatusMessage()).isEqualTo("16 Resource not found");

        verify(idamFeignClientMock, times(1)).getUserById(any());

    }

    @Test
    public void test_RegisterUserWithFeignExceptionThrown() {
        FeignException feignExceptionMock = Mockito.mock(FeignException.class);
        IdamRegisterUserRequest dataMock = Mockito.mock(IdamRegisterUserRequest.class);

        when(idamFeignClientMock.createUserProfile(dataMock)).thenThrow(feignExceptionMock);
        when(feignExceptionMock.status()).thenReturn(StatusCode.NOT_FOUND.getStatus());

        IdamRegistrationInfo idamId = sut.registerUser(dataMock);

        assertThat(idamId.getIdamRegistrationResponse()).isNotNull();
        assertThat(idamId.getIdamRegistrationResponse().value())
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        verify(idamFeignClientMock, times(1)).createUserProfile(any());
    }

    @Test
    public void test_GetHttpStatusFromFeignException() {
        IdamServiceImpl idamService = new IdamServiceImpl();
        RetryableException retryableExceptionMock = mock(RetryableException.class);

        HttpStatus status = idamService.gethttpStatusFromFeignException(retryableExceptionMock);
        assertThat(status).isNotNull().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(400);

        HttpStatus status1 = idamService.gethttpStatusFromFeignException(feignException);
        assertThat(status1).isNotNull().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_UpdateUserRoles() {
        List<String> roleRequest = new ArrayList<>();
        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Response responseMock = Mockito.mock(Response.class);

        when(idamFeignClientMock.updateUserRoles(roleRequest, userId)).thenReturn(responseMock);
        when(responseMock.headers()).thenReturn(headerData);
        when(responseMock.body()).thenReturn(response.body());

        // NB, technical exception to avoid coupling test to logic inside static method of a separate class
        when(responseMock.status()).thenReturn(StatusCode.INTERNAL_SERVER_ERROR.getStatus());

        IdamRolesInfo result = sut.updateUserRoles(roleRequest, userId);

        verify(idamFeignClientMock, times(1)).updateUserRoles(roleRequest, userId);
        verify(responseMock, times(2)).headers();
        verify(responseMock, times(3)).status();

        assertThat(result).isNotNull();
    }

    @Test
    public void test_UpdateUserRolesWhenFeignException() {
        List<String> roleRequest = new ArrayList<>();

        FeignException feignExceptionMock = Mockito.mock(FeignException.class);

        when(feignExceptionMock.status()).thenReturn(StatusCode.NOT_FOUND.getStatus());
        when(idamFeignClientMock.updateUserRoles(roleRequest, userId)).thenThrow(feignExceptionMock);

        IdamRolesInfo result = sut.updateUserRoles(roleRequest, userId);

        verify(idamFeignClientMock, times(1)).updateUserRoles(roleRequest, userId);
        assertThat(result).isNotNull();
    }

    @Test
    public void testAddUserRoles() {
        Set<Map<String, String>> roleRequest = new HashSet<>();
        Map<String, String> rolesMap = new HashMap<String, String>();
        rolesMap.put("name", "pui-caa");
        roleRequest.add(rolesMap);

        when(idamFeignClientMock.addUserRoles(roleRequest, userId)).thenReturn(responseMock);
        IdamRolesInfo result = sut.addUserRoles(roleRequest, userId);

        verify(idamFeignClientMock, times(1)).addUserRoles(roleRequest, userId);
        assertThat(result).isNotNull();
    }

    @Test
    public void test_UpdateUserDetails() {
        UpdateUserDetails updateUserDetailsMock = Mockito.mock(UpdateUserDetails.class);

        Response responseMock = Mockito.mock(Response.class);

        when(idamFeignClientMock.updateUserDetails(updateUserDetailsMock, userId)).thenReturn(responseMock);
        when(responseMock.headers()).thenReturn(headerData);

        when(responseMock.status()).thenReturn(StatusCode.OK.getStatus());

        AttributeResponse result = sut.updateUserDetails(updateUserDetailsMock, userId);

        verify(idamFeignClientMock, times(1)).updateUserDetails(updateUserDetailsMock, userId);
        verify(responseMock, times(1)).headers();
        verify(responseMock, times(3)).status();

        assertThat(result).isNotNull();
        assertThat(result.getIdamStatusCode()).isEqualTo(200);
        assertThat(result.getIdamMessage()).isEqualTo("11 OK");

    }

    @Test
    public void test_UpdateUserDetails_withFailure() {
        UpdateUserDetails updateUserDetailsMock = Mockito.mock(UpdateUserDetails.class);

        FeignException feignExceptionMock = Mockito.mock(FeignException.class);

        when(idamFeignClientMock.updateUserDetails(updateUserDetailsMock, userId)).thenThrow(feignExceptionMock);

        AttributeResponse result = sut.updateUserDetails(updateUserDetailsMock, userId);

        verify(idamFeignClientMock, times(1)).updateUserDetails(updateUserDetailsMock, userId);

        assertThat(result).isNotNull();
    }

    @Test
    public void test_AddUserRolesWhenFeignException() {
        Set<Map<String, String>> roleRequest = new HashSet<>();
        Map<String, String> rolesMap = new HashMap<String, String>();
        rolesMap.put("name", "pui-caa");
        roleRequest.add(rolesMap);

        FeignException feignExceptionMock = Mockito.mock(FeignException.class);

        when(feignExceptionMock.status()).thenReturn(StatusCode.NOT_FOUND.getStatus());
        when(idamFeignClientMock.addUserRoles(roleRequest, userId)).thenThrow(feignExceptionMock);

        IdamRolesInfo result = sut.addUserRoles(roleRequest, userId);

        verify(idamFeignClientMock, times(1)).addUserRoles(roleRequest, userId);
        assertThat(result).isNotNull();
    }

    //tbc refactor this out in favor of a public enum
    enum StatusCode {
        OK(200),
        CREATED(202),
        NOT_FOUND(404),
        INTERNAL_SERVER_ERROR(500);

        final int content;

        StatusCode(int content) {
            this.content = content;
        }

        int getStatus() {
            return content;
        }
    }

}