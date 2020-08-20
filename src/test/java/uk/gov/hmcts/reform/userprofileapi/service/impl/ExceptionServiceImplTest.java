package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.exception.ErrorPersistingException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.exception.UndefinedException;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionServiceImplTest {

    private ExceptionServiceImpl sut = new ExceptionServiceImpl();

    @Test(expected = ResourceNotFoundException.class)
    public void test_ThrowRuntimeException() {
        sut.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, "ResourceNotFoundException Message");
    }

    @Test(expected = IdamServiceException.class)
    public void test_ThrowIdamServiceException() {
        sut.throwCustomRuntimeException(ExceptionType.IDAMSERVICEEXCEPTION, "IdamServiceException Message");
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void test_ThrowRequiredFieldMissingException() {
        sut.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION,
                "RequiredFieldMissingException Message");
    }

    @Test(expected = UndefinedException.class)
    public void test_ThrowDefaultException() {
        sut.throwCustomRuntimeException(ExceptionType.UNDEFINDEDEXCEPTION, "ExceptionNotFound Message");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void test_OverloadedException() {
        sut.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, "ResourceNotFoundException Message",
                HttpStatus.ACCEPTED);
    }

    @Test(expected = ErrorPersistingException.class)
    public void test_ErrorPersistingException() {
        sut.throwCustomRuntimeException(ExceptionType.ERRORPERSISTINGEXCEPTION, "Error while persisting user profile",
                HttpStatus.ACCEPTED);
    }

    @Test(expected = InvalidRequest.class)
    public void test_BadRequestException() {
        sut.throwCustomRuntimeException(ExceptionType.BADREQUEST, "Bad request", HttpStatus.BAD_REQUEST);
    }

    @Test(expected = HttpClientErrorException.class)
    public void test_TooManyRequestException() {
        sut.throwCustomRuntimeException(ExceptionType.TOOMANYREQUESTS, "too many request", HttpStatus.ACCEPTED);
    }

}
