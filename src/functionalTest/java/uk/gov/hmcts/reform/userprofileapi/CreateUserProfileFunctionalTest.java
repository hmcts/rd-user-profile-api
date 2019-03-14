package uk.gov.hmcts.reform.userprofileapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.serialization.StdSerializer;
import uk.gov.hmcts.reform.userprofileapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CreateUserProfileFunctionalTest {

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private Environment environment;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired private StdSerializer<UserProfile> serializer;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_return_201_and_200_when_creating_and_retrieving_user_profile_when_all_required_fields_provided() {


        String requestUri = "/profiles";
        UserProfile userProfile = new UserProfile("test-idam-id",
            "joe.bloggs@somewhere.com",
            "joe",
            "bloggs");

        String json = serializer.serialize(userProfile);

        UserProfile actualResponseBody =
            SerenityRest
                .given()
                .headers(authorizationHeadersProvider.getServiceAuthorization())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(json)
                .when()
                .post(requestUri)
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract()
                .body()
                .as(UserProfile.class);


    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_required_field_is_missing() {

    }

    @Test
    public void should_return_200_when_retrieving_user_profile_when_uuid_path_param_provided() {

    }

    @Test
    public void should_return_200_when_retrieving_user_profile_when_email_param_provided() {

    }

    @Test
    public void should_return_400_when_retrieving_user_profile_when_email_param_not_correctly_provided() {

    }

    @Test
    public void should_return_200_when_retrieving_user_profile_when_idamId_param_provided() {

    }

    @Test
    public void should_return_400_when_retrieving_user_profile_when_idamId_param_not_correctly_provided() {

    }

}
