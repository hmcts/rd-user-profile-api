package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;

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

    @Mock
    private ValidationHelperService validationHelperService;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);

    private UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();

    @Spy
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo.getIdamRegistrationResponse());

    private IdamRolesInfo idamRolesInfo = mock(IdamRolesInfo.class);

    @Test
    public void should_create_user_profile_successfully() {

        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileCreator.create(userProfileCreationData);

        assertThat(response).isEqualToIgnoringGivenFields(userProfile, "idamId");

        InOrder inOrder = inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(auditRepository, times(1)).save(any(Audit.class));

    }

    @Test
    public void should_create_user_profile_successfully_profileData_has_status_populated() {

        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        userProfileCreationData.setStatus(IdamStatus.PENDING);
        UserProfile response = userProfileCreator.create(userProfileCreationData);

        assertThat(response).isEqualToIgnoringGivenFields(userProfile, "idamId");

        InOrder inOrder = inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository, times(1)).save(any(UserProfile.class));

        verify(auditRepository, times(1)).save(any(Audit.class));
        assertThat(response.getIdamId()).isNotNull();
        assertThat(response.getStatus()).isNotNull();

    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtime_when_create_user_profile_fails_to_save() {

        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        when(userProfileRepository.save(any(UserProfile.class))).thenThrow(new RuntimeException());

        UserProfile response = userProfileCreator.create(userProfileCreationData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository, times(1)).save(any(UserProfile.class));

        verify(auditRepository, times(1)).save(any(Audit.class));

    }

    @Test
    public void should_throw_IdamServiceException_when_user_already_exist() {

        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        assertThatThrownBy(() -> userProfileCreator.create(userProfileCreationData)).isExactlyInstanceOf(IdamServiceException.class);
        verify(userProfileRepository, times(0)).save(any(UserProfile.class));
        verify(auditRepository, times(1)).save(any(Audit.class));
    }

    @Test
    public void should_throw_IdamServiceException_when_idam_registration_fail() {

        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.BAD_REQUEST);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        when(idamService.registerUser(any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        assertThatThrownBy(() -> userProfileCreator.create(userProfileCreationData)).isExactlyInstanceOf(IdamServiceException.class);
        verify(userProfileRepository, times(0)).save(any(UserProfile.class));
        verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        verify(auditRepository, times(1)).save(any(Audit.class));
    }

    @Test
    public void should_register_when_idam_registration_conflicts() {

        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manager");
        ResponseEntity entity = mock(ResponseEntity.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/users/" + UUID.randomUUID().toString());
        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CONFLICT, Optional.ofNullable(entity));

        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(idamService.registerUser(any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        when(entity.getHeaders()).thenReturn(headers);
        when(idamRolesInfo.getRoles()).thenReturn(roles);
        when(idamRolesInfo.getResponseStatusCode()).thenReturn(HttpStatus.OK);
        when(idamRolesInfo.getStatusMessage()).thenReturn("test error message");
        when(idamRolesInfo.isSuccessFromIdam()).thenReturn(true);
        when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        when(idamRolesInfo.getForename()).thenReturn("fname");
        when(idamRolesInfo.getSurname()).thenReturn("lastName");

        when(idamService.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        when(idamService.addUserRoles(any(), anyString())).thenReturn(idamRolesInfo);

        createDecisionMap();
        ReflectionTestUtils.setField(userProfileCreator, "sidamGetUri", "/api/v1/users/");

        UserProfile responseUserProfile = userProfileCreator.create(userProfileCreationData);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(auditRepository, times(1)).save(any(Audit.class));
        assertThat(responseUserProfile).isNotNull();
    }

    @Test
    public void should_register_when_idam_registration_conflicts_and_roles_null() {

        ResponseEntity entity = mock(ResponseEntity.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/users/" + UUID.randomUUID().toString());
        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CONFLICT, Optional.ofNullable(entity));

        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(null));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(idamService.registerUser(any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        when(entity.getHeaders()).thenReturn(headers);
        when(idamRolesInfo.getRoles()).thenReturn(null);
        when(idamRolesInfo.getResponseStatusCode()).thenReturn(HttpStatus.OK);
        when(idamRolesInfo.getStatusMessage()).thenReturn("test error message");
        when(idamRolesInfo.isSuccessFromIdam()).thenReturn(true);
        when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        when(idamRolesInfo.getForename()).thenReturn("fname");
        when(idamRolesInfo.getSurname()).thenReturn("lastName");

        when(idamService.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        when(idamService.addUserRoles(any(), anyString())).thenReturn(idamRolesInfo);

        createDecisionMap();
        ReflectionTestUtils.setField(userProfileCreator, "sidamGetUri", "/api/v1/users/");

        UserProfile responseUserProfile = userProfileCreator.create(userProfileCreationData);
        verify(userProfileRepository, times(1)).findByEmail(any());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(auditRepository, times(1)).save(any(Audit.class));
        assertThat(responseUserProfile).isNotNull();
    }

    @Test
    public void should_set_CreateUserProfileData_fields() {

        createDecisionMap();

        when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        when(idamRolesInfo.getForename()).thenReturn("fname");
        when(idamRolesInfo.getSurname()).thenReturn("lastName");
        when(idamRolesInfo.getActive()).thenReturn(true);
        when(idamRolesInfo.getPending()).thenReturn(false);


        UserProfileCreationData userProfileCreationData = mock(UserProfileCreationData.class);
        userProfileCreator.updateInputRequestWithLatestSidamUserInfo(userProfileCreationData, idamRolesInfo);
        verify(userProfileCreationData, times(1)).setEmail("any@emai");
        verify(userProfileCreationData, times(1)).setFirstName("fname");
        verify(userProfileCreationData, times(1)).setLastName("lastName");
        verify(userProfileCreationData, times(1)).setStatus(any(IdamStatus.class));

    }

    @Test
    public void should_set_CreateUserProfileData_fields_with_idamStatus_null() {

        createDecisionMap();

        when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        when(idamRolesInfo.getForename()).thenReturn("fname");
        when(idamRolesInfo.getSurname()).thenReturn("lastName");
        when(idamRolesInfo.getActive()).thenReturn(true);
        when(idamRolesInfo.getPending()).thenReturn(true);

        UserProfileCreationData userProfileCreationData = mock(UserProfileCreationData.class);
        userProfileCreator.updateInputRequestWithLatestSidamUserInfo(userProfileCreationData, idamRolesInfo);
        verify(userProfileCreationData, times(1)).setEmail("any@emai");
        verify(userProfileCreationData, times(1)).setFirstName("fname");
        verify(userProfileCreationData, times(1)).setLastName("lastName");
        verify(userProfileCreationData, times(1)).setStatus(any());

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
        idamStatusMap.put(addRule(false, true, false), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false, false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(false, false, false), IdamStatus.SUSPENDED);
        return idamStatusMap;
    }

    @Test
    public void test_consolidateRolesFromXuiAndIdam_1() {

        List<String> xuiRolesList = new ArrayList<>();
        xuiRolesList.add("pui-case-manager");
        xuiRolesList.add("pui-user-manager");
        xuiRolesList.add("prd-admin");

        List<String> idamRolesList = new ArrayList<>();
        idamRolesList.add("pui-case-manager");
        idamRolesList.add("pui-user-manager");

        UserProfileCreationData userProfileCreationDataMock = mock(UserProfileCreationData.class);
        when(userProfileCreationDataMock.getRoles()).thenReturn(xuiRolesList);

        IdamRolesInfo idamRolesInfoMock = mock(IdamRolesInfo.class);
        when(idamRolesInfoMock.getRoles()).thenReturn(idamRolesList);

        Set<String> rolesToUpdate = userProfileCreator.consolidateRolesFromXuiAndIdam(userProfileCreationDataMock, idamRolesInfoMock);

        assertThat(rolesToUpdate.size()).isEqualTo(1);
        assertThat(rolesToUpdate).contains("prd-admin");
    }

    @Test
    public void test_consolidateRolesFromXuiAndIdam_2() {

        List<String> xuiRolesList = new ArrayList<>();
        xuiRolesList.add("pui-case-manager");
        xuiRolesList.add("pui-user-manager");
        xuiRolesList.add("pui-user-manager");

        List<String> idamRolesList = new ArrayList<>();
        idamRolesList.add("prd-admin");

        UserProfileCreationData userProfileCreationDataMock = mock(UserProfileCreationData.class);
        when(userProfileCreationDataMock.getRoles()).thenReturn(xuiRolesList);

        IdamRolesInfo idamRolesInfoMock = mock(IdamRolesInfo.class);
        when(idamRolesInfoMock.getRoles()).thenReturn(idamRolesList);

        Set<String> rolesToUpdate = userProfileCreator.consolidateRolesFromXuiAndIdam(userProfileCreationDataMock, idamRolesInfoMock);

        assertThat(rolesToUpdate.size()).isEqualTo(2);
        assertThat(rolesToUpdate)
                .contains("pui-case-manager")
                .contains("pui-user-manager");
    }

    @Test
    public void test_consolidateRolesFromXuiAndIdam_3() {

        List<String> xuiRolesList = new ArrayList<>();
        xuiRolesList.add("pui-case-manager");
        xuiRolesList.add("pui-user-manager");

        List<String> idamRolesList = new ArrayList<>();
        idamRolesList.add("pui-case-manager");
        idamRolesList.add("pui-user-manager");

        UserProfileCreationData userProfileCreationDataMock = mock(UserProfileCreationData.class);
        when(userProfileCreationDataMock.getRoles()).thenReturn(xuiRolesList);

        IdamRolesInfo idamRolesInfoMock = mock(IdamRolesInfo.class);
        when(idamRolesInfoMock.getRoles()).thenReturn(idamRolesList);

        Set<String> rolesToUpdate = userProfileCreator.consolidateRolesFromXuiAndIdam(userProfileCreationDataMock, idamRolesInfoMock);

        assertThat(rolesToUpdate.size()).isZero();
    }

    @Test
    public void should_reinvite_user_successfully() {
        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(validationHelperService.validateReInvitedUser(any())).thenReturn(userProfile);

        UserProfile response = userProfileCreator.reInviteUser(userProfileCreationData);

        assertThat(response).isEqualToIgnoringGivenFields(userProfile, "idamId");
        assertThat(response.getIdamRegistrationResponse()).isEqualTo(HttpStatus.ACCEPTED.value());

        InOrder inOrder = inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(auditRepository, times(1)).save(any(Audit.class));
        verify(userProfile, times(1)).setIdamRegistrationResponse(anyInt());
    }

    @Test
    public void should_not_reinvite_user_when_sidam_returns_409() {

        ReflectionTestUtils.setField(userProfileCreator, "syncInterval", "60");
        IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CONFLICT);
        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(validationHelperService.validateReInvitedUser(any())).thenReturn(userProfile);


        final Throwable raisedException = catchThrowable(() -> userProfileCreator.reInviteUser(userProfileCreationData));

        assertThat(raisedException).isInstanceOf(IdamServiceException.class)
                .hasMessageContaining("7 : Resend invite failed as user is already active. Wait for 60 minutes for the system to refresh.");

        InOrder inOrder = inOrder(idamService, auditRepository);
        inOrder.verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        verify(userProfileRepository, times(0)).save(any(UserProfile.class));
        inOrder.verify(auditRepository, times(1)).save(any(Audit.class));

    }

    @Test
    public void should_not_reinvite_user_when_sidam_returns_400() {

        IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.BAD_REQUEST);
        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(validationHelperService.validateReInvitedUser(any())).thenReturn(userProfile);


        final Throwable raisedException = catchThrowable(() -> userProfileCreator.reInviteUser(userProfileCreationData));

        assertThat(raisedException).isInstanceOf(IdamServiceException.class)
                .hasMessageContaining("13 Required parameters or one of request field is missing or invalid");

        InOrder inOrder = inOrder(idamService, auditRepository);
        inOrder.verify(idamService, times(1)).registerUser(any(IdamRegisterUserRequest.class));
        verify(userProfileRepository, times(0)).save(any(UserProfile.class));
        inOrder.verify(auditRepository, times(1)).save(any(Audit.class));

    }

    @Test
    public void should_not_reinvite_user_when_validation_fails_returns_400() {

        userProfile.setStatus(IdamStatus.ACTIVE);
        when(userProfileRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(validationHelperService.validateReInvitedUser(any())).thenThrow(InvalidRequest.class);

        final Throwable raisedException = catchThrowable(() -> userProfileCreator.reInviteUser(userProfileCreationData));
        assertThat(raisedException).isInstanceOf(InvalidRequest.class);

        verify(idamService, times(0)).registerUser(any(IdamRegisterUserRequest.class));
        verify(userProfileRepository, times(0)).save(any(UserProfile.class));
    }

    @Test
    public void test_createIdamRolesRequest() {
        Set<String> rolesToUpdate = new HashSet<>();
        rolesToUpdate.add("pui-user-manager");

        Set<Map<String, String>> roles = userProfileCreator.createIdamRolesRequest(rolesToUpdate);

        assertThat(roles).isNotEmpty();
    }
}