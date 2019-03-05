package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceRetriever;

@Api(
    value = "/profiles",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)

@RestController
public class UserProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(UserProfileController.class);

    private ResourceRetriever resourceRetriever;

    public UserProfileController(ResourceRetriever resourceRetriever, JdbcTemplate jdbcTemplate) {
        this.resourceRetriever = resourceRetriever;
    }

    @ApiOperation("Retrieves user profile data")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        )
    })
    @GetMapping(
        path = "/{id}",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<Object> getUserProfile(@NotNull @PathVariable String id) {

        LOG.info("Getting user profile with id: {}", id);

        throw new UnsupportedOperationException("Still being implemented");


    }
}
