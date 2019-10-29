package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;

@Service
public class ExceptionServiceImpl implements ExceptionService {

    public void throwCustomRuntimeException(String className, String msg) {
        throwCustomRuntimeException(className, msg, HttpStatus.OK);
    }

    public void throwCustomRuntimeException(String className, String msg, HttpStatus httpStatus) {
        switch (className) {
            case "IdamServiceException" : throw new IdamServiceException(msg, httpStatus);
            case "RequiredFieldMissingException" : throw new RequiredFieldMissingException(msg);
            case "ResourceNotFoundException" : throw new ResourceNotFoundException(msg);
            default: throw new RuntimeException("Unhandled exception:" + msg);
        }
    }
}
