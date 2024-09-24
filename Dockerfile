ARG APP_INSIGHTS_AGENT_VERSION=3.5.2
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rd-user-profile-api.jar /opt/app/

EXPOSE 8091

CMD [ "rd-user-profile-api.jar" ]
