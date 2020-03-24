package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import lombok.Getter;

@Getter
public enum ExceptionType {
    RESOURCENOTFOUNDEXCEPTION("ResourceNotFoundException"),
    REQUIREDFIELDMISSINGEXCEPTION("RequiredFieldMissingException"),
    IDAMSERVICEEXCEPTION("IdamServiceException"),
    UNDEFINDEDEXCEPTION("UndefinedException"),
    ERRORPERSISTINGEXCEPTION("PersistingException"),
    BADREQUEST("BadRequest"),
    TOOMANYREQUESTS("TooManyRequestException");

    private String content;

    ExceptionType(String content) {
        this.content = content;
    }


}
