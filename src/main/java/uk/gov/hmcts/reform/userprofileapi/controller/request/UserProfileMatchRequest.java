package uk.gov.hmcts.reform.userprofileapi.controller.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = UserProfileMatchRequest.Deserializer.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileMatchRequest {
    @NotNull
    @Size(max = 1000)
    private List<@NotBlank @Size(max = 50) String> userIds;

    @NotNull
    private String fullNameContains;

    public static class Deserializer extends JsonDeserializer<UserProfileMatchRequest> {
        private static final String INVALID_REQUEST = "Invalid profile match request";

        @Override
        public UserProfileMatchRequest deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            JsonNode body = parser.readValueAsTree();
            JsonNode identifiers = body.path("userIds");
            JsonNode term = body.path("fullNameContains");
            if (parser.nextToken() != null || !body.isObject() || !identifiers.isArray()
                    || identifiers.size() > 1000 || !term.isTextual()) {
                throw JsonMappingException.from(parser, INVALID_REQUEST);
            }
            var ids = new ArrayList<String>();
            for (JsonNode id : identifiers) {
                if (!id.isTextual()) {
                    throw JsonMappingException.from(parser, INVALID_REQUEST);
                }
                ids.add(id.textValue());
            }
            return new UserProfileMatchRequest(ids, term.textValue());
        }
    }
}
