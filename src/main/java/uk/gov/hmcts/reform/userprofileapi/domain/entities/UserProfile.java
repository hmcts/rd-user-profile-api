package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@Getter
@Setter
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
    private UserCategory userCategory;
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    private IdamStatus idamStatus;
    private Integer idamRegistrationResponse;

    @CreationTimestamp
    private LocalDateTime createdTs;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedTs;

    @Transient
    private List<String> roles = new ArrayList<String>();

    public UserProfile() {
        //noop
    }

    public UserProfile(CreateUserProfileData data, IdamRegistrationInfo idamInfo) {

        this.email = data.getEmail();
        this.firstName = data.getFirstName();
        this.lastName = data.getLastName();
        this.languagePreference = LanguagePreference.EN;
        this.userCategory = UserCategory.valueOf(data.getUserCategory());
        this.userType = UserType.valueOf(data.getUserType());
        this.idamRegistrationResponse = idamInfo.getIdamRegistrationResponse().value();
        this.idamStatus = IdamStatus.PENDING;

    }

    public void setRoles(IdamRolesInfo idamrolesInfo) {
        this.roles = idamrolesInfo.getRoles();
    }
}
