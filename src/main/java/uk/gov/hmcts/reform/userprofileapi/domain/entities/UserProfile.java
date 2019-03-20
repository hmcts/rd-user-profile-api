package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.reform.userprofileapi.domain.*;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@Entity
public class UserProfile {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private LanguagePreference languagePreference;

    private boolean emailCommsConsent;
    private LocalDateTime emailCommsConsentTs;
    private boolean postalCommsConsent;
    private LocalDateTime postalCommsConsentTs;

    @Enumerated(EnumType.STRING)
    private CreationChannel creationChannel;
    @Enumerated(EnumType.STRING)
    private UserCategory userCategory;
    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String idamId;
    private String idamStatus;
    private String idamRoles;
    private Integer idamRegistrationResponse;

    @CreationTimestamp
    private LocalDateTime createdTs;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedTs;

    @Enumerated(EnumType.STRING)
    private UserProfileStatus userProfileStatus;

    public UserProfile() {
        //noop
    }

    public UserProfile(CreateUserProfileData data, IdamRegistrationInfo idamInfo) {

        this.email = data.getEmail();
        this.firstName = data.getFirstName();
        this.lastName = data.getLastName();

        this.languagePreference =
            LanguagePreference.valueOf(
                Optional.ofNullable((
                    data.getLanguagePreference())).orElse(
                    LanguagePreference.EN.toString()));

        this.emailCommsConsent = data.isEmailCommsConsent();
        this.emailCommsConsentTs = LocalDateTime.now();
        this.postalCommsConsent = data.isPostalCommsConsent();
        this.postalCommsConsentTs = LocalDateTime.now();

        this.creationChannel = CreationChannel.API;
        this.userCategory = UserCategory.valueOf(data.getUserCategory());
        this.userType = UserType.valueOf(data.getUserType());

        this.idamRoles = data.getIdamRoles();
        this.idamRegistrationResponse = idamInfo.getIdamRegistrationResponse().value();

        this.userProfileStatus = UserProfileStatus.ACTIVE;

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

    public LanguagePreference getLanguagePreference() {
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

    public CreationChannel getCreationChannel() {
        return creationChannel;
    }

    public UserCategory getUserCategory() {
        return userCategory;
    }

    public UserType getUserType() {
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

    public UserProfileStatus getUserProfileStatus() {
        return userProfileStatus;
    }
}
