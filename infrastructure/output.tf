# output "resourceGroup" {
#   value = "${azurerm_resource_group.rg.name}"
# }

# output "appServicePlan" {
#   value = "${local.app_service_plan}"
# }

output "vaultUri" {
  value = "${local.s2s_vault_uri}"
}
