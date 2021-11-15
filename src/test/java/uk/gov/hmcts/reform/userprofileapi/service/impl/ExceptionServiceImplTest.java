package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.exception.ErrorPersistingException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.exception.UndefinedException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ExceptionServiceImplTest {

    private final ExceptionServiceImpl sut = new ExceptionServiceImpl();

    @Test
    void testThrowRuntimeException() {
        assertThrows(ResourceNotFoundException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION,
                        "ResourceNotFoundException Message")
        );
    }

    @Test
    void testThrowIdamServiceException() {
        assertThrows(IdamServiceException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.IDAMSERVICEEXCEPTION, "IdamServiceException Message")
        );
    }

    @Test
    void test_ThrowRequiredFieldMissingException() {
        assertThrows(RequiredFieldMissingException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION,
                        "RequiredFieldMissingException Message")
        );
    }

    @Test
    void test_ThrowDefaultException() {
        assertThrows(UndefinedException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.UNDEFINDEDEXCEPTION, "ExceptionNotFound Message")
        );
    }

    @Test
    void test_OverloadedException() {
        assertThrows(ResourceNotFoundException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION,
                        "ResourceNotFoundException Message",
                        HttpStatus.ACCEPTED)
        );
    }

    @Test
    void test_ErrorPersistingException() {
        assertThrows(ErrorPersistingException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.ERRORPERSISTINGEXCEPTION,
                        "Error while persisting user profile",
                        HttpStatus.ACCEPTED)
        );
    }

    @Test
    void test_BadRequestException() {
        assertThrows(InvalidRequest.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.BADREQUEST, "Bad request", HttpStatus.BAD_REQUEST)
        );
    }

    @Test
    void test_TooManyRequestException() {
        assertThrows(HttpClientErrorException.class, () ->
                sut.throwCustomRuntimeException(ExceptionType.TOOMANYREQUESTS, "too many request", HttpStatus.ACCEPTED)
        );
    }
}
