package uk.gov.hmcts.reform.userprofileapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Data;

@Data
public class UserProfilesRequest {

    private List<String> userIds;

    @JsonCreator
    public UserProfilesRequest(@JsonProperty(value = "userId") List<String> userIds) {
        this.userIds = userIds;
    }
}

