package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamService;
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class IdamServiceTest {

    @Test
    public void should_return_idam_id_successfully() {

        IdamService idamService = new IdamService();
        CreateUserProfileData data = Mockito.mock(CreateUserProfileData.class);
        IdamRegistrationInfo idamId = idamService.registerUser(data);

        assertThat(idamId.getIdamRegistrationResponse()).isNotNull();
        assertThat(idamId.getIdamRegistrationResponse().value())
            .isEqualTo(HttpStatus.ACCEPTED.value());

    }

}
