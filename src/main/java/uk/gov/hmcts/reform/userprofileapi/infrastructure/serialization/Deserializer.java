package uk.gov.hmcts.reform.userprofileapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
