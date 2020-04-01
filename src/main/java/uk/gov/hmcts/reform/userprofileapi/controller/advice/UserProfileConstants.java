package uk.gov.hmcts.reform.userprofileapi.controller.advice;

public class UserProfileConstants {

    private UserProfileConstants() {
    }

    public static final String NAME_FORMAT_REGEX = "^[A-Za-z'-]+$";
    public static final String NAME_FORMAT_ERROR_MESSAGE = "First and Last Names must only consist of Letters aA - zZ and the following special characters ' and -";
}
