package uk.gov.hmcts.reform.userprofileapi.idam;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.userprofileapi.idam.IdamApi.CreateUserRequest;
import static uk.gov.hmcts.reform.userprofileapi.idam.IdamApi.CreateUserRequest.*;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@Service
@Slf4j
public class IdamServiceForFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(IdamServiceForFunctional.class);

    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE = "code";
    public static final String BASIC = "Basic ";

    private final IdamApi idamApi;
    private final TestConfigProperties testConfig;

    @Autowired
    public IdamServiceForFunctional(TestConfigProperties testConfig) {
        this.testConfig = testConfig;

        idamApi = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(IdamApi.class, testConfig.getIdamApiUrl());
    }


    public String createUserWith(String userGroup, String... roles) {
        String email = nextUserEmail();
        CreateUserRequest userRequest = userRequest(email, userGroup, roles);
        idamApi.createUser(userRequest);
        return email;
    }

    private CreateUserRequest userRequest(String email, String userGroup, String[] roles) {
        return userRequestWith()
                .email(email)
                .password(testConfig.getTestUserPassword())
                .roles(Stream.of(roles)
                        .map(IdamApi.Role::new)
                        .collect(toList()))
                .userGroup(new IdamApi.UserGroup(userGroup))
                .build();
    }

    private String nextUserEmail() {
        return String.format(testConfig.getGeneratedUserEmailPattern(), RandomStringUtils.randomAlphanumeric(10));
    }
}