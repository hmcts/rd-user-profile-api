package uk.gov.hmcts.reform.userprofileapi.domain;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate now();
}
