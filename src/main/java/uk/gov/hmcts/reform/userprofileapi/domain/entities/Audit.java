package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;



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
