package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
public class CreateUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Test
    public void should_create_user_profile_and_verify_successfully() throws Exception {

        UserProfileCreationResponse createdResource = createUserProfile(createUserProfileData(), HttpStatus.CREATED);

        verifyCreateUserProfile(createdResource);


    }

    @Test
    public void should_create_user_profile_for_duplicate_idam_user_and_verify_successfully_for_prd_roles()
            throws Exception {

        UserProfileCreationData data = createUserProfileData();
        UserProfileCreationResponse duplicateUserResource = createActiveUserProfile(data);
        verifyCreateUserProfile(duplicateUserResource);

        //get user by getUserById to check new roles got added in SIDAM
        //should have 2 roles
        String userId = duplicateUserResource.getIdamId();
        UserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + userId + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");

    }

    @Test
    public void should_create_up_for_duplicate_idam_user_and_verify_roles_updated_successfully_for_user_citizen_role()
            throws Exception {

        //create user with citizen role in SIDAM
        List<String> roles = new ArrayList<>();
        roles.add("citizen");
        roles.add("pui-case-manager");
        Map<String, String> userCreds = idamOpenIdClient.createUser(roles);

        //create user profile in UP with PRD-ADMIN token for above user with same email with "pui-user-manager" roles
        UserProfileCreationData data = createUserProfileData();
        data.setEmail(userCreds.get(EMAIL));
        List<String> xuiRoles = new ArrayList();
        xuiRoles.add("pui-user-manager");
        xuiRoles.add("pui-case-manager");
        data.setRoles(xuiRoles);
        UserProfileCreationResponse duplicateUserResource = createUserProfile(data, HttpStatus.CREATED);
        verifyCreateUserProfile(duplicateUserResource);

        //get user by getUserById to check new roles got added in SIDAM
        String userId = duplicateUserResource.getIdamId();
        UserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + userId + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource.getRoles()).contains("citizen");
        assertThat(resource.getRoles()).contains("pui-user-manager");
        assertThat(resource.getRoles()).contains("pui-case-manager");
    }

    @Test
    public void should_return_201_when_sending_extra_fields() throws Exception {

        JSONObject json = new JSONObject(testRequestHandler.asJsonString(createUserProfileData()));
        json.put("extra-field1", randomAlphabetic(20));
        json.put("extra-field2", randomAlphabetic(20));

        LOG.info("json output {} ", json.toString());

        testRequestHandler.sendPost(json.toString(), HttpStatus.CREATED, requestUri);
    }

    @Test
    public void should_return_400_when_attempting_to_add_duplicate_emails() throws Exception {

        UserProfileCreationData data = createUserProfileData();

        UserProfileCreationResponse createdResource =
                testRequestHandler.sendPost(
                        data,
                        HttpStatus.CREATED,
                        requestUri,
                        UserProfileCreationResponse.class
                );

        assertThat(createdResource).isNotNull();

        testRequestHandler.sendPost(
                testRequestHandler.asJsonString(data),
                HttpStatus.CONFLICT,
                requestUri);
    }

    @Test
    public void should_return_404_when_invalid_roles_are_passed_while_user_registration() throws Exception {

        UserProfileCreationData data = createUserProfileData();

        List<String> roles = new ArrayList<>();
        roles.add("puicase-manager");
        data.setRoles(roles);

        ErrorResponse errorResponse =
                testRequestHandler.sendPost(
                        data,
                        HttpStatus.NOT_FOUND,
                        requestUri,
                        ErrorResponse.class
                );

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrorMessage()).isEqualTo("16 Resource not found");
        assertThat(errorResponse.getErrorDescription()).isEqualTo("The role to be assigned does not exist.");
    }

}
