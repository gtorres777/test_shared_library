terraform {
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 4.0"
    }
  }
}

# Configure the GitHub Provider
provider "github" {
  token = var.github_token
}
data "github_repository" "customer_name" {
    full_name = "gtorres777/${var.customer_name}"
}

output "gitname" {
  value = data.github_repository.customer_name
}

resource "github_repository_file" "terraform_state" {
  repository          = data.github_repository.customer_name.name
  branch              = var.odoo_version
  file                = "state_files/terraform.tfstate"
  content             = file("../terraform.tfstate")
  commit_message      = "terraform state file"
  overwrite_on_create = true

}

resource "github_repository_file" "terraform_state_lock" {

  repository          = data.github_repository.customer_name.name
  branch              = var.odoo_version
  file                = "state_files/.terraform.lock.hcl"
  content             = file("../.terraform.lock.hcl")
  commit_message      = "terraform state file"
  overwrite_on_create = true

}


