package uk.gov.hmcts.reform.userprofileapi.repository;

public interface UserProfileMatch {
    String getUserIdentifier();

    String getFirstName();

    String getLastName();

    String getEmail();

    boolean getNameMatches();
}
