package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class UpdatedUserDetails {
    protected String forename;
    protected String surname;
    protected Boolean active;

    public UpdatedUserDetails(String foreName, String surname, Boolean active) {
        if (StringUtils.isNotEmpty(foreName)) {
            this.forename = foreName;
        }
        if (StringUtils.isNotEmpty(surname)) {
            this.surname = surname;
        }
        if (null != active) {
            this.active = active;
        }
    }
}


