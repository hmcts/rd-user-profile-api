package uk.gov.hmcts.reform.userprofileapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WelcomeControllerTest {

    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    void test_return_welcome_response() {
        String expectedMessage = "Welcome to the User Profile API";
        ResponseEntity<String> responseEntity = welcomeController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).contains(expectedMessage);
    }
}
