locals {
  preview_vault_name      = join("-", [var.raw_product, "aat"])
  non_preview_vault_name  = join("-", [var.raw_product, var.env])
  key_vault_name          = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name
}

resource "azurerm_resource_group" "rg" {
  name      = join("-", [var.raw_product, var.component, var.env])
  location  = var.location
  tags      = {
      "Deployment Environment"  = var.env
      "Team Name"               = var.team_name
      "lastUpdated"             = timestamp()
    }
}

data "azurerm_key_vault" "rd_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name          = join("-", [var.component, "POSTGRES-USER"])
  value         = module.db-user-profile.user_name
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name          = join("-", [var.component, "POSTGRES-PASS"])
  value         = module.db-user-profile.postgresql_password
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name          = join("-", [var.component, "POSTGRES-HOST"])
  value         = module.db-user-profile.host_name
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name          = join("-", [var.component, "POSTGRES-PORT"])
  value         = "5432"
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name          = join("-", [var.component, "POSTGRES-DATABASE"])
  value         = module.db-user-profile.postgresql_database
  key_vault_id  = data.azurerm_key_vault.rd_key_vault.id
}

module "db-user-profile" {
  source            = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product           = join("-", [var.product, var.component, "postgres-db"])
  location          = var.location
  subscription      = var.subscription
  env               = var.env
  postgresql_user   = "dbuserprofile"
  database_name     = "dbuserprofile"
  common_tags       = var.common_tags
}