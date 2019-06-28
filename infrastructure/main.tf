# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.22.0"
}

locals {
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  preview_app_service_plan = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"

  s2s_url = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  s2s_vault_name = "s2s-${local.local_env}"
  s2s_vault_uri = "https://s2s-${local.local_env}.vault.azure.net/"
}

resource "azurerm_resource_group" "rg" {
  name = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
}

data "azurerm_key_vault" "rd_key_vault" {
  name = "${local.key_vault_name}"
  resource_group_name = "${local.key_vault_name}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "up_s2s_secret" {
  name = "up-s2s-secret"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "up_s2s_microservice" {
  name = "up-s2s-microservice"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_url" {
  name = "idam-url"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_url" {
  name = "s2s-url"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "DB_UP_USERNAME" {
  name      = "${var.component}-POSTGRES-USER"
  value     = "${module.db-user-profile.user_name}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "DB_UP_PASSWORD" {
  name      = "${var.component}-POSTGRES-PASS"
  value     = "${module.db-user-profile.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "DB_UP_POSTGRES_HOST" {
  name      = "${var.component}-POSTGRES-HOST"
  value     = "${module.db-user-profile.host_name}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "DB_UP_POSTGRES_PORT" {
  name      = "${var.component}-POSTGRES-PORT"
  value     = "5432"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

resource "azurerm_key_vault_secret" "DB_UP_POSTGRES_DATABASE" {
  name      = "${var.component}-POSTGRES-DATABASE"
  value     = "${module.db-user-profile.postgresql_database}"
  key_vault_id = "${data.azurerm_key_vault.rd_key_vault.id}"
}

module "db-user-profile" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-${var.component}-postgres-db"
  location = "${var.location}"
  subscription = "${var.subscription}"
  env = "${var.env}"
  postgresql_user = "dbuserprofile"
  database_name = "dbuserprofile"
  common_tags = "${var.common_tags}"

}

module "rd-user-profile-api" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${var.product}-${var.component}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription = "${var.subscription}"
  capacity = "${var.capacity}"
  instance_size = "${var.instance_size}"
  common_tags = "${merge(var.common_tags, map("lastUpdated", "${timestamp()}"))}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  asp_name = "${local.app_service_plan}"
  asp_rg = "${local.app_service_plan}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE = false

    DB_UP_POSTGRES_HOST = "${module.db-user-profile.host_name}"
    DB_UP_POSTGRES_PORT = "${module.db-user-profile.postgresql_listen_port}"
    DB_UP_POSTGRES_DATABASE = "${module.db-user-profile.postgresql_database}"
    DB_UP_USER = "${module.db-user-profile.user_name}"
    DB_UP_USERNAME = "${module.db-user-profile.user_name}"
    DB_UP_PASSWORD = "${module.db-user-profile.postgresql_password}"
    DB_UP_CONNECTION_OPTIONS = "?"

    UP_S2S_SECRET = "${data.azurerm_key_vault_secret.up_s2s_secret.value}"
    UP_S2S_MICROSERVICE = "${data.azurerm_key_vault_secret.up_s2s_microservice.value}"

    IDAM_URL = "${data.azurerm_key_vault_secret.idam_url.value}"
    S2S_URL = "${data.azurerm_key_vault_secret.s2s_url.value}"

    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_IA = "${var.log_level_rd}"
    EXCEPTION_LENGTH = 100
  }
}
