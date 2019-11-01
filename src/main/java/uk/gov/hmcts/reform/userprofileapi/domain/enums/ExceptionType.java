package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import lombok.Getter;

@Getter
public enum ExceptionType {
    RESOURCENOTFOUNDEXCEPTION("ResourceNotFoundException"),
    REQUIREDFIELDMISSINGEXCEPTION("RequiredFieldMissingException"),
    IDAMSERVICEEXCEPTION("IdamServiceException"),
    UNDEFINDEDEXCEPTION("UndefinedException");

    private String content;

    ExceptionType(String content) {
        this.content = content;
    }


}
