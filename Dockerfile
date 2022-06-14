ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/rd-user-profile-api.jar /opt/app/

EXPOSE 8091

CMD [ "rd-user-profile-api.jar" ]