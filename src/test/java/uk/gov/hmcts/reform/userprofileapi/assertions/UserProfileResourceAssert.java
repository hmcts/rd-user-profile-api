package uk.gov.hmcts.reform.userprofileapi.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileResponse;

public class UserProfileResourceAssert extends AbstractAssert<UserProfileResourceAssert, CreateUserProfileResponse> {

    public UserProfileResourceAssert(CreateUserProfileResponse actual) {
        super(actual, CreateUserProfileResponse.class);
    }

    public static UserProfileResourceAssert assertThat(CreateUserProfileResponse actual) {
        return new UserProfileResourceAssert(actual);
    }

    public UserProfileResourceAssert isSameAs(UserProfile userProfile) {
        isNotNull();
        Assertions.assertThat(userProfile.getId().toString()).isEqualTo(actual.getIdamId());

        return this;
    }
}
