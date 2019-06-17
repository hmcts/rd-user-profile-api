package uk.gov.hmcts.reform.userprofileapi.domain;

import lombok.Getter;
import java.util.List;

@Getter
public class IdamRolesInfo {

    private List<String> roles;

    public IdamRolesInfo(List<String> roles){
        this.roles =roles;
    }
}