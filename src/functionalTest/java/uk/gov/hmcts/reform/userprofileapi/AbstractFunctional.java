package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.GetUserProfileWithRolesResponse;



public class AbstractFunctional {

    @Value("${targetInstance}") protected String targetInstance;

    @Autowired
    protected FuncTestRequestHandler testRequestHandler;

    protected String requestUri = "/v1/userprofile";

    protected CreateUserProfileResponse createUserProfile(CreateUserProfileData createUserProfileData,HttpStatus expectedStatus) throws Exception {

        CreateUserProfileResponse resource = testRequestHandler.sendPost(
                        createUserProfileData,
                        expectedStatus,
                        requestUri,
                        CreateUserProfileResponse.class
                );
        verifyCreateUserProfile(resource);
        return resource;
    }

    protected CreateUserProfileData createUserProfileData() {
        return buildCreateUserProfileData();
    }

    protected void verifyCreateUserProfile(CreateUserProfileResponse resource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull();
        assertThat(resource.getIdamId()).isInstanceOf(UUID.class);
        assertThat(resource.getIdamRegistrationResponse()).isEqualTo(HttpStatus.CREATED.value());
    }

    protected void verifyGetUserProfile(GetUserProfileResponse resource, CreateUserProfileData expectedResource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull().isExactlyInstanceOf(UUID.class);
        assertThat(resource.getFirstName()).isEqualTo(expectedResource.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedResource.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedResource.getEmail().toLowerCase());
        assertThat(resource.getStatus()).isNotNull();
    }

    protected void verifyGetUserProfileWithRoles(GetUserProfileWithRolesResponse resource, CreateUserProfileData expectedResource) {

        verifyGetUserProfile(resource, expectedResource);
        assertThat(resource.getRoles()).isNotEmpty();
    }

}
