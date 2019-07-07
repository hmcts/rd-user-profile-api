package uk.gov.hmcts.reform.userprofileapi.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserProfilesRequest {

    private List<String> userIds;

    @JsonCreator
    public GetUserProfilesRequest(@JsonProperty(value = "userIds") List<String> userIds) {
        this.userIds = userIds;
    }
}

