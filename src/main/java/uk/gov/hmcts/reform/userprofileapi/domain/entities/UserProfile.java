package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SequenceGenerator(name = "user_profile_id_seq", sequenceName = "user_profile_id_seq", allocationSize = 1)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_profile_id_seq")
    private Long id;

    private String idamId;

    @Column(name = "email_address")
    private String email;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private LanguagePreference languagePreference = LanguagePreference.EN;

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
    private IdamStatus status = IdamStatus.PENDING;

    @Transient
    private Integer idamRegistrationResponse;

    @CreationTimestamp
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "userProfile")
    private List<Audit> responses = new ArrayList<>();

    @Transient
    private List<String> roles = new ArrayList<>();

    @Transient
    private String errorMessage;

    @Transient
    private String errorStatusCode;

    public UserProfile(UserProfileCreationData data, HttpStatus idamStatus) {
        this.email = data.getEmail().trim().toLowerCase();
        this.firstName = data.getFirstName().trim();
        this.lastName = data.getLastName().trim();

        if (StringUtils.isNotBlank(data.getLanguagePreference())) {
            this.languagePreference = LanguagePreference.valueOf(data.getLanguagePreference());
        }
        this.emailCommsConsent = data.isEmailCommsConsent();
        this.postalCommsConsent = data.isPostalCommsConsent();
        this.userCategory = UserCategory.valueOf(data.getUserCategory());
        this.userType = UserType.valueOf(data.getUserType());
        this.idamRegistrationResponse = idamStatus.value();
    }


    public void setStatus(UserProfileCreationData userProfileCreationData) {
        this.status = userProfileCreationData.getStatus();
    }

    public void setStatus(IdamStatus status) {
        this.status = status;
    }

    public void setRoles(IdamRolesInfo idamrolesInfo) {
        this.roles = idamrolesInfo.getRoles();
    }
}
