package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;



public class AuditTest {

    UserProfile userProfile = mock(UserProfile.class);

    @Test
    public void should_populate_all_fields() {

        Audit audit = new Audit(200, "testErrorMessage", ResponseSource.API, userProfile);

        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(200);
        assertThat(audit.getStatusMessage()).isEqualTo("testErrorMessage");
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile()).isEqualTo(userProfile);
        assertThat(audit.getId()).isNull();
        assertThat(audit.getAuditTs()).isNull();
    }

}
