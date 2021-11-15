package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepositoryMock;

    @InjectMocks
    private AuditService sut = new AuditServiceImpl();

    private HttpStatus httpStatusMock;

    private ResponseSource responseSourceMock;

    @BeforeEach
    public void setUp() {
        httpStatusMock = Mockito.mock(HttpStatus.class);
        responseSourceMock = Mockito.mock(ResponseSource.class);
    }

    @Test
    void testPersistAudit() {
        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        sut.persistAudit(httpStatusMock, userProfileMock, responseSourceMock);

        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }

    @Test
    void test_PersistAuditTwoArgs() {
        sut.persistAudit(httpStatusMock, responseSourceMock);

        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }

    @Test
    void testPersistAuditForDeleteUserProfiles() {
        UserProfilesDeletionResponse userProfilesDeletionResponse =
                new UserProfilesDeletionResponse(204, "successfully deleted");
        sut.persistAudit(userProfilesDeletionResponse);
        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }
}
