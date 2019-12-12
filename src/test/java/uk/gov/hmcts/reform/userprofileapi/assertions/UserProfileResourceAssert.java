package uk.gov.hmcts.reform.userprofileapi.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileResourceAssert extends AbstractAssert<UserProfileResourceAssert, UserProfileCreationResponse> {

    public UserProfileResourceAssert(UserProfileCreationResponse actual) {
        super(actual, UserProfileCreationResponse.class);
    }

    public static UserProfileResourceAssert assertThat(UserProfileCreationResponse actual) {
        return new UserProfileResourceAssert(actual);
    }

    public UserProfileResourceAssert isSameAs(UserProfile userProfile) {
        isNotNull();
        Assertions.assertThat(userProfile.getId().toString()).isEqualTo(actual.getIdamId());

        return this;
    }
}
