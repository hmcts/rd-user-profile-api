package uk.gov.hmcts.reform.userprofileapi.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@Configuration
public class IdamStatusConfiguration {

    @Bean
    public Map<Map<String, Boolean>, IdamStatus> idamStatusResolverMap() {
        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false,true, false), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false,false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(true, false,true), IdamStatus.ACTIVE_AND_LOCKED);
        idamStatusMap.put(addRule(false,false,false), IdamStatus.SUSPENDED);
        idamStatusMap.put(addRule(false,false,true), IdamStatus.SUSPENDED_AND_LOCKED);
        return idamStatusMap;
    }

    public Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag, boolean lockedFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        pendingMapWithRules.put("LOCKED", lockedFlag);
        return pendingMapWithRules;
    }


}
