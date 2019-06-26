package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
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
@SequenceGenerator(name = "user_profile_id_seq", sequenceName = "user_profile_id_seq", allocationSize = 1)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_profile_id_seq")
    private Long id;
    private UUID idamId;
    @Column(name = "email_address")
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

    @Column (name = "idam_status")
    @Enumerated(EnumType.STRING)
    private IdamStatus status;
    private Integer idamRegistrationResponse;

    @CreationTimestamp
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Transient
    private List<String> roles = new ArrayList<String>();

    public UserProfile() {
        //noop
    }

    public UserProfile(CreateUserProfileData data, IdamRegistrationInfo idamInfo) {

        this.email = data.getEmail().trim().toLowerCase();
        this.firstName = data.getFirstName().trim();
        this.lastName = data.getLastName().trim();
        this.languagePreference = LanguagePreference.valueOf(data.getLanguagePreference());
        this.userCategory = UserCategory.valueOf(data.getUserCategory());
        this.userType = UserType.valueOf(data.getUserType());
        this.idamRegistrationResponse = idamInfo.getIdamRegistrationResponse().value();
        this.status = IdamStatus.PENDING;
        this.idamId = UUID.randomUUID();

    }

    public void setRoles(IdamRolesInfo idamrolesInfo) {
        this.roles = idamrolesInfo.getRoles();
    }
}
