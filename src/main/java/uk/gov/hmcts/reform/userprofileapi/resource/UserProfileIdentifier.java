package uk.gov.hmcts.reform.userprofileapi.resource;

import java.util.List;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;

public class UserProfileIdentifier implements RequestData {

    private IdentifierName name;
    private String value;
    private List<String> values;

    public UserProfileIdentifier(IdentifierName name, String value) {
        this.name = name;
        this.value = value;
    }

    public UserProfileIdentifier(IdentifierName name, List<String> value) {
        this.name = name;
        this.values = value;
    }

    public IdentifierName getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<String> getValues() {
        return values;
    }

}
