package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileQueryProvider;

@Service
public class UserProfileRetriever implements ResourceRetriever<UserProfileIdentifier> {

    @Autowired
    private UserProfileQueryProvider querySupplier;
    @Autowired
    private IdamService idamService;
    @Autowired
    private AuditRepository auditRepository;

    @Override
    public UserProfile retrieve(UserProfileIdentifier identifier, boolean fetchRoles) {

        UserProfile userProfile =
            querySupplier.getRetrieveByIdQuery(identifier)
                .get()
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Could not find resource from database with given identifier: "
                        + identifier.getValue()));
        if (fetchRoles) {
            userProfile = getRolesFromIdam(userProfile, false);
        }
        return userProfile;
    }

    public UserProfile getRolesFromIdam(UserProfile userProfile, boolean isMultiUserGet) {

        IdamRolesInfo idamRolesInfo = idamService.fetchUserById(userProfile.getIdamId().toString());
        if (idamRolesInfo.getResponseStatusCode().is2xxSuccessful()) {
            persistAudit(idamRolesInfo, userProfile);
            userProfile.setRoles(idamRolesInfo);
        } else {
            persistAudit(idamRolesInfo, userProfile);
            // for multiple users get request , do not throw exception and continue flow
            if (!isMultiUserGet) {
                throw new IdamServiceException(idamRolesInfo.getStatusMessage(), idamRolesInfo.getResponseStatusCode());
            } else {
                // if SIDAM fails then send errorMessage and status code in response
                userProfile.setErrorMessage(idamRolesInfo.getStatusMessage());
                userProfile.setErrorStatusCode(idamRolesInfo.getResponseStatusCode().value());
            }
        }
        return userProfile;
    }

    public List<UserProfile> retrieveMultipleProfiles(UserProfileIdentifier identifier, boolean showDeleted) {
        //get all users from UP DB
        List<UserProfile> userProfiles = querySupplier.getProfilesByIds(identifier, showDeleted).orElse(new ArrayList<UserProfile>());
        if (CollectionUtils.isEmpty(userProfiles)) {
            throw new ResourceNotFoundException("Could not find resource");
        }
        //get roles from sidam for each user
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return userProfiles.stream().map(profile -> {
            RequestContextHolder.setRequestAttributes(attributes);
            return getRolesFromIdam(profile, true);
        }).collect(Collectors.toList());

    }

    private void persistAudit(IdamRolesInfo idamRolesInfo, UserProfile userProfile) {
        Audit audit = new Audit(idamRolesInfo.getResponseStatusCode().value(), idamRolesInfo.getStatusMessage(), ResponseSource.API, userProfile);
        auditRepository.save(audit);
    }

}
