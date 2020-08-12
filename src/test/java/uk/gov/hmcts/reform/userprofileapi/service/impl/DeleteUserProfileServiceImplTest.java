package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;


@RunWith(MockitoJUnitRunner.class)
public class DeleteUserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    private UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();

    private UserProfile userProfile = new UserProfile(userProfileCreationData, HttpStatus.OK);

    @Mock
    private AuditService auditServiceMock;


    @InjectMocks
    private DeleteUserProfileServiceImpl sut;

    @Before
    public void setUp() {
        userProfile.setStatus(IdamStatus.PENDING);
        userProfile.setIdamId("1234");
        userProfile.setId((long)1234);
    }

    @Test
    public void testDeleteUserProfile() throws Exception {


        List<String> userIds = new ArrayList<String>();
        userIds.add("1234");
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        HttpStatus status = HttpStatus.NO_CONTENT;
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(status.value());
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


}
