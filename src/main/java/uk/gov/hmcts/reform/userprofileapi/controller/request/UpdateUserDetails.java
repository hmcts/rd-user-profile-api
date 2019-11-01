package uk.gov.hmcts.reform.userprofileapi.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

@Getter
@Setter
public class UpdateUserDetails {

    protected String forename;
    protected String surname;
    protected Boolean active;


    public UpdateUserDetails(String forename, String surname, Boolean active) {

        if (!StringUtils.isEmpty(forename)) {
            this.forename = forename;
        }
        if (!StringUtils.isEmpty(surname)) {
            this.surname = surname;
        }
        if (active != null) {
            this.active = active;
        }
    }
}
