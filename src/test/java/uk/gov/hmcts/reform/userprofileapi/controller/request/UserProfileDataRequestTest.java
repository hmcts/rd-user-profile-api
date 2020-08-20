package uk.gov.hmcts.reform.userprofileapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class UserProfileDataRequestTest {

    private List<String> userIds;

    @Before
    public void setUp() {
        userIds = new ArrayList<>();
    }

    @Test
    public void test_UserProfileRequest() {
        userIds.add(UUID.randomUUID().toString());
        UserProfileDataRequest getUserProfileRequest = new UserProfileDataRequest(userIds);

        assertThat(getUserProfileRequest.getUserIds().size()).isEqualTo(1);
    }

    @Test
    public void test_UserProfileRequestWithNullIds() {
        userIds.add(UUID.randomUUID().toString());
        UserProfileDataRequest getUserProfileRequest = new UserProfileDataRequest(null);

        assertThat(getUserProfileRequest.getUserIds()).isNull();

        getUserProfileRequest.setUserIds(userIds);

        assertThat(getUserProfileRequest.getUserIds()).isNotNull();
        assertThat(getUserProfileRequest.getUserIds().size()).isEqualTo(1);

        getUserProfileRequest.setUserIds(null);
        
        assertThat(getUserProfileRequest.getUserIds()).isNull();
    }
}
