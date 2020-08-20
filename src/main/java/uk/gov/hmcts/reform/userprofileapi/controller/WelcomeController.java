package uk.gov.hmcts.reform.userprofileapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(
    value = "/",
    produces = MediaType.APPLICATION_JSON_VALUE
)

@Slf4j
@RestController
public class WelcomeController {

    private static final String INSTANCE_ID = UUID.randomUUID().toString();
    private static final String MESSAGE = "Welcome to the User Profile API";

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @ApiOperation("Welcome to the System User Profile Data API")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Welcome message",
            response = String.class
        )
    })
    @GetMapping(
        path = "/",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> welcome() {

        log.info("{}:: Welcome message '{}' from running instance: {}", loggingComponentName, MESSAGE, INSTANCE_ID);

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .body("{\"message\": \"" + MESSAGE + "\"}");
    }
}
