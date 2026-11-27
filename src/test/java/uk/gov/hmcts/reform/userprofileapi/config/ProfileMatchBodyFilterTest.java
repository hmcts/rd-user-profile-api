package uk.gov.hmcts.reform.userprofileapi.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileMatchBodyFilterTest {
    private final ProfileMatchBodyFilter filter = new ProfileMatchBodyFilter();

    @Test
    void rejectsOversizedDeclaredAndChunkedBodies() throws Exception {
        for (boolean chunked : new boolean[]{false, true}) {
            var request = new MockHttpServletRequest() {
                @Override
                public long getContentLengthLong() {
                    return chunked ? -1 : super.getContentLengthLong();
                }
            };
            request.setContent(new byte[ProfileMatchBodyFilter.MAX_BODY_BYTES + 1]);
            var response = new MockHttpServletResponse();
            var called = new AtomicBoolean();
            filter.doFilter(request, response, (req, res) -> called.set(true));
            assertThat(response.getStatus()).isEqualTo(413);
            assertThat(called).isFalse();
        }
    }

    @Test
    void preservesAcceptedBodyForJsonParsing() throws Exception {
        var request = new MockHttpServletRequest();
        request.setContent("{\"userIds\":[]}".getBytes(StandardCharsets.UTF_8));
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> assertThat(
                ((HttpServletRequest) req).getInputStream().readAllBytes()).isEqualTo(request.getContentAsByteArray()));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void acceptsExactBodyLimit() throws Exception {
        var request = new MockHttpServletRequest();
        request.setContent(new byte[ProfileMatchBodyFilter.MAX_BODY_BYTES]);
        var called = new AtomicBoolean();
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> called.set(true));
        assertThat(called).isTrue();
    }
}
