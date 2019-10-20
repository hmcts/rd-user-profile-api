package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.AddRoleResponse;
import uk.gov.hmcts.reform.userprofileapi.client.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.DeleteRoleResponse;
import uk.gov.hmcts.reform.userprofileapi.client.RoleName;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
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

    @Mock
    AttributeResponse response;

    @Mock
    IdamService idamService;

    @Mock
    AttributeResponse attributeResponse;

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

        response = updateAttributesInProfileData();
        assertThat(response).isNotNull();
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
        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        addRoleResponse.setIdamMessage("Failure");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        Mockito.when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        Mockito.when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(500).build());
        UserProfileRolesResponse response = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());
        assertThat(response.getAddRolesResponse().getIdamStatusCode()).isEqualTo("500");
    }

    @Test
    public void deleteRoles_InternalServerError() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);

        updateUserProfileData.setRolesDelete(roles);

        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse();
        deleteRoleResponse.setIdamStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        deleteRoleResponse.setIdamMessage("Failure");
        List<DeleteRoleResponse> rolesResponse = new ArrayList<DeleteRoleResponse>();
        rolesResponse.add(deleteRoleResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileRolesResponse.setDeleteRolesResponse(rolesResponse);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        Mockito.when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        Mockito.when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(500).build());

        UserProfileRolesResponse response = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());
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
        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        addRoleResponse.setIdamMessage("Success");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        Mockito.when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());
    }

    @Test
    public void should_update_user_profile_by_sync_successfully() {

        String userId = UUID.randomUUID().toString();

        Mockito.when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));
        Mockito.when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        AttributeResponse response = userProfileUpdator.update(updateUserProfileData, userId, null);

        assertThat(response).isNotNull();

        Mockito.verify(userProfileRepository, Mockito.times(1)).save(any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));

    }

    @Test
    public void should_update_user_details_by_exui_successfully() {

        UpdateUserDetails updateUserDetailsMock = mock(UpdateUserDetails.class);
        updateUserDetailsMock.setActive(true);
        updateUserDetailsMock.setForename("firstName");
        updateUserDetailsMock.setSurname("lastName");
        String userId = UUID.randomUUID().toString();
        Mockito.when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));

        AttributeResponse response = userProfileUpdator.update(updateUserProfileData, userId, "EXUI");

        assertThat(response).isNotNull();

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));

    }

    @Test
    public void should_update_user_status_by_exui_successfully() {

        UpdateUserDetails updateUserDetailsMock = mock(UpdateUserDetails.class);
        updateUserDetailsMock.setActive(true);
        updateUserDetailsMock.setForename("firstName");
        updateUserDetailsMock.setSurname("lastName");
        String userId = UUID.randomUUID().toString();
        Mockito.when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();
        updateUserProfileData.setIdamStatus("SUSPENDED");
        AttributeResponse response = userProfileUpdator.update(updateUserProfileData, userId, "EXUI");

        assertThat(response).isNotNull();

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));

    }

    @Test
    public void should_throw_ResourceNotFound_when_userId_not_valid() {

        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,"invalid", "EXUI")).isExactlyInstanceOf(ResourceNotFoundException.class);
        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
    }

    @Test
    public void should_throw_IdamServiceException_when_user_user_profile_not_found_in_db() {

        String userId = UUID.randomUUID().toString();
        Mockito.when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(null));
        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,userId.toString(),"EXUI")).isExactlyInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void should_throw_IdamServiceException_when_request_invalid() {

        String userId = UUID.randomUUID().toString();
        RoleName roleName = new RoleName("prd-admin");
        Set<RoleName> roleNames = new HashSet<RoleName>();
        roleNames.add(roleName);
        updateUserProfileData = new UpdateUserProfileData("", "", "", "ACTIV", roleNames,roleNames);
        Mockito.when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));
        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData, userId, "EXUI")).isExactlyInstanceOf(RequiredFieldMissingException.class);
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
        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        addRoleResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAddRolesResponse(addRoleResponse);

        userProfileUpdator.updateRoles(updateUserProfileData, "1567");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void userProfileRolesResponse_update_invalid_user() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        addRoleResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAddRolesResponse(addRoleResponse);

        userProfileUpdator.update(updateUserProfileData, "xyz", null);
    }

    private UserProfileRolesResponse addRoles() throws Exception {
        UpdateUserProfileData updateUserProfileData = new  UpdateUserProfileData();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        addRoleResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAddRolesResponse(addRoleResponse);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        Mockito.when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        Mockito.when(idamFeignClientMock.addUserRoles(updateUserProfileData.getRolesAdd(), "1234")).thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        UserProfileRolesResponse response = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());

        return response;
    }

    private UserProfileRolesResponse updateAttributesInProfileData() throws Exception {
        UpdateUserProfileData updataAttData = new UpdateUserProfileData();
        updataAttData.setFirstName("firstName");
        updataAttData.setLastName("lastName");
        updataAttData.setEmail("some@gmail.com");
        updataAttData.setIdamStatus(IdamStatus.SUSPENDED.name());

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(HttpStatus.OK.toString());
        attributeResponse.setIdamMessage("Success");
        userProfileRolesResponse.setAttributeResponse(attributeResponse);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(userProfileRolesResponse);

        Mockito.when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        UserProfileRolesResponse response = userProfileUpdator.updateRoles(updataAttData, userProfile.getIdamId());

        return response;
    }


    private UserProfileRolesResponse deleteRoles() throws Exception {
        RoleName roleName1 = new RoleName("pui-case-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        UpdateUserProfileData updateUserProfileData = new  UpdateUserProfileData();
        updateUserProfileData.setRolesDelete(roles);
        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse();
        deleteRoleResponse.setRoleName("pui-case-manager");
        deleteRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        deleteRoleResponse.setIdamMessage("Success");
        List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<DeleteRoleResponse>();
        deleteRoleResponses.add(deleteRoleResponse);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = mapper.writeValueAsString(deleteRoleResponse);

        Mockito.when(userProfileRepository.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        Response response = Response.builder().request(Request.create(Request.HttpMethod.DELETE, "", new HashMap<>(), Request.Body.empty())).body(body, Charset.defaultCharset()).status(200).build();

        Mockito.when(idamFeignClientMock.deleteUserRole("1234", "pui-case-manager")).thenReturn(response);

        UserProfileRolesResponse response1 = userProfileUpdator.updateRoles(updateUserProfileData, userProfile.getIdamId());
        return response1;
    }

}
