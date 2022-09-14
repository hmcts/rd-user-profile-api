package uk.gov.hmcts.reform.userprofileapi.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserProfileRetriever implements ResourceRetriever<UserProfileIdentifier> {
    @Autowired
    private final AuditService auditService;
    @Autowired
    private final UserProfileQueryProvider querySupplier;
    @Autowired
    private final IdamService idamService;
    @Autowired
    private final AuditRepository auditRepository;

    @Override
    public UserProfile retrieve(UserProfileIdentifier identifier, boolean fetchRoles) {

        UserProfile userProfile =
            querySupplier.getRetrieveByIdQuery(identifier)
                .get()
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Could not find resource from database with given identifier"));
        if (fetchRoles) {
            userProfile = getRolesFromIdam(userProfile, false);
            log.debug("get Roles from Idam" + userProfile.getStatus() + userProfile.getIdamId()
                    + userProfile.getRoles() + userProfile.getErrorMessage());
        }
        log.debug("Inside retrieve method return userProfile" + userProfile.getErrorMessage()
                + userProfile.getErrorStatusCode() + userProfile.getStatus());
        return userProfile;
    }

    public UserProfile getRolesFromIdam(UserProfile userProfile, boolean isMultiUserGet) {

        log.debug("Inside getRolesFromIdam" + userProfile.getStatus());
        if (IdamStatus.ACTIVE == userProfile.getStatus()) {
            log.debug("IdamService fetchUserById");
            IdamRolesInfo idamRolesInfo = idamService.fetchUserById(userProfile.getIdamId());
            log.debug("fetch user By Id" + idamRolesInfo.getId() + idamRolesInfo.getStatusMessage()
                    + idamRolesInfo.getRoles() + idamRolesInfo.getResponseStatusCode());
            if (idamRolesInfo.getResponseStatusCode().is2xxSuccessful()) {
                persistAudit(idamRolesInfo, userProfile);
                userProfile.setRoles(idamRolesInfo);
                userProfile.setErrorMessage(idamRolesInfo.getStatusMessage());
                userProfile.setErrorStatusCode(String.valueOf(idamRolesInfo.getResponseStatusCode().value()));
            } else {
                persistAudit(idamRolesInfo, userProfile);
                // for multiple users get request , do not throw exception and continue flow
                if (!isMultiUserGet) {
                    throw new IdamServiceException(idamRolesInfo.getStatusMessage(),
                            idamRolesInfo.getResponseStatusCode());
                } else {
                    // if SIDAM fails then send errorMessage and status code in response
                    userProfile.setErrorMessage(idamRolesInfo.getStatusMessage());
                    userProfile.setErrorStatusCode(String.valueOf(idamRolesInfo.getResponseStatusCode().value()));
                    log.debug("inside else method where idam status is not success"
                            + idamRolesInfo.getResponseStatusCode());
                }
            }
        } else {
            userProfile.setErrorMessage(IdamStatusResolver.NO_IDAM_CALL);
            userProfile.setErrorStatusCode(" ");
            log.debug("Inside Else Block" + userProfile.getErrorMessage() + userProfile.getErrorStatusCode());
        }
        log.debug("In the end of the method getRolesFromIdam" + userProfile.getErrorStatusCode()
                + userProfile.getErrorMessage() + userProfile.getStatus());
        return userProfile;
    }

    public List<UserProfile> retrieveMultipleProfiles(UserProfileIdentifier identifier, boolean showDeleted,
                                                      boolean rolesRequired) {
        //get all users from UP DB
        List<UserProfile> userProfiles = querySupplier.getProfilesByIds(identifier, showDeleted)
                .orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(userProfiles)) {
            throw new ResourceNotFoundException("Could not find resource");
        }
        //get roles from sidam for each user
        if (rolesRequired) {
            return userProfiles.stream().map(profile -> getRolesFromIdam(profile, true))
                    .toList();
        } else {
            return userProfiles;
        }
    }

    private void persistAudit(IdamRolesInfo idamRolesInfo, UserProfile userProfile) {
        Audit audit = new Audit(idamRolesInfo.getResponseStatusCode().value(), idamRolesInfo.getStatusMessage(),
                ResponseSource.API, userProfile);
        auditRepository.save(audit);
    }

}
