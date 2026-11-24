package uk.gov.hmcts.reform.userprofileapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

import java.util.List;

@Getter
@Setter
public class UserProfileDataRequest implements RequestData {

    private List<String> userIds;

    @JsonCreator
    public UserProfileDataRequest(List<String> userIds) {
        this.userIds = userIds;
    }
}

