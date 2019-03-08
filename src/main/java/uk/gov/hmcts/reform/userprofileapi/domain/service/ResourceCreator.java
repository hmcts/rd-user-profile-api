package uk.gov.hmcts.reform.userprofileapi.domain.service;

import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileCreationData;

public interface ResourceCreator {

    void create(UserProfileCreationData profileData);

}
