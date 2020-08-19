package uk.gov.hmcts.reform.userprofileapi.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    public IdamRepository(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    public UserInfo getUserInfo(String jwtToken) {
        log.info("{}:: Inside getUserInfo::", loggingComponentName);
        return idamClient.getUserInfo("Bearer " + jwtToken);
    }

}
