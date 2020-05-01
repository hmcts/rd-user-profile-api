package uk.gov.hmcts.reform.userprofileapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDataRequest {

    private List<String> userIds;

    @JsonCreator
    public UserProfileDataRequest(@JsonProperty(value = "userId") List<String> userIds) {
        this.userIds = userIds;
    }
}

