package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
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

        Mockito.when(idamService.registerUser(Mockito.any())).thenReturn(idamRegistrationInfo);
        Mockito.when(userProfileRepository.findByEmail(Mockito.any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(Mockito.any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileCreator.create(createUserProfileData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = Mockito.inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService).registerUser(Mockito.any(IdamRegisterUserRequest.class));
        inOrder.verify(userProfileRepository).save(Mockito.any(UserProfile.class));

        Mockito.verify(auditRepository, Mockito.times(1)).save(Mockito.any(Audit.class));

    }

    @Test
    public void should_throw_IdamServiceException_when_user_already_exist() {

        Audit audit = Mockito.mock(Audit.class);
        Mockito.when(userProfileRepository.findByEmail(Mockito.any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        assertThatThrownBy(() -> userProfileCreator.create(createUserProfileData)).isExactlyInstanceOf(IdamServiceException.class);
        Mockito.verify(userProfileRepository, Mockito.times(0)).save(Mockito.any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(Mockito.any(Audit.class));
    }

    @Test
    public void should_throw_IdamServiceException_when_idam_registration_fail() {

        Audit audit = Mockito.mock(Audit.class);
        idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.BAD_REQUEST);
        Mockito.when(userProfileRepository.findByEmail(Mockito.any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(idamService.registerUser(Mockito.any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        assertThatThrownBy(() -> userProfileCreator.create(createUserProfileData)).isExactlyInstanceOf(IdamServiceException.class);
        Mockito.verify(userProfileRepository, Mockito.times(0)).save(Mockito.any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(Mockito.any(Audit.class));
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

        Mockito.when(userProfileRepository.findByEmail(Mockito.any(String.class))).thenReturn(Optional.ofNullable(null));
        Mockito.when(userProfileRepository.save(Mockito.any(UserProfile.class))).thenReturn(userProfile);
        Mockito.when(idamService.registerUser(Mockito.any(IdamRegisterUserRequest.class))).thenReturn(idamRegistrationInfo);
        //when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        Mockito.when(entity.getHeaders()).thenReturn(headers);
        Mockito.when(idamRolesInfo.getRoles()).thenReturn(roles);
        Mockito.when(idamRolesInfo.getResponseStatusCode()).thenReturn(HttpStatus.OK);
        Mockito.when(idamRolesInfo.getStatusMessage()).thenReturn("test error message");
        Mockito.when(idamRolesInfo.isSuccessFromIdam()).thenReturn(true);
        Mockito.when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        Mockito.when(idamRolesInfo.getForename()).thenReturn("fname");
        Mockito.when(idamRolesInfo.getSurname()).thenReturn("lastName");

        Mockito.when(idamService.fetchUserById(Mockito.any(String.class))).thenReturn(idamRolesInfo);
        Mockito.when(idamService.updateUserRoles(Mockito.any(), Mockito.anyString())).thenReturn(idamRolesInfo);

        ReflectionTestUtils.setField(userProfileCreator, "sidamGetUri", "/api/v1/users/");

        UserProfile responseUserProfile = userProfileCreator.create(createUserProfileData);
        Mockito.verify(userProfileRepository, Mockito.times(1)).save(Mockito.any(UserProfile.class));
        Mockito.verify(auditRepository, Mockito.times(1)).save(Mockito.any(Audit.class));
    }

    @Test
    public void should_set_CreateUSerProfileDate_fileds() {

        Mockito.when(idamRolesInfo.getEmail()).thenReturn("any@emai");
        Mockito.when(idamRolesInfo.getForename()).thenReturn("fname");
        Mockito.when(idamRolesInfo.getSurname()).thenReturn("lastName");
        CreateUserProfileData createUserProfileData = Mockito.mock(CreateUserProfileData.class);
        userProfileCreator.updateInputRequestWithLatestSidamUserInfo(createUserProfileData, idamRolesInfo);
        Mockito.verify(createUserProfileData, Mockito.times(1)).setEmail("any@emai");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setFirstName("fname");
        Mockito.verify(createUserProfileData, Mockito.times(1)).setLastName("lastName");

    }

}
