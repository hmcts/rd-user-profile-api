package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;


public interface ResourceRetriever<T extends RequestData> {

    UserProfile retrieve(T identifier, boolean fetchRoles);

}
