package uk.gov.hmcts.reform.userprofileapi.domain;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserDetails;

public interface UserDetailsProvider {

    UserDetails getUserDetails();
}
