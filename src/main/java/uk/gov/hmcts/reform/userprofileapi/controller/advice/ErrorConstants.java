package uk.gov.hmcts.reform.userprofileapi.controller.advice;

public enum ErrorConstants {

    MALFORMED_JSON("1 : Malformed Input Request"),

    UNSUPPORTED_MEDIA_TYPES("2 : Unsupported Media Type"),

    INVALID_REQUEST("3 : There is a problem with your request. Please check and try again"),

    RESOURCE_NOT_FOUND("4 : Resource not found"),

    METHOD_ARG_NOT_VALID("5 : validation on an argument failed"),

    DATA_INTEGRITY_VIOLATION("6 : attempt to insert or update data resulted in violation of an integrity"
            .concat(" constraint")),

    USER_ALREADY_ACTIVE(
            "7 : Resend invite failed as user is already active. Wait for some time for the system to "
                    .concat("refresh.")),

    UNKNOWN_EXCEPTION("8 : error was caused by an unknown exception"),

    TOO_MANY_REQUESTS(
            "10 : The request was last made less than %s minutes ago. Please try after some time");



    private final String errorMessage;

    ErrorConstants(String  errorMessage) {
        this.errorMessage  = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
