package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetUserProfilesRequest {

    private List<String> userIds;

    @JsonCreator
    public GetUserProfilesRequest(@JsonProperty(value = "userIds") List<String> userIds) {
        this.userIds = userIds;
    }
}

