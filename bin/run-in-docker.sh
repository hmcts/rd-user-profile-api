#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true

# Test S2S key - not used in any HMCTS key vaults or services
export S2S_SECRET=SZDUA3L7N32PE2IS
export S2S_MICROSERVICE=rd_user_profile_api

build_s2s_image() {
    git clone git@github.com:hmcts/s2s-test-tool.git
    cd s2s-test-tool
    git checkout allow-all-microservices
    ./gradlew build
    docker build -t hmcts/service-token-provider .
    #cd .. && rm -rf s2s-test-tool
}

clean_old_docker_artifacts() {
    docker stop rd-user-profile-api
    docker stop rd-user-profile-db
    docker stop service-token-provider

    docker rm rd-user-profile-api
    docker rm rd-user-profile-db
    docker rm service-token-provider

    docker rmi hmcts/rd-user-profile-api
    docker rmi hmcts/rd-user-profile-db
    docker rmi hmcts/service-token-provider

    docker volume rm rd-user-profile-api_rd-user-profile-db-volume
}

execute_script() {

  clean_old_docker_artifacts

  build_s2s_image

  cd $(dirname "$0")/..

  ./gradlew clean assemble

  export SERVER_PORT="${SERVER_PORT:-8091}"

  chmod +x bin/*

  docker-compose up
}

execute_script