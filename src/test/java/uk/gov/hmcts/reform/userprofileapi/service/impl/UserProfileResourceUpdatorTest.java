package uk.gov.hmcts.reform.userprofileapi.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleAdditionResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileResourceUpdatorTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @Mock
    private AuditRepository auditRepositoryMock;

    @Mock
    private ValidationService validationServiceMock;

    @Mock
    private AuditService auditServiceMock;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);

    private UserProfileCreationData userProfileCreationData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com", "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(), new HashSet<RoleName>());

    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo.getIdamRegistrationResponse());

    private final IdamFeignClient idamFeignClientMock = mock(IdamFeignClient.class);

    @InjectMocks
    private UserProfileResourceUpdator sut;

    @Before
    public void setUp() {
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setIdamId("1234");
        userProfile.setId((long)1234);
    }

    @Test
    public void updateRolesForAdd() throws Exception {

        UserProfileRolesResponse response = addRoles();

        assertThat(response).isNotNull();
        assertThat(response.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200");
    }

    @Test
    public void updateRolesForAddAndDelete() throws Exception {

        UserProfileRolesResponse response;
        response = addRoles();

        assertThat(response).isNotNull();
        assertThat(response.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200");

        response = deleteRoles();

        assertThat(response).isNotNull();
        assertThat(response.getDeleteRolesResponse().size()).isEqualTo(1);
        assertThat(response.getDeleteRolesResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(response.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");
    }

    @Test
    public void updateRolesForDelete() throws Exception {
        UserProfileRolesResponse response1 = deleteRoles();

        assertThat(response1).isNotNull();
        assertThat(response1.getDeleteRolesResponse().size()).isEqualTo(1);
        assertThat(response1.getDeleteRolesResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(response1.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");
    }

    @Test
    public void addRoles_InternalServerError() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        roleAdditionResponse.setIdamMessage("Failure");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(500).build());

        UserProfileRolesResponse response = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(response.getAddRolesResponse().getIdamStatusCode()).isEqualTo("500");
    }

    @Test
    public void deleteRoles_InternalServerError() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        // RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);

        updateUserProfileData.setRolesDelete(roles);

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        roleDeletionResponse.setIdamMessage("Failure");
        List<RoleDeletionResponse> rolesResponse = new ArrayList<RoleDeletionResponse>();
        rolesResponse.add(roleDeletionResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileRolesResponse.setDeleteRolesResponse(rolesResponse);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(500).build());

        UserProfileRolesResponse response = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(response.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("500");
    }

    @Test(expected = InvalidRequest.class)
    public void addRoles_InvalidRequest() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);
        userProfile.setStatus(IdamStatus.PENDING);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
    }

    @Test
    public void should_update_user_profile_successfully() {

        String userId = UUID.randomUUID().toString();

        Optional<UserProfile> expected = Optional.of(userProfile);

        when(validationServiceMock.validateUpdate(any(), any())).thenReturn(expected);

        when(userProfileRepositoryMock.save(any(UserProfile.class))).thenReturn(userProfile);

        Optional<UserProfile> response = sut.update(updateUserProfileData, userId);

        assertThat(response).isNotNull();
        assertThat(response.get().getEmail()).isEqualTo("email@net.com");
        assertThat(response.get().getFirstName()).isEqualTo("firstName");
        assertThat(response.get().getLastName()).isEqualTo("lastName");
        assertThat(response.get().getStatus()).isEqualTo(IdamStatus.ACTIVE);

        verify(userProfileRepositoryMock,times(1)).save(any(UserProfile.class));


        //  TODO verify in separate auditService test
        //! verify(auditRepositoryMock,times(1)).save(any(Audit.class));

    }

    @Test
    public void should_throw_ResourceNotFound_when_userId_not_valid() {

        when(validationServiceMock.validateUpdate(any(), any())).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> sut.update(updateUserProfileData,"invalid")).isExactlyInstanceOf(ResourceNotFoundException.class);

        //! verify(auditRepositoryMock, times(1)).save(any(Audit.class));
        //TODO verify auditService independantly
    }

    @Test
    public void should_throw_IdamServiceException_when_user_user_profile_not_found_in_db() {

        String userId = UUID.randomUUID().toString();

        when(validationServiceMock.validateUpdate(any(), any())).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> sut.update(updateUserProfileData, userId)).isExactlyInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void should_throw_IdamServiceException_when_request_invalid() {

        String userId = UUID.randomUUID().toString();

        when(validationServiceMock.validateUpdate(any(), eq(userId))).thenThrow(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> sut.update(updateUserProfileData, userId)).isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void userProfileRolesResponse_addRoles_nullProfile() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAddRolesResponse(roleAdditionResponse);

        sut.updateRoles(updateUserProfileData, "1567");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void userProfileRolesResponse_update_invalid_user() {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAddRolesResponse(roleAdditionResponse);

        when(validationServiceMock.validateUpdate(any(), any())).thenThrow(ResourceNotFoundException.class);

        sut.update(updateUserProfileData, "");
    }

    private UserProfileRolesResponse addRoles() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAddRolesResponse(roleAdditionResponse);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        UserProfileRolesResponse response = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());

        return response;
    }

    private UserProfileRolesResponse deleteRoles() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);

        updateUserProfileData.setRolesDelete(roles);
        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setRoleName("pui-case-manager");
        roleDeletionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleDeletionResponse.setIdamMessage("Success");
        List<RoleDeletionResponse> roleDeletionRespons = new ArrayList<RoleDeletionResponse>();
        roleDeletionRespons.add(roleDeletionResponse);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(roleDeletionResponse);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        Response response = Response.builder().request(Request.create(Request.HttpMethod.DELETE, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build();

        when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(response);

        UserProfileRolesResponse response1 = sut.updateRoles(updateUserProfileData, userProfile.getIdamId());
        return response1;
    }

}