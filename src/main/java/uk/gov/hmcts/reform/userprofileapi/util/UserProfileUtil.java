package uk.gov.hmcts.reform.userprofileapi.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;

import java.util.Optional;

import static java.util.Objects.nonNull;

@Slf4j
public class UserProfileUtil {

    private UserProfileUtil() {
    }

    public static final String USER_EMAIL = "UserEmail";

    public static String getUserEmailFromHeader() {
        String userEmail = null;
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (nonNull(servletRequestAttributes)) {

            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (nonNull(request.getHeader(USER_EMAIL))) {

                userEmail = request.getHeader(USER_EMAIL);
            }
        }

        return userEmail;
    }

    public static void idam5xxErrorResponse(String message, HttpStatus idamStatus) {
        Optional<HttpStatus> idapStatusOptional = Optional.ofNullable(idamStatus);
        if (idapStatusOptional.isPresent() && idapStatusOptional.get().is5xxServerError()) {
            throw new IdamServiceException(message, HttpStatus.UNAUTHORIZED);
        }

    }
}
