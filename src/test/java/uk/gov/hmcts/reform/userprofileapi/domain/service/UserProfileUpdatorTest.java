package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Request;
import feign.Response;

import java.nio.charset.Charset;
import java.util.ArrayList;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.DeleteRoleResponse;
import uk.gov.hmcts.reform.userprofileapi.client.RoleName;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileUpdator;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileUpdatorTest {

    @InjectMocks
    private UserProfileUpdator userProfileUpdator;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuditRepository auditRepository;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);

    private CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com", "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(), new HashSet<RoleName>());

    private UserProfile userProfile = new UserProfile(createUserProfileData, idamRegistrationInfo.getIdamRegistrationResponse());

    private final IdamFeignClient idamFeignClientMock = mock(IdamFeignClient.class);

    @Before
    public void setUp() {
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setIdamId("1234");
        userProfile.setId((long)1234);
    }

    @Test
    public void updateRoles() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();

        userProfileRolesResponse.setResponseStatusCode(HttpStatus.OK.toString());
        userProfileRolesResponse.setStatusMessage("Success");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        UserProfileRolesResponse response = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());

        assertThat(response).isNotNull();
        assertThat(response.getResponseStatusCode()).isEqualTo("200");
    }

    @Test
    public void updateRolesForAddAndDelete() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        //RoleName roleName2 = new RoleName("pui-organisation-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        // roles.add(roleName2);

        updateUserProfileData.setRolesDelete(roles);
        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse();
        deleteRoleResponse.setRoleName("pui-case-manager");
        deleteRoleResponse.setIdamGetResponseStatusCode(HttpStatus.OK.toString());
        deleteRoleResponse.setStatusMessage("Success");
        List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<DeleteRoleResponse>();
        deleteRoleResponses.add(deleteRoleResponse);
        /* DeleteRoleResponse deleteRoleResponse1 = new DeleteRoleResponse();
        deleteRoleResponse1.setRoleName("pui-organisation-manager");
        deleteRoleResponse1.setIdamGetResponseStatusCode(HttpStatus.OK.toString());
        deleteRoleResponse1.setStatusMessage("Success");
        deleteRoleResponses.add(deleteRoleResponse1);*/
        //userProfileRolesResponse.setDeleteResponses(deleteRoleResponses);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(deleteRoleResponse);

        when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        Response response = Response.builder().request(Request.create(Request.HttpMethod.DELETE, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build();

        // Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build()
        when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(response);

        UserProfileRolesResponse response1 = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());

        assertThat(response1).isNotNull();
        assertThat(response1.getDeleteResponses().size()).isEqualTo(1);
        assertThat(response1.getDeleteResponses().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(response1.getDeleteResponses().get(0).getIdamGetResponseStatusCode()).isEqualTo("200");
    }

    @Test
    public void updateRoles_InternalServerError() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();

        userProfileRolesResponse.setResponseStatusCode(HttpStatus.OK.toString());
        userProfileRolesResponse.setStatusMessage("Success");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(500).build());

        UserProfileRolesResponse response = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(response.getResponseStatusCode()).isEqualTo("500");
    }

    @Test(expected = InvalidRequest.class)
    public void updateRoles_InvalidRequest() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);
        userProfile.setStatus(IdamStatus.PENDING);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();

        userProfileRolesResponse.setResponseStatusCode(HttpStatus.OK.toString());
        userProfileRolesResponse.setStatusMessage("Success");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());
    }

    @Test
    public void should_update_user_profile_successfully() {

        String userId = UUID.randomUUID().toString();

        when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileUpdator.update(updateUserProfileData, userId.toString());

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("email@net.com");
        assertThat(response.getFirstName()).isEqualTo("firstName");
        assertThat(response.getLastName()).isEqualTo("lastName");
        assertThat(response.getStatus()).isEqualTo(IdamStatus.ACTIVE);

        verify(userProfileRepository,times(1)).save(any(UserProfile.class));
        verify(auditRepository,times(1)).save(any(Audit.class));

    }

    @Test
    public void should_throw_ResourceNotFound_when_userId_not_valid() {

        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,"invalid")).isExactlyInstanceOf(ResourceNotFoundException.class);
        verify(auditRepository,times(1)).save(any(Audit.class));
    }

    @Test
    public void should_throw_IdamServiceException_when_user_user_profile_not_found_in_db() {

        String userId = UUID.randomUUID().toString();
        when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(null));
        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,userId.toString())).isExactlyInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void should_throw_IdamServiceException_when_request_invalid() {

        String userId = UUID.randomUUID().toString();
        RoleName roleName = new RoleName("prd-admin");
        Set<RoleName> roleNames = new HashSet<RoleName>();
        roleNames.add(roleName);
        updateUserProfileData = new UpdateUserProfileData("", "", "", "ACTIV", roleNames,roleNames);
        when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));
        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,userId.toString())).isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

}
