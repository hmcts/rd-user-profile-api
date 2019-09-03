package uk.gov.hmcts.reform.userprofileapi.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class RoleName extends UpdateUserProfileData{

    private String name;

    public RoleName(String name) {
        this.name = name;
    }


}
