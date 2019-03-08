package uk.gov.hmcts.reform.userprofileapi.domain.service;

import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

public interface ResourceCreator {

    void create(CreateUserProfileData profileData);

}
