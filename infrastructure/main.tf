# Temporary fix for template API version error on deployment
provider "azurerm" {
  version = "1.19.0"
}

locals {
  preview_app_service_plan = "${var.product}-${var.component}-${var.env}"
  non_preview_app_service_plan = "${var.product}-${var.env}"
  app_service_plan = "${var.env == "preview" || var.env == "spreview" ? local.preview_app_service_plan : local.non_preview_app_service_plan}"

  preview_vault_name = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name = "${var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name}"
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

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "s2s-secret"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  name = "s2s-microservice"
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

data "azurerm_key_vault_secret" "db_up_username" {
  name = "db-up-username"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "db_up_password" {
  name = "db-up-password"
  vault_uri = "${data.azurerm_key_vault.rd_key_vault.vault_uri}"
}

module "db-sys-ref-data" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-${var.component}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
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

    DB_UP_HOST = "${module.db-sys-ref-data.host_name}"
    DB_UP_PORT = "${module.db-sys-ref-data.postgresql_listen_port}"
    DB_UP_DATABASE = "${module.db-sys-ref-data.postgresql_database}"
    DB_UP_USER = "${module.db-sys-ref-data.user_name}"
    DB_UP_USERNAME = "${module.db-sys-ref-data.user_name}"
    DB_UP_PASSWORD = "${module.db-sys-ref-data.postgresql_password}"
    DB_UP_CONNECTION_OPTIONS = "?"

    IA_S2S_SECRET = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    IA_S2S_MICROSERVICE = "${data.azurerm_key_vault_secret.s2s_microservice.value}"

    IDAM_URL = "${data.azurerm_key_vault_secret.idam_url.value}"
    S2S_URL = "${data.azurerm_key_vault_secret.s2s_url.value}"

    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_IA = "${var.log_level_rd}"
    EXCEPTION_LENGTH = 100
  }
}
