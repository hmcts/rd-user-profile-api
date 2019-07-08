package uk.gov.hmcts.reform.userprofileapi.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
