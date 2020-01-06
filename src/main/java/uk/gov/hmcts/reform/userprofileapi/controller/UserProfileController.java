package uk.gov.hmcts.reform.userprofileapi.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateCreateUserProfileRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.*;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.*;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator;


@Api(
    value = "/v1/userprofile"
)

@RequestMapping(
    path = "/v1/userprofile"
)

@Slf4j
@RestController
public class UserProfileController {

    @Autowired
    private UserProfileService<RequestData> userProfileService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private ValidationService validationService;


    @ApiOperation(value = "Create a User Profile",
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization") 
                  })
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = "Create a User Profile using request body",
            response = UserProfileCreationResponse.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 409,
            message = "User already exists",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })

    @PostMapping(
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileCreationResponse> createUserProfile(@Valid @RequestBody UserProfileCreationData userProfileCreationData) {
        //Creating new User Profile

        validateCreateUserProfileRequest(userProfileCreationData);

        UserProfileCreationResponse resource = userProfileService.create(userProfileCreationData);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);

    }

    @ApiOperation(value = "Retrieves user profile with roles by id",
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 404,
            message = "Not Found",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })
    @GetMapping(
        path = "/{id}/roles",
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileWithRolesResponse> getUserProfileWithRolesById(@PathVariable String id) {
        //Getting user profile by id
        isUserIdValid(id, true);
        UserProfileWithRolesResponse response = userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID, id));
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Retrieves user profile with roles by email",
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  }
    )
    @ApiParam(name = "email", required = true)
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 404,
            message = "Not Found",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })
    @GetMapping(
            path = "/roles",
            params = "email",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileWithRolesResponse> getUserProfileWithRolesByEmail(@RequestParam String email) {
        //Getting user profile by email

        requireNonNull(email, "email cannot be null");
        UserProfileWithRolesResponse response = userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.EMAIL, email.toLowerCase()));
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Retrieves user profile queried by email or userId. If both provided email is preferred", 
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Representation of a user profile data",
                    response = UserProfileResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not Found"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )

    })
    @GetMapping(
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResponse> getUserProfileByEmail(@ApiParam(name = "email", required = false) @RequestParam (value = "email", required = false) String email,
                                                                     @ApiParam(name = "userId", required = false) @RequestParam (value = "userId", required = false) String userId) {
        UserProfileResponse response;
        if (email == null && userId == null) {
            return ResponseEntity.badRequest().build();
        } else if (email != null) {

            //Getting user profile by email

            response =
                    userProfileService.retrieve(
                            new UserProfileIdentifier(IdentifierName.EMAIL, email.toLowerCase().trim())
                    );
        } else {
            isUserIdValid(userId, true);
            response =  userProfileService.retrieve(
                    new UserProfileIdentifier(IdentifierName.UUID, userId.trim()));
        }
        return ResponseEntity.ok().body(response);
    }

    @ApiOperation(value = "Update user profile", 
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Update User Profile using request body",
                    response = UserProfileCreationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request",
                    response = String.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error",
                    response = String.class
            )
    })

    @PutMapping(
            path = "/{userId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )

    @ResponseBody
    public ResponseEntity updateUserProfile(@Valid @RequestBody UpdateUserProfileData updateUserProfileData,
                                                                 @PathVariable String userId,
                                                                 @ApiParam(name = "origin", required = false) @RequestParam (value = "origin", required = false) String origin) {
        UserProfileRolesResponse userProfileResponse = null;
        if (CollectionUtils.isEmpty(updateUserProfileData.getRolesAdd())
             && CollectionUtils.isEmpty(updateUserProfileData.getRolesDelete())) {
            //Updating user profile details
            AttributeResponse attributeResponse = userProfileService.update(updateUserProfileData, userId, origin);
            userProfileResponse = new UserProfileRolesResponse();
            userProfileResponse.setAttributeResponse(attributeResponse);
            return ResponseEntity.status(attributeResponse.getIdamStatusCode()).body(userProfileResponse);

        } else { // New update roles behavior
            //Updating user roles
            UserProfileValidator.validateUserProfileDataAndUserId(updateUserProfileData, userId);
            userProfileResponse = userProfileService.updateRoles(updateUserProfileData, userId);
            return ResponseEntity.ok().body(userProfileResponse);
        }

    }

    @ApiOperation(value = "Retrieving multiple user profiles",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiParam(
            name = "showdeleted",
            required = true
    )

    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Retrieving multiple user profiles",
                    response = UserProfileDataResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request",
                    response = String.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "Resource not found",
                    response = String.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error",
                    response = String.class
            )
    })

    @PostMapping(
            path = "/users",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileDataResponse> retrieveUserProfiles(@ApiParam(name = "showdeleted", required = true)@RequestParam (value = "showdeleted", required = true) String showDeleted,
                                                                        @ApiParam(name = "rolesRequired", required = true)@RequestParam (value = "rolesRequired", required = true) String rolesRequired,
                                                                        @RequestBody UserProfileDataRequest userProfileDataRequest) {
        //Retrieving multiple user profiles

        boolean showDeletedBoolean = UserProfileValidator.validateAndReturnBooleanForParam(showDeleted);
        boolean rolesRequiredBoolean = UserProfileValidator.validateAndReturnBooleanForParam(rolesRequired);
        UserProfileValidator.validateUserIds(userProfileDataRequest);
        UserProfileDataResponse userProfileDataResponse =
                userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID_LIST, userProfileDataRequest.getUserIds()), showDeletedBoolean, rolesRequiredBoolean);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileDataResponse);

    }

}
