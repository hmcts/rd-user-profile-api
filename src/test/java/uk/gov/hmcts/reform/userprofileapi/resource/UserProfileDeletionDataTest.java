package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class UserProfileDeletionDataTest {

    @Test
    public void should_add_userIds_when_add_to_setter() {
        UserProfilesDeletionData userProfilesDeletionData = new UserProfilesDeletionData();
        List<String> userIds = new ArrayList<String>();
        userIds.add("12345");
        userProfilesDeletionData.setUserIds(userIds);
        assertThat(userProfilesDeletionData.getUserIds().size()).isEqualTo(1);
        UserProfilesDeletionData userProfilesDeletionData1 = new UserProfilesDeletionData(userIds);
        assertThat(userProfilesDeletionData1.getUserIds().size()).isEqualTo(1);
    }

}