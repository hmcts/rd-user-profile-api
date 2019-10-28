package uk.gov.hmcts.reform.userprofileapi.domain.enums;

public enum RegEx {
    EMAIL("^.*[@].*[.].*$");

    private final String content;

    RegEx(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
