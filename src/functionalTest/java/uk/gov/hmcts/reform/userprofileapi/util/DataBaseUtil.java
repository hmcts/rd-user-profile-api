package uk.gov.hmcts.reform.userprofileapi.util;

import static java.lang.System.getenv;
import static java.util.Objects.isNull;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.config.DbConfig;
import java.sql.Connection;

@Slf4j
@Component
public class DataBaseUtil {

    @Autowired
    DbConfig dbConfig;

    private DataSource dataSource;

    public DataSource getDataSource() {
        if (isNull(dataSource)) {
            try {
                DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
                dataSourceBuilder.driverClassName("org.postgresql.Driver");
                dataSourceBuilder.url(String.format("jdbc:postgresql://%s:%s/%s", dbConfig.getPostgresHost(),
                        dbConfig.getPostgresPort(), dbConfig.getPostgresDbName()));
                dataSourceBuilder.username(dbConfig.getPostgresUserName());
                dataSourceBuilder.password(dbConfig.getPostgresPassword());
                dataSource = dataSourceBuilder.build();
                log.info("datasource created");
            } catch (Exception e) {
                log.error("Unable to connect to database from functional test case : {}", e.getMessage());
            }
        }
        return dataSource;
    }

    @PreDestroy
    public void runDeleteScript() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(getenv("isNightlyBuild"))) {
            log.info("Delete test data script execution started");
            try {
                Connection connection = getDataSource().getConnection();
                connection.setAutoCommit(false);
                ScriptUtils.executeSqlScript(connection,
                        new EncodedResource(new ClassPathResource("delete-functional-test-data.sql")),
                        false, true,
                        ScriptUtils.DEFAULT_COMMENT_PREFIX,
                        ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
                log.info("Delete test data script execution completed");
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (Exception exe) {
                log.error("Delete test data script execution failed: {}", exe.getMessage());
            }
        } else {
            log.info("Not executing delete test data script");
        }
    }
}

