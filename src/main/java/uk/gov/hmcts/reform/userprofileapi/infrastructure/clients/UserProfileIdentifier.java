package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import javafx.util.Pair;

public class UserProfileIdentifier {

    private Pair<String, String> identifier;

    public UserProfileIdentifier(Pair<String, String> identifier) {
        this.identifier = identifier;
    }

    public Pair<String, String> getIdentifier() {
        return identifier;
    }
}
