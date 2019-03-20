package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.UUID;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileResource {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String languagePreference;

    private boolean emailCommsConsent;
    private LocalDateTime emailCommsConsentTs;
    private boolean postalCommsConsent;
    private LocalDateTime postalCommsConsentTs;

    private String creationChannel;
    private String userCategory;
    private String userType;

    private String idamId;
    private String idamStatus;
    private String idamRoles;
    private Integer idamRegistrationResponse;

    private LocalDateTime createdTs;
    private LocalDateTime lastUpdatedTs;

    private String userProfileStatus;

    public UserProfileResource() {
    }

    public UserProfileResource(UserProfile userProfile) {

        requireNonNull(userProfile, "userProfile must not be null");

        this.id = userProfile.getId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();

        this.languagePreference = userProfile.getLanguagePreference().toString();
        this.emailCommsConsent = userProfile.isEmailCommsConsent();
        this.emailCommsConsentTs = userProfile.getEmailCommsConsentTs();
        this.postalCommsConsent = userProfile.isPostalCommsConsent();
        this.postalCommsConsentTs = userProfile.getPostalCommsConsentTs();

        this.creationChannel = userProfile.getCreationChannel().toString();
        this.userCategory = userProfile.getUserCategory().toString();
        this.userType = userProfile.getUserType().toString();

        this.idamId = userProfile.getIdamId();
        this.idamStatus = userProfile.getIdamStatus();
        this.idamRoles = userProfile.getIdamRoles();
        this.idamRegistrationResponse = userProfile.getIdamRegistrationResponse();

        this.createdTs = userProfile.getCreatedTs();
        this.lastUpdatedTs = userProfile.getLastUpdatedTs();

        this.userProfileStatus = userProfile.getUserProfileStatus().toString();

    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLanguagePreference() {
        return languagePreference;
    }

    public boolean isEmailCommsConsent() {
        return emailCommsConsent;
    }

    public LocalDateTime getEmailCommsConsentTs() {
        return emailCommsConsentTs;
    }

    public boolean isPostalCommsConsent() {
        return postalCommsConsent;
    }

    public LocalDateTime getPostalCommsConsentTs() {
        return postalCommsConsentTs;
    }

    public String getCreationChannel() {
        return creationChannel;
    }

    public String getUserCategory() {
        return userCategory;
    }

    public String getUserType() {
        return userType;
    }

    public String getIdamId() {
        return idamId;
    }

    public String getIdamStatus() {
        return idamStatus;
    }

    public String getIdamRoles() {
        return idamRoles;
    }

    public Integer getIdamRegistrationResponse() {
        return idamRegistrationResponse;
    }

    public LocalDateTime getCreatedTs() {
        return createdTs;
    }

    public LocalDateTime getLastUpdatedTs() {
        return lastUpdatedTs;
    }

    public String getUserProfileStatus() {
        return userProfileStatus;
    }
}

