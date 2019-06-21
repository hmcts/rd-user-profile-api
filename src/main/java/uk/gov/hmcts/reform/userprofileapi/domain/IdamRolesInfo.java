package uk.gov.hmcts.reform.userprofileapi.domain;

import java.util.List;
import lombok.Getter;

@Getter
public class IdamRolesInfo {

    private List<String> roles;

    public IdamRolesInfo(List<String> roles) {
        this.roles = roles;
    }
}