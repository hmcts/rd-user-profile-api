package uk.gov.hmcts.reform.userprofileapi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class SystemDateProviderTest {

    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @Test
    void test_returns_now_date() {
        LocalDate actualDate = systemDateProvider.now();
        Assertions.assertNotNull(actualDate);
        Assertions.assertFalse(actualDate.isAfter(LocalDate.now()));
    }
}
