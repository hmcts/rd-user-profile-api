package uk.gov.hmcts.reform.userprofileapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
