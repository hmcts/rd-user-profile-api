package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;

@ExtendWith(MockitoExtension.class)
public class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepositoryMock;

    @InjectMocks
    private AuditServiceImpl sut;

    @Mock
    private HttpStatus httpStatusMock;

    @Mock
    private ResponseSource responseSourceMock;

    @Test
    public void test_PersistAudit() {
        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        sut.persistAudit(httpStatusMock, userProfileMock, responseSourceMock);

        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }

    @Test
    public void test_PersistAuditTwoArgs() {
        sut.persistAudit(httpStatusMock, responseSourceMock);

        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }

    @Test
    public void testPersistAuditForDeleteUserProfiles() {
        UserProfilesDeletionResponse userProfilesDeletionResponse =
            new UserProfilesDeletionResponse(204,"successfully deleted");
        sut.persistAudit(userProfilesDeletionResponse);
        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }
}