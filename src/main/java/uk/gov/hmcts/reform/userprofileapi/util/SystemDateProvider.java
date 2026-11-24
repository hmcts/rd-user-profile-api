package uk.gov.hmcts.reform.userprofileapi.util;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.DateProvider;

import java.time.LocalDate;

@Service
public class SystemDateProvider implements DateProvider {

    public LocalDate now() {
        return LocalDate.now();
    }
}
