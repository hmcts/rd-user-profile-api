package uk.gov.hmcts.reform.userprofileapi.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserIdamStatusWithEmailResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.exception.ForbiddenException;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator;

import javax.validation.Valid;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.API_IS_NOT_AVAILABLE_IN_PROD_ENV;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.NO_USER_ID_OR_EMAIL_PATTERN_PROVIDED_TO_DELETE;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileUtil.getUserEmailFromHeader;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateCreateUserProfileRequest;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateUserIds;


@RequestMapping(
        path = "/v1/userprofile"
)

@Slf4j
@RestController
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileController {

    @Autowired
    private UserProfileService<RequestData> userProfileService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private ValidationService validationService;

    @Value("${environment_name}")
    private String environmentName;

    @Operation(summary = "Create a User Profile",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })

            @ApiResponse(
                    responseCode = "201",
                    description = "User Profile created successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileCreationResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested"
                            + " resource is restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "409",
                    description = "A User already exists with the given information",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests made for re-invite",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )


    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<UserProfileCreationResponse> createUserProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "userProfileCreationData")
            @Valid @RequestBody UserProfileCreationData userProfileCreationData,
            @RequestParam(value = "origin", required = false)
            @Parameter(name = "origin", description = "Any Valid String is allowed") String origin) {
        log.debug("Inside createUserProfile Controller" + origin);
        UserProfileCreationResponse resource;
        validateCreateUserProfileRequest(userProfileCreationData);

        if (userProfileCreationData.isResendInvite()) {
            resource = userProfileService.reInviteUser(userProfileCreationData);
        } else {
            resource = userProfileService.create(userProfileCreationData, origin);
        }
        log.debug("Response createUserProfile from controller" + resource.getIdamRegistrationResponse());
        return ResponseEntity.status(resource.getIdamRegistrationResponse()).body(resource);
    }

    @Operation(summary = "Retrieves a User Profile and their Roles with the given ID",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

            @ApiResponse(
                    responseCode = "200",
                    description = "Representation of a User profile with their Roles",
                    content = @Content(schema = @Schema(implementation = UserProfileWithRolesResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested "
                            + "resource is restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "Not User Profile found with the given ID",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )

    @GetMapping(
            path = "/{id}/roles",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileWithRolesResponse> getUserProfileWithRolesById(@PathVariable String id) {
        //Getting user profile by id
        isUserIdValid(id, true);
        log.debug("Inside getUserProfileWithRolesById Controller" + id);
        UserProfileWithRolesResponse response = userProfileService
                .retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID, id));
        log.debug("Response retuned to the controller" + response.getIdamMessage() + response.getIdamStatusCode()
                + response.getIdamStatus());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieves a User Profile and their Roles with the given Email Address",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization"),
                    @SecurityRequirement(name = "UserEmail")
            }
    )


            @ApiResponse(
                    responseCode = "200",
                    description = "Representation of a User profile with their Roles",
                    content = @Content(schema = @Schema(implementation = UserProfileWithRolesResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested resource is "
                            + "restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "No User Profile found with the given Email Address",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )

    @GetMapping(
            path = "/roles",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileWithRolesResponse> getUserProfileWithRolesByEmail() {
        String userEmail = getUserEmailFromHeader();

        if (isEmpty(userEmail)) {
            throw new InvalidRequest("No User Email provided via header");
        }

        UserProfileWithRolesResponse response = userProfileService
                .retrieveWithRoles(new UserProfileIdentifier(IdentifierName.EMAIL, userEmail.toLowerCase()));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve a User Profile by Email or ID. If both are present then Email is used to retrieve.",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization"),
                    @SecurityRequirement(name = "UserEmail")
            })

            @ApiResponse(
                    responseCode = "200",
                    description = "Representation of a User profile",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested resource is "
                            + "restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "No User Profile found with the given ID",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )


    @GetMapping(
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResponse> getUserProfileByEmail(
            @RequestParam(value = "userId", required = false) String userId) {
        UserProfileResponse response;
        String userEmail = getUserEmailFromHeader();
        if (userEmail == null && userId == null) {
            return ResponseEntity.badRequest().build();
        } else if (userEmail != null) {

            response =
                    userProfileService.retrieve(
                            new UserProfileIdentifier(IdentifierName.EMAIL, userEmail.toLowerCase().trim())
                    );
        } else {
            isUserIdValid(userId, true);
            response = userProfileService.retrieve(
                    new UserProfileIdentifier(IdentifierName.UUID, userId.trim()));
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Update a User Profile",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })

            @ApiResponse(
                    responseCode = "200",
                    description = "User Profile has been Updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileCreationResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested resource is "
                            + "restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "No User Profile found with the given ID",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "412",
                    description = "One or more of the Roles provided is already assigned to the User",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )


    @PutMapping(
            path = "/{userId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )

    @ResponseBody
    public ResponseEntity<UserProfileRolesResponse> updateUserProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "updateUserProfileData")
            @Valid @RequestBody UpdateUserProfileData updateUserProfileData,
            @PathVariable String userId,
            @Parameter(name = "origin") @RequestParam(value = "origin", required = false) String origin) {

        UserProfileRolesResponse userProfileResponse = null;
        if (CollectionUtils.isEmpty(updateUserProfileData.getRolesAdd())
                && CollectionUtils.isEmpty(updateUserProfileData.getRolesDelete())) {
            //Updating user profile details
            AttributeResponse attributeResponse = userProfileService.update(updateUserProfileData, userId, origin);
            userProfileResponse = new UserProfileRolesResponse();
            userProfileResponse.setAttributeResponse(attributeResponse);
            return ResponseEntity.status(attributeResponse.getIdamStatusCode()).body(userProfileResponse);

        } else if (isEachAttributeNull(updateUserProfileData)) { // New update roles behavior
            //Updating user roles
            UserProfileValidator.validateUserProfileDataAndUserId(updateUserProfileData, userId);
            userProfileResponse = userProfileService.updateRoles(updateUserProfileData, userId);
            return ResponseEntity.ok().body(userProfileResponse);
        } else {
            UserProfileRolesResponse userProfileRolesResponse
                    = userProfileService.updateUserProfileData(updateUserProfileData, userId, origin);
            return ResponseEntity.ok().body(userProfileRolesResponse);
        }

    }

    @Operation(summary = "Retrieve multiple User Profiles",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })
    @Parameter(
            name = "showdeleted",
            required = true
    )


            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved multiple User Profiles",
                    content = @Content(schema = @Schema(implementation = UserProfileDataResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested resource is "
                            + "restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "No User Profile found with the given ID",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )

@PostMapping(
            path = "/users",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileDataResponse> retrieveUserProfiles(
            @Parameter(name = "showdeleted", required = true)
            @RequestParam(value = "showdeleted",
                    required = true) String showDeleted,
            @Parameter(name = "rolesRequired",
                    required = true)
            @RequestParam(value = "rolesRequired",
                    required = true) String rolesRequired,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "userProfileDataRequest")
            @RequestBody UserProfileDataRequest
                    userProfileDataRequest) {
        //Retrieving multiple user profiles

        boolean showDeletedBoolean = UserProfileValidator.validateAndReturnBooleanForParam(showDeleted);
        boolean rolesRequiredBoolean = UserProfileValidator.validateAndReturnBooleanForParam(rolesRequired);
        validateUserIds(userProfileDataRequest);
        UserProfileDataResponse userProfileDataResponse =
                userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID_LIST,
                        userProfileDataRequest.getUserIds()), showDeletedBoolean, rolesRequiredBoolean);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileDataResponse);

    }

    @Operation(summary = "Delete User Profiles",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })

            @ApiResponse(
                    responseCode = "204",
                    description = "User Profiles deleted successfully",
                    content = @Content(schema = @Schema(implementation = UserProfilesDeletionResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested resource is "
                            + "restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )


    @DeleteMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<UserProfilesDeletionResponse> deleteUserProfiles(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "userProfilesDeletionDataReq")
            @Valid @RequestBody UserProfileDataRequest userProfilesDeletionDataReq) {
        UserProfilesDeletionResponse resource;

        validateUserIds(userProfilesDeletionDataReq);

        resource = userProfileService.delete(userProfilesDeletionDataReq);

        return ResponseEntity.status(resource.getStatusCode()).body(resource);
    }

    @Operation(summary = "Delete User Profiles by User ID or Email Pattern",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })

            @ApiResponse(
                    responseCode = "204",
                    description = "User Profiles deleted successfully",
                    content = @Content(schema = @Schema(implementation = UserProfilesDeletionResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "An invalid request has been provided",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : The requested resource is "
                            + "restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )


    @DeleteMapping(
            path = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<UserProfilesDeletionResponse> deleteUserProfileByIdOrEmailPattern(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "emailPattern", required = false) String emailPattern) {

        /**
         * This API will need to be revisited if it is to be used for business functionality.
         */

        log.info("ENVIRONMENT NAME:::::: " + environmentName);

        if (environmentName.equalsIgnoreCase("PROD")) {
            throw new ForbiddenException(API_IS_NOT_AVAILABLE_IN_PROD_ENV.getErrorMessage());
        }

        UserProfilesDeletionResponse resource;

        if (isNotBlank(userId)) {
            resource = userProfileService.deleteByUserId(userId);

        } else if (isNotBlank(emailPattern)) {
            resource = userProfileService.deleteByEmailPattern(emailPattern);

        } else {
            throw new InvalidRequest(NO_USER_ID_OR_EMAIL_PATTERN_PROVIDED_TO_DELETE.getErrorMessage());
        }

        return ResponseEntity.status(resource.getStatusCode()).body(resource);
    }

    private boolean isEachAttributeNull(UpdateUserProfileData updateUserProfileData) {
        return !StringUtils.hasLength(updateUserProfileData.getFirstName())
                || !StringUtils.hasLength(updateUserProfileData.getLastName())
                || !StringUtils.hasLength(updateUserProfileData.getIdamStatus());
    }


    @Operation(summary = "Retrieves email IDs and IDAM status of user profiles",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )

            @ApiResponse(
                    responseCode = "200",
                    description = "",
                    content = @Content(schema = @Schema(implementation = UserIdamStatusWithEmailResponse.class))
            )
            @ApiResponse(
                    responseCode = "400",
                    description = "There is a problem with your request. Please check and try again",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized Error : "
                            + "The requested resource is restricted and requires authentication",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "404",
                    description = "Could not find any profiles",
                    content = @Content
            )
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
            )

    @GetMapping(
            path = "/idamStatus",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserIdamStatusWithEmailResponse> getUserProfileIdamStatus(@RequestParam String category) {
        if (StringUtils.hasText(category) && category.equalsIgnoreCase("caseworker")) {
            log.debug("Inside getUserProfileIdamStatus Controller" + category);
            UserIdamStatusWithEmailResponse response = userProfileService
                    .retrieveIdamStatus(category);
            log.debug("Response returned to the controller");
            return ResponseEntity.ok(response);
        } else {
            throw new InvalidRequest(INVALID_REQUEST.getErrorMessage());
        }

    }

}