package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;



@Getter
@Entity
@Table(name = "user_profile_audit")
@SequenceGenerator(name = "response_id_seq", sequenceName = "response_id_seq", allocationSize = 1)
@NoArgsConstructor
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "response_id_seq")
    private Long id;

    private Integer idamRegistrationResponse;

    private String statusMessage;

    @Enumerated(EnumType.STRING)
    private ResponseSource source;

    @CreationTimestamp
    private LocalDateTime auditTs;

    @ManyToOne
    @JoinColumn(name = "USER_PROFILE_ID")
    private UserProfile userProfile;

    public Audit(Integer idamRegistrationResponse, String statusMessage, ResponseSource source,
                 UserProfile userProfile) {
        this(idamRegistrationResponse, statusMessage, source);
        this.userProfile = userProfile;
    }

    public Audit(Integer idamRegistrationResponse, String statusMessage, ResponseSource source) {
        this.idamRegistrationResponse = idamRegistrationResponse;
        this.statusMessage = statusMessage;
        this.source = source;
    }
}
