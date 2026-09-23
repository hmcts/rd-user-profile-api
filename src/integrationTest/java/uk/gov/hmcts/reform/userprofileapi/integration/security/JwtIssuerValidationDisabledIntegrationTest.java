package uk.gov.hmcts.reform.userprofileapi.integration.security;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.userprofileapi.integration.security.BaseSecurityIntegrationTest.VALID_ISSUER_1;
import static uk.gov.hmcts.reform.userprofileapi.integration.security.BaseSecurityIntegrationTest.VALID_ISSUER_2;

@TestPropertySource(properties = {
    "idam.security.issuer-validation=false",
    "idam.security.allowed-issuers[0]=" + VALID_ISSUER_1,
    "idam.security.allowed-issuers[1]=" + VALID_ISSUER_2
})
public class JwtIssuerValidationDisabledIntegrationTest extends BaseSecurityIntegrationTest {

    @MockitoBean
    UserProfileService userProfileService;

    private static Stream<Arguments> issuerValidationDisabledScenarios() {
        return Stream.of(
                Arguments.of(
                        "Scenario 1 - JWT Issuer validation is disabled "
                                + "And Valid primary issuer is accepted - 200",
                        VALID_ISSUER_1,
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 2 - JWT Issuer validation is disabled "
                                + "And Valid secondary issuer is accepted - 200",
                        VALID_ISSUER_2,
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 3 - JWT Issuer validation is disabled And Rogue issuer is accepted - 200",
                        ROGUE_ISSUER,
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 4 - JWT Issuer validation is disabled And Missing issuer is accepted - 200",
                        null,
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 5 - JWT Issuer validation is disabled And Empty issuer is accepted - 200",
                        "",
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 6 - JWT Issuer validation is disabled "
                                + "And Issuer with trailing slash is accepted - 200",
                        VALID_ISSUER_1 + "/",
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 7 - JWT Issuer validation is disabled "
                                + "And Issuer with different case is accepted - 200",
                        VALID_ISSUER_1.toUpperCase(),
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 8 - JWT Issuer validation is disabled And Expired token is rejected - 401",
                        VALID_ISSUER_1,
                        true,
                        UNAUTHORIZED.value())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("issuerValidationDisabledScenarios")
    void issuerValidationDisabled_shouldReturnExpectedStatus(String scenario,
                                                             String jwtIssuer,
                                                             boolean tokenExpired,
                                                             int expectedStatusCode) throws Exception {

        mockCreateUser();
        RequestSpecification jwtRequestSpecification =
                tokenExpired
                        ? expiredJwt(jwtIssuer)
                        : unexpiredJwt(jwtIssuer);

        jwtRequestSpecification
                .when()
                .request()
                .with()
                .body(getObjectMapper().writeValueAsString(getUserProfileCreationData()))
                .and()
                .get(CREATE_USER_URL)
                .then()
                .assertThat()
                .statusCode(expectedStatusCode);
    }

    private void mockCreateUser() {
        when(userProfileService.create(any(), any())).thenReturn(getUserProfileCreationResponse());
    }
}