package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileMatchResponse {
    private final List<Profile> matchedProfiles;
    private final List<String> unavailableUserIds;

    @Getter
    @AllArgsConstructor
    public static class Profile {
        private final String userIdentifier;
        private final String firstName;
        private final String lastName;
        private final String email;
    }
}
