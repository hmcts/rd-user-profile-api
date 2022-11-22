package uk.gov.hmcts.reform.userprofileapi.integration.util;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class UserProfileApiPostgresqlContainer extends PostgreSQLContainer<UserProfileApiPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";

    private UserProfileApiPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    @Container
    private static final UserProfileApiPostgresqlContainer container = new UserProfileApiPostgresqlContainer();

    @Test
    void test() {
        assertTrue(container.isRunning());
    }

}
