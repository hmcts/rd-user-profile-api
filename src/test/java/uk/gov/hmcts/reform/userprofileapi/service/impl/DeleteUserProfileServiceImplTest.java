package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static uk.gov.hmcts.reform.userprofileapi.constants.TestConstants.COMMON_EMAIL_PATTERN;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    private UserProfileCreationData userProfileCreationData =
            CreateUserProfileTestDataBuilder.buildCreateUserProfileData();

    private UserProfile userProfile = new UserProfile(userProfileCreationData, HttpStatus.OK);

    @Mock
    private AuditService auditServiceMock;

    @Mock
    private IdamFeignClient idamClientMock;

    @InjectMocks
    private DeleteUserProfileServiceImpl sut;

    @Before
    public void setUp() {
        userProfile.setStatus(IdamStatus.PENDING);
        userProfile.setIdamId("1234");
        userProfile.setId((long) 1234);
    }

    @Test
    public void testDeleteUserProfile() {
        List<String> userIds = new ArrayList<>();
        userIds.add("1234");

        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        UserProfileDataRequest userProfilesDeletionData = new UserProfileDataRequest(userIds);

        UserProfilesDeletionResponse deletionResp = sut.delete(userProfilesDeletionData);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(userProfileRepositoryMock, times(1)).deleteAll(any());
        verify(auditServiceMock, times(1)).persistAudit(any());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testShouldThrowExceptionWhenEmptyUserProfileToDelete() {
        List<String> userIds = new ArrayList<String>();
        userIds.add("1234");

        UserProfileDataRequest userProfilesDeletionData = new UserProfileDataRequest(userIds);

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(any()));

        sut.delete(userProfilesDeletionData);

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(userProfileRepositoryMock, times(0)).deleteAll(any());
    }

    @Test
    public void testDeleteUserProfileByUserId() {
        Response responseMock = mock(Response.class);

        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(idamClientMock.deleteUser(userProfile.getIdamId())).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NO_CONTENT.value());
        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        UserProfilesDeletionResponse deletionResp = sut.deleteByUserId(userProfile.getIdamId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(userProfileRepositoryMock, times(1)).deleteAll(any());
        verify(auditServiceMock, times(1)).persistAudit(any());
    }

    @Test
    public void testDeleteUserProfileByUserId_WhenIdamReturns404() {
        Response responseMock = mock(Response.class);

        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(idamClientMock.deleteUser(userProfile.getIdamId())).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NOT_FOUND.value());
        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));

        UserProfilesDeletionResponse deletionResp = sut.deleteByUserId(userProfile.getIdamId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(userProfileRepositoryMock, times(1)).deleteAll(any());
        verify(auditServiceMock, times(1)).persistAudit(any());
    }

    @Test
    public void testDeleteUserProfileByUserId_WhenIdamReutrnsError() {
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("IDAM Delete request failed for userId: " + userProfile.getIdamId()
                + ". With following IDAM message: INTERNAL SERVER ERROR");
        deletionResponse.setStatusCode(BAD_REQUEST.value());

        Response responseMock = mock(Response.class);
        Response.Body bodyMock = mock(Response.Body.class);

        when(idamClientMock.deleteUser(userProfile.getIdamId())).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(BAD_REQUEST.value());
        when(responseMock.body()).thenReturn(bodyMock);
        when(bodyMock.toString()).thenReturn("INTERNAL SERVER ERROR");
        when(responseMock.body().toString()).thenReturn("INTERNAL SERVER ERROR");

        UserProfilesDeletionResponse deletionResp = sut.deleteByUserId(userProfile.getIdamId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).contains("IDAM Delete request failed for userId");

        verify(responseMock, times(3)).status();
    }

    @Test
    public void testDeleteUserProfileByUserId_WhenUserIsPendingInIdam() {
        String userId = UUID.randomUUID().toString();
        userProfile.setIdamId(userId);

        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("User deleted in UP with userId: " + userId
                + " but not in IDAM due to pending status");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        Response responseMock = mock(Response.class);
        Response.Body bodyMock = mock(Response.Body.class);

        when(idamClientMock.deleteUser(userId)).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(BAD_REQUEST.value());
        when(responseMock.body()).thenReturn(bodyMock);
        when(responseMock.body().toString()).thenReturn("The user cannot be modified as their state is 'pending'");
        when(userProfileRepositoryMock.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));

        UserProfilesDeletionResponse deletionResp = sut.deleteByUserId(userId);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(userId);
        verify(userProfileRepositoryMock, times(1)).deleteAll(any());
        verify(auditServiceMock, times(1)).persistAudit(any());
    }

    @Test
    public void testDeleteUserProfileByEmailPattern() {
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(userProfile);

        Response responseMock = mock(Response.class);

        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(idamClientMock.deleteUser(userProfile.getIdamId())).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NO_CONTENT.value());
        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(userProfileRepositoryMock
                .findByEmailIgnoreCaseContaining(COMMON_EMAIL_PATTERN)).thenReturn(userProfiles);

        UserProfilesDeletionResponse deletionResp = sut.deleteByEmailPattern(COMMON_EMAIL_PATTERN);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(userProfileRepositoryMock, times(1))
                .findByEmailIgnoreCaseContaining(any(String.class));
        verify(userProfileRepositoryMock, times(1)).deleteAll(any());
        verify(auditServiceMock, times(2)).persistAudit(any());
    }

    @Test
    public void testDeleteUserProfileByEmailPattern_WhenIdamReturns404() {
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(userProfile);

        Response responseMock = mock(Response.class);

        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(idamClientMock.deleteUser(userProfile.getIdamId())).thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NOT_FOUND.value());
        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.ofNullable(userProfile));
        when(userProfileRepositoryMock
                .findByEmailIgnoreCaseContaining(COMMON_EMAIL_PATTERN)).thenReturn(userProfiles);

        UserProfilesDeletionResponse deletionResp = sut.deleteByEmailPattern(COMMON_EMAIL_PATTERN);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(userProfileRepositoryMock, times(1))
                .findByEmailIgnoreCaseContaining(any(String.class));
        verify(userProfileRepositoryMock, times(1)).deleteAll(any());
        verify(auditServiceMock, times(2)).persistAudit(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateUserAfterIdamDeleteWhenStatusIs204() {
        Optional<UserProfile> userProfile = mock(Optional.class);
        String userId = UUID.randomUUID().toString();

        when(userProfile.isPresent()).thenReturn(false);

        UserProfilesDeletionResponse deletionResponse =
                sut.validateUserAfterIdamDelete(userProfile, userId, 204);

        assertThat(deletionResponse.getStatusCode()).isEqualTo(204);
        assertThat(deletionResponse.getMessage())
                .isEqualTo("User deleted in IDAM but was not present in UP with userId: " + userId);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateUserAfterIdamDeleteWhenStatusIsNot204() {
        Optional<UserProfile> userProfile = mock(Optional.class);
        String userId = UUID.randomUUID().toString();

        when(userProfile.isPresent()).thenReturn(false);

        UserProfilesDeletionResponse deletionResponse =
                sut.validateUserAfterIdamDelete(userProfile, userId, 404);

        assertThat(deletionResponse.getStatusCode()).isEqualTo(404);
        assertThat(deletionResponse.getMessage())
                .isEqualTo("User was not present in IDAM or UP with userId: " + userId);
    }
}
