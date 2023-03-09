package uk.gov.hmcts.reform.userprofileapi.docs;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.integration.AuthorizationEnabledIntegrationTest;

import java.io.File;
import java.io.FileOutputStream;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Built-in feature which saves service's swagger specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */
@WithTags({@WithTag("testType:Integration")})
class SwaggerPublisherTest extends AuthorizationEnabledIntegrationTest {

    private static final Logger LOG = getLogger(SwaggerPublisherTest.class);

    private static final String SWAGGER_DOC_JSON_FILE = "/tmp/swagger-specs.json";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        WebRequestTrackingFilter filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        this.mockMvc = webAppContextSetup(webApplicationContext)
            .addFilter(filter)
            .build();
    }

    @Test
    void shouldGenerateDocs() throws Exception {

        LOG.info("Generating Swagger Docs");

        File linuxTmpDir = new File("/tmp");
        if (!linuxTmpDir.exists()) {
            return;
        }

        byte[] specs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        try (FileOutputStream outputStream = new FileOutputStream(SWAGGER_DOC_JSON_FILE)) {
            outputStream.write(specs);
        }

        LOG.info("Completed Generating Swagger docs to the following location {}",
                SWAGGER_DOC_JSON_FILE);
    }

}
