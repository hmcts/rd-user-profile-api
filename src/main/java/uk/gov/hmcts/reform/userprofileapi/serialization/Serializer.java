package uk.gov.hmcts.reform.userprofileapi.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
