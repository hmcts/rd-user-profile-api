package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam.IdamService;

@RunWith(MockitoJUnitRunner.class)
public class IdamServiceTest {

    @Test
    public void should_return_idam_id_successfully() {

        IdamService idamService = new IdamService();
        CreateUserProfileData data = mock(CreateUserProfileData.class);
        String idamId = idamService.registerUser(data);

        assertThat(idamId).isNotEmpty();

    }

}
