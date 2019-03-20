package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam.IdamService;

@RunWith(MockitoJUnitRunner.class)
public class IdamServiceTest {

    @Test
    public void should_return_idam_id_successfully() {

        IdamService idamService = new IdamService();
        CreateUserProfileData data = new CreateUserProfileData();
        String idamId = idamService.registerUser(data);

        assertThat(idamId).isNotEmpty();

    }

}
