package uk.gov.hmcts.reform.userprofileapi.clients;

public class UserProfileIdentifier implements RequestData {

    private IdentifierName name;
    private String value;

    public UserProfileIdentifier(IdentifierName name, String value) {
        this.name = name;
        this.value = value;
    }

    public IdentifierName getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
