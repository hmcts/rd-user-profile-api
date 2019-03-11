package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

public class UserProfileIdentifier {

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
