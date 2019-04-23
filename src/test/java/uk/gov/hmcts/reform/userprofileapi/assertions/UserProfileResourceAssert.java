package uk.gov.hmcts.reform.userprofileapi.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

public class UserProfileResourceAssert extends AbstractAssert<UserProfileResourceAssert, UserProfileResource> {

    public UserProfileResourceAssert(UserProfileResource actual) {
        super(actual, UserProfileResource.class);
    }

    public static UserProfileResourceAssert assertThat(UserProfileResource actual) {
        return new UserProfileResourceAssert(actual);
    }

    public UserProfileResourceAssert isSameAs(UserProfile userProfile) {
        isNotNull();
        Assertions.assertThat(userProfile.getId().toString()).isEqualTo(actual.getId());

        return this;
    }
}
