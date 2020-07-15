package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;

public class AuditTest {

    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

    @Test
    public void test_populate_all_fields() {
        Audit audit = new Audit(200, "testErrorMessage", ResponseSource.API, userProfile);

        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(200);
        assertThat(audit.getStatusMessage()).isEqualTo("testErrorMessage");
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile()).isEqualTo(userProfile);
        assertThat(audit.getId()).isNull();
        assertThat(audit.getAuditTs()).isNull();
    }

}