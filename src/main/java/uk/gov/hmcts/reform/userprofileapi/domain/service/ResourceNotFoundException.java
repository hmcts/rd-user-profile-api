package uk.gov.hmcts.reform.userprofileapi.domain.service;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
