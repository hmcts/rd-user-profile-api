package uk.gov.hmcts.reform.userprofileapi.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserProfilesDeletionData implements RequestData {

    @NotEmpty(message = "at least one userIds is required")
    @JsonProperty
    private List<String> userIds;

    public UserProfilesDeletionData(List<String> userIds) {
        this.userIds = userIds;

    }


}
