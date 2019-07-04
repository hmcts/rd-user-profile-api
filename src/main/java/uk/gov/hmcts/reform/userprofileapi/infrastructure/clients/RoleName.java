package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class RoleName {
    public RoleName(String name) {
        this.name = name;
    }

    private String name;
}
