package uk.gov.hmcts.reform.userprofileapi.controller.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileDataRequestTest {

    private List<String> userIds;

    @BeforeEach
    public void setUp() {
        userIds = new ArrayList<>();
    }

    @Test
    void test_UserProfileRequest() {
        userIds.add(UUID.randomUUID().toString());
        UserProfileDataRequest getUserProfileRequest = new UserProfileDataRequest(userIds);

        assertThat(getUserProfileRequest.getUserIds().size()).isEqualTo(1);
    }

    @Test
    void test_UserProfileRequestWithNullIds() {
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
