package uk.gov.hmcts.reform.userprofileapi.clients;

import static org.assertj.core.api.Assertions.assertThat;

import feign.RetryableException;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.service.impl.IdamService;

@RunWith(MockitoJUnitRunner.class)
public class IdamServiceTest {

    @Ignore
    @Test
    public void should_return_idam_id_successfully() {

        IdamService idamService = new IdamService();
        CreateUserProfileData data = Mockito.mock(CreateUserProfileData.class);
        IdamRegistrationInfo idamId = idamService.registerUser(data);

        assertThat(idamId.getIdamRegistrationResponse()).isNotNull();
        assertThat(idamId.getIdamRegistrationResponse().value())
                .isEqualTo(HttpStatus.ACCEPTED.value());

    }

    @Test
    public void should_return_HttpStatus_for_idam_connectivity_fails() {

        IdamService idamService = new IdamService();

        HttpStatus status = idamService.gethttpStatusFromIdam(new RetryableException("test Exception", new Throwable(), new Date()));
        assertThat(status).isNotNull().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);



    }

}
