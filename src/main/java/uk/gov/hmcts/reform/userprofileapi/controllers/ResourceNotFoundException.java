package uk.gov.hmcts.reform.userprofileapi.controllers;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
