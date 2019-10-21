package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepositoryMock;

    @InjectMocks
    private AuditService sut = new AuditServiceImpl();

    private HttpStatus httpStatusMock;

    private ResponseSource responseSourceMock;

    @Before
    public void setUp() {
        httpStatusMock = Mockito.mock(HttpStatus.class);
        responseSourceMock = Mockito.mock(ResponseSource.class);
    }

    @Test
    public void testPersistAudit() {
        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        sut.persistAudit(httpStatusMock, userProfileMock, responseSourceMock);

        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }

    @Test
    public void testPersistAuditTwoArgs() {
        sut.persistAudit(httpStatusMock, responseSourceMock);

        verify(auditRepositoryMock, times(1)).save(any(Audit.class));
    }
}