package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileCreator;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileCreatorTest {

    @InjectMocks
    private UserProfileCreator userProfileCreator;

    @Mock
    private IdamService idamService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuditRepository auditRepository;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);

    private CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();

    private UserProfile userProfile = new UserProfile(createUserProfileData, idamRegistrationInfo.getIdamRegistrationResponse());

    private IdamRolesInfo idamRolesInfo = Mockito.mock(IdamRolesInfo.class);

    @Test
    public void should_create_user_profile_successfully() {

        Mockito.when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileCreator.create(createUserProfileData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = Mockito.inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository).save(any(UserProfile.class));

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));

    }

    @Test
    public void should_create_user_profile_successfully_profileData_has_status_populated() {

        Mockito.when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        createUserProfileData.setStatus(IdamStatus.PENDING);
        UserProfile response = userProfileCreator.create(createUserProfileData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = Mockito.inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository).save(any(UserProfile.class));

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
        assertThat(response.getIdamId()).isNull();
        assertThat(response.getStatus()).isNotNull();

    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtime_when_create_user_profile_fails_to_save() {

        Mockito.when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(any(UserProfile.class))).thenThrow(new RuntimeException());

        UserProfile response = userProfileCreator.create(createUserProfileData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = Mockito.inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository).save(any(UserProfile.class));

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));

    }

    @Test
    public void should_throw_IdamServiceException_when_user_already_exist() {

        Audit audit = Mockito.mock(Audit.class);
        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        assertThatThrownBy(() -> userProfileCreator.create(createUserProfileData)).isExactlyInstanceOf(IdamServiceException.class);
        Mockito.verify(userProfileRepository, Mockito.times(0)).save(any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
    }

    @Test
    public void should_throw_IdamServiceException_when_idam_registration_fail() {

        Audit audit = Mockito.mock(Audit.class);
        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.BAD_REQUEST);
        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(idamService.registerUser(any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        assertThatThrownBy(() -> userProfileCreator.create(createUserProfileData)).isExactlyInstanceOf(IdamServiceException.class);
        Mockito.verify(userProfileRepository, Mockito.times(0)).save(any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
    }

    @Test
    public void should_register_when_idam_registration_conflicts() {

        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manager");
        Audit audit = Mockito.mock(Audit.class);
        ResponseEntity entity = Mockito.mock(ResponseEntity.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/users/" + UUID.randomUUID().toString());
        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CONFLICT, Optional.ofNullable(entity));

        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        Mockito.when(idamService.registerUser(any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        Mockito.when(entity.getHeaders()).thenReturn(headers);
        Mockito.when(idamRolesInfo.getRoles()).thenReturn(roles);
        Mockito.when(idamRolesInfo.getResponseStatusCode()).thenReturn(HttpStatus.OK);
        Mockito.when(idamRolesInfo.getStatusMessage()).thenReturn("test error message");
        Mockito.when(idamRolesInfo.isSuccessFromIdam()).thenReturn(true);
        Mockito.when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        Mockito.when(idamRolesInfo.getForename()).thenReturn("fname");
        Mockito.when(idamRolesInfo.getSurname()).thenReturn("lastName");

        Mockito.when(idamService.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        Mockito.when(idamService.updateUserRoles(any(), Mockito.anyString())).thenReturn(idamRolesInfo);

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = createDecisionMap();
        ReflectionTestUtils.setField(userProfileCreator, "idamStatusResolverMap", idamStatusMap);
        ReflectionTestUtils.setField(userProfileCreator, "sidamGetUri", "/api/v1/users/");

        UserProfile responseUserProfile = userProfileCreator.create(createUserProfileData);
        Mockito.verify(userProfileRepository, Mockito.times(1)).save(any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
        assertThat(responseUserProfile).isNotNull();
    }

    @Test
    public void should_register_when_idam_registration_conflicts_and_roles_null() {

        //List<String> roles = new ArrayList<>();
        //roles.add("pui-case-manager");
        Audit audit = Mockito.mock(Audit.class);
        ResponseEntity entity = Mockito.mock(ResponseEntity.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/users/" + UUID.randomUUID().toString());
        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CONFLICT, Optional.ofNullable(entity));

        Mockito.when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        Mockito.when(idamService.registerUser(any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        Mockito.when(entity.getHeaders()).thenReturn(headers);
        Mockito.when(idamRolesInfo.getRoles()).thenReturn(null);
        Mockito.when(idamRolesInfo.getResponseStatusCode()).thenReturn(HttpStatus.OK);
        Mockito.when(idamRolesInfo.getStatusMessage()).thenReturn("test error message");
        Mockito.when(idamRolesInfo.isSuccessFromIdam()).thenReturn(true);
        Mockito.when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        Mockito.when(idamRolesInfo.getForename()).thenReturn("fname");
        Mockito.when(idamRolesInfo.getSurname()).thenReturn("lastName");

        Mockito.when(idamService.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        Mockito.when(idamService.updateUserRoles(any(), Mockito.anyString())).thenReturn(idamRolesInfo);

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = createDecisionMap();
        ReflectionTestUtils.setField(userProfileCreator, "idamStatusResolverMap", idamStatusMap);
        ReflectionTestUtils.setField(userProfileCreator, "sidamGetUri", "/api/v1/users/");

        UserProfile responseUserProfile = userProfileCreator.create(createUserProfileData);
        Mockito.verify(userProfileRepository, Mockito.times(1)).save(any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
        assertThat(responseUserProfile).isNotNull();
    }

    @Test
    public void should_set_CreateUserProfileData_fields() {

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = createDecisionMap();

        ReflectionTestUtils.setField(userProfileCreator, "idamStatusResolverMap", idamStatusMap);
        Mockito.when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        Mockito.when(idamRolesInfo.getForename()).thenReturn("fname");
        Mockito.when(idamRolesInfo.getSurname()).thenReturn("lastName");
        Mockito.when(idamRolesInfo.getActive()).thenReturn(true);
        Mockito.when(idamRolesInfo.getPending()).thenReturn(false);
        Mockito.when(idamRolesInfo.getLocked()).thenReturn(false);

        CreateUserProfileData createUserProfileData = Mockito.mock(CreateUserProfileData.class);
        userProfileCreator.updateInputRequestWithLatestSidamUserInfo(createUserProfileData, idamRolesInfo);
        Mockito.verify(createUserProfileData, Mockito.times(1)).setEmail("any@emai");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setFirstName("fname");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setLastName("lastName");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setStatus(any(IdamStatus.class));

    }

    @Test
    public void should_set_CreateUserProfileData_fields_with_idamStatus_null() {

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = createDecisionMap();

        ReflectionTestUtils.setField(userProfileCreator, "idamStatusResolverMap", idamStatusMap);
        Mockito.when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        Mockito.when(idamRolesInfo.getForename()).thenReturn("fname");
        Mockito.when(idamRolesInfo.getSurname()).thenReturn("lastName");
        Mockito.when(idamRolesInfo.getActive()).thenReturn(true);
        Mockito.when(idamRolesInfo.getPending()).thenReturn(true);
        Mockito.when(idamRolesInfo.getLocked()).thenReturn(true);

        CreateUserProfileData createUserProfileData = Mockito.mock(CreateUserProfileData.class);
        userProfileCreator.updateInputRequestWithLatestSidamUserInfo(createUserProfileData, idamRolesInfo);
        Mockito.verify(createUserProfileData, Mockito.times(1)).setEmail("any@emai");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setFirstName("fname");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setLastName("lastName");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setStatus(null);

    }

    public Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag, boolean lockedFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        pendingMapWithRules.put("LOCKED", lockedFlag);
        return pendingMapWithRules;
    }

    public Map<Map<String, Boolean>, IdamStatus> createDecisionMap() {
        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false,true, false), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false,false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(true, false,true), IdamStatus.ACTIVE_AND_LOCKED);
        idamStatusMap.put(addRule(false,false,false), IdamStatus.SUSPENDED);
        idamStatusMap.put(addRule(false,false,true), IdamStatus.SUSPENDED_AND_LOCKED);
        return idamStatusMap;
    }

}
