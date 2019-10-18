package uk.gov.hmcts.reform.userprofileapi.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

@Getter
@Setter
public class UpdateUserDetails {

    protected String forename;
    protected String surname;
    protected Boolean active;


    public UpdateUserDetails(String foreName, String surname, Boolean active) {
        if (!StringUtils.isEmpty(foreName)) {
            this.forename = foreName;
        }
        if (!StringUtils.isEmpty(surname)) {
            this.surname = surname;
        }
        if (active != null) {
            this.active = active;
        }
    }
}