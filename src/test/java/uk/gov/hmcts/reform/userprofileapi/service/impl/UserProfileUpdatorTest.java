package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleAdditionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileUpdatorTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @Mock
    private ValidationService validationServiceMock;

    @Mock
    private AuditService auditServiceMock;

    @Mock
    private IdamService idamServiceMock;

    @Mock
    private ValidationHelperService validationHelperServiceMock;

    private AttributeResponse attributeResponse = new AttributeResponse(status(OK).build());

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(CREATED).build());

    private UserProfileCreationData userProfileCreationData
            = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com",
            "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(),
            new HashSet<RoleName>());

    private UserProfile userProfile = new UserProfile(userProfileCreationData,
            idamRegistrationInfo.getIdamRegistrationResponse());

    private final IdamFeignClient idamFeignClientMock = mock(IdamFeignClient.class);

    public static final String EXUI = "EXUI";

    public static final String SYNC = "sync";

    @InjectMocks
    private UserProfileUpdator sut;

    private final String dummyEmail = "email@net.com";
    private final String dummyFirstName = "april";
    private final String dummyLastName = "o'neil";

    private String userId;

    @Before
    public void setUp() {
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setIdamId("1234");
        userProfile.setId((long) 1234);

        userId = UUID.randomUUID().toString();
    }

    @Test
    public void test_updateRolesForAdd() throws Exception {
        UserProfileRolesResponse response = addRoles();

        assertThat(response).isNotNull();
        assertThat(response.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200");

        verify(idamFeignClientMock, times(1)).addUserRoles(any(), any(String.class));
    }

    @Test
    public void test_updateRolesForAddAndDelete() throws Exception {

        UserProfileRolesResponse response;
        response = addRoles();

        assertThat(response).isNotNull();
        assertThat(response.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200");

        response = deleteRoles();

        assertThat(response).isNotNull();
        assertThat(response.getRoleDeletionResponse().size()).isEqualTo(1);
        assertThat(response.getRoleDeletionResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(response.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("200");

        verify(idamFeignClientMock, times(2)).addUserRoles(any(), any(String.class));
        verify(idamFeignClientMock, times(1)).deleteUserRole(any(), any(String.class));
    }

    @Test
    public void test_updateRolesForDelete() throws Exception {
        UserProfileRolesResponse response1 = deleteRoles();

        assertThat(response1).isNotNull();
        assertThat(response1.getRoleDeletionResponse().size()).isEqualTo(1);
        assertThat(response1.getRoleDeletionResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(response1.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("200");

        verify(idamFeignClientMock, times(1)).deleteUserRole(any(), any(String.class));
    }

    @Test
    public void test_addRoles_InternalServerError() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        roleAdditionResponse.setIdamMessage("Failure");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(userProfileResponse);

        Response response = Response.builder().request(mock(Request.class)).body(body,
                Charset.defaultCharset()).status(500).build();

        Response responseMock = mock(Response.class);
        when(responseMock.status()).thenReturn(500);
        when(responseMock.body()).thenReturn(response.body());

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234"))
                .thenReturn(responseMock);

        UserProfileRolesResponse updateRolesResponse = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(updateRolesResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("500");

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(idamFeignClientMock, times(1)).addUserRoles(any(), any(String.class));
    }

    @Test
    public void test_updateRoles_FeignException_WhenAddRoles() {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        roleAdditionResponse.setIdamMessage("Failure");

        FeignException feignExceptionMock = Mockito.mock(FeignException.class);
        when(feignExceptionMock.status()).thenReturn(IdamServiceImplTest.StatusCode.INTERNAL_SERVER_ERROR.getStatus());
        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234"))
                .thenThrow(feignExceptionMock);

        UserProfileRolesResponse response = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(response.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("500");

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(idamFeignClientMock, times(1)).addUserRoles(any(), any(String.class));
        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(UserProfile.class), any(ResponseSource.class));
    }

    @Test
    public void test_updateRoles_InternalServerError_WhenDeleteRoles() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);

        updateUserProfileData.setRolesDelete(roles);

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        roleDeletionResponse.setIdamMessage("Failure");
        List<RoleDeletionResponse> rolesResponse = new ArrayList<RoleDeletionResponse>();
        rolesResponse.add(roleDeletionResponse);

        UserProfileResponse userProfileRolesResponse = new UserProfileResponse();
        userProfileRolesResponse.setRoleDeletionResponse(rolesResponse);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        Response response = Response.builder().request(mock(Request.class)).body(body,
                Charset.defaultCharset()).status(500).build();

        Response responseMock = mock(Response.class);
        when(responseMock.status()).thenReturn(500);
        when(responseMock.body()).thenReturn(response.body());

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(responseMock);

        UserProfileRolesResponse updateRolesResponse = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(updateRolesResponse.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("500");

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(idamFeignClientMock, times(1)).deleteUserRole(any(String.class),
                any(String.class));
    }

    @Test
    public void test_updateRoles_FeignException_WhenDeleteRoles() {
        RoleName roleName1 = new RoleName("pui-case-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        updateUserProfileData.setRolesDelete(roles);

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        roleDeletionResponse.setIdamMessage("Failure");
        List<RoleDeletionResponse> rolesResponse = new ArrayList<>();
        rolesResponse.add(roleDeletionResponse);

        FeignException feignExceptionMock = Mockito.mock(FeignException.class);
        when(feignExceptionMock.status()).thenReturn(IdamServiceImplTest.StatusCode.INTERNAL_SERVER_ERROR.getStatus());

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager"))
                .thenThrow(feignExceptionMock);

        UserProfileRolesResponse response = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(response.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("500");

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(idamFeignClientMock, times(1)).deleteUserRole(any(String.class),
                any(String.class));
        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(UserProfile.class), any(ResponseSource.class));
    }

    @Test(expected = InvalidRequest.class)
    public void test_updateRoles_addRoles_InvalidRequest() {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);
        userProfile.setStatus(IdamStatus.PENDING);

        updateUserProfileData.setRolesAdd(roles);

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        sut.updateRoles(updateUserProfileData, userProfile.getIdamId());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
    }

    @Test
    public void test_update_user_profile_successfully() {

        String userId = UUID.randomUUID().toString();

        when(userProfileRepositoryMock.save(any(UserProfile.class))).thenReturn(userProfile);

        when(validationServiceMock.validateUpdate(any(), any(), any())).thenReturn(userProfile);

        when(validationHelperServiceMock.validateUserPersisted(any())).thenReturn(true);

        AttributeResponse response = sut.update(updateUserProfileData, userId, EXUI);

        assertThat(response).isNotNull();

        verify(userProfileRepositoryMock, times(1)).save(any(UserProfile.class));
        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.OK),
                any(UserProfile.class), any());
    }

    @Test
    public void test_update_user_profile_successfully_for_sync() {
        when(userProfileRepositoryMock.save(any(UserProfile.class))).thenReturn(userProfile);
        when(validationServiceMock.validateUpdate(any(), any(), any())).thenReturn(userProfile);
        when(validationHelperServiceMock.validateUserPersisted(any())).thenReturn(true);

        AttributeResponse response = sut.update(updateUserProfileData, userProfile.getIdamId(), SYNC);

        assertThat(response).isNotNull();

        verify(userProfileRepositoryMock, times(1)).save(any(UserProfile.class));
        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.OK),
                any(UserProfile.class), eq(ResponseSource.SYNC));
    }

    @Test
    public void test_update_idam_user_details_successfully() {

        String userId = UUID.randomUUID().toString();

        when(userProfileRepositoryMock.save(any(UserProfile.class))).thenReturn(userProfile);

        when(validationServiceMock.isExuiUpdateRequest(any())).thenReturn(false);
        when(validationServiceMock.validateUpdate(any(), any(), any())).thenReturn(userProfile);

        AttributeResponse response = sut.update(updateUserProfileData, userId, EXUI);

        assertThat(response).isNotNull();
        assertThat(updateUserProfileData.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());

        verify(userProfileRepositoryMock, times(1)).save(any(UserProfile.class));
        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.OK),
                any(UserProfile.class), any());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_update_should_throw_ResourceNotFound_when_userId_not_valid() {

        when(validationServiceMock.validateUpdate(any(), any(), any())).thenThrow(ResourceNotFoundException.class);

        sut.update(updateUserProfileData, "invalid", EXUI);

        verify(validationServiceMock, times(1)).validateUpdate(any(), any(), any());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_update_should_throw_IdamServiceException_when_user_user_profile_not_found_in_db() {

        String userId = UUID.randomUUID().toString();

        when(validationServiceMock.validateUpdate(any(), any(), any())).thenThrow(ResourceNotFoundException.class);

        sut.update(updateUserProfileData, userId, EXUI);

        verify(validationServiceMock, times(1)).validateUpdate(any(), any(), any());
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void test_update_should_throw_IdamServiceException_when_request_invalid() {

        String userId = UUID.randomUUID().toString();

        when(validationServiceMock.validateUpdate(any(), eq(userId), any()))
                .thenThrow(RequiredFieldMissingException.class);

        sut.update(updateUserProfileData, userId, EXUI);

        verify(validationServiceMock, times(1)).validateUpdate(any(), any(), any());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_userProfileRolesResponse_addRoles_nullProfile() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileResponse userProfileRolesResponse = new UserProfileResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        sut.updateRoles(updateUserProfileData, "1567");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_userProfileRolesResponse_update_invalid_user() {

        when(validationServiceMock.validateUpdate(any(), any(), any())).thenThrow(ResourceNotFoundException.class);

        sut.update(updateUserProfileData, "", EXUI);

        verify(validationServiceMock, times(1)).validateUpdate(any(), any(), any());
    }

    private UserProfileRolesResponse addRoles() throws Exception {
        Set<RoleName> roles = new HashSet<>();
        roles.add(new RoleName("pui-case-manager"));
        roles.add(new RoleName("pui-case-organisation"));
        updateUserProfileData.setRolesAdd(roles);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();

        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");

        userProfileResponse.setRoleAdditionResponse(roleAdditionResponse);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(userProfileResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234"))
                .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(200).build());

        return sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
    }

    private UserProfileRolesResponse deleteRoles() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);

        updateUserProfileData.setRolesDelete(roles);
        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setRoleName("pui-case-manager");
        roleDeletionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleDeletionResponse.setIdamMessage("Success");
        List<RoleDeletionResponse> roleDeletionResponses = new ArrayList<RoleDeletionResponse>();
        roleDeletionResponses.add(roleDeletionResponse);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(roleDeletionResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        Response response = Response.builder().request(Request.create(Request.HttpMethod.DELETE, "",
                new HashMap<>(), Request.Body.empty(), null)).body(body,
                Charset.defaultCharset()).status(200).build();

        when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(response);

        return sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
    }

    @Test
    public void test_updateSidamAndUserProfile() {

        String userId = UUID.randomUUID().toString();

        when(idamServiceMock.updateUserDetails(any(), any())).thenReturn(attributeResponse);
        when(userProfileRepositoryMock.save(any())).thenReturn(userProfile);
        AttributeResponse response = sut.updateSidamAndUserProfile(updateUserProfileData, userProfile,
                ResponseSource.API, userId);
        assertThat(response).isNotNull();

        verify(userProfileRepositoryMock, times(1)).save(any());
    }

    @Test
    public void test_getHttpStatusFromFeignException_with_RetryableException() {
        FeignException feignException = new RetryableException(400, "some message", Request.HttpMethod.GET, new Date(),
                Request.create(Request.HttpMethod.DELETE, "", new HashMap<>(), Request.Body.empty(),
                        null));
        HttpStatus httpStatus = sut.getHttpStatusFromFeignException(feignException);
        assertThat(httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void test_getHttpStatusFromFeignException_with_no_RetryableException() {

        FeignException feignExceptionMock = mock(FeignException.class);
        when(feignExceptionMock.status()).thenReturn(400);
        HttpStatus httpStatus = sut.getHttpStatusFromFeignException(feignExceptionMock);
        assertThat(httpStatus).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(feignExceptionMock, times(1)).status();
    }

}
