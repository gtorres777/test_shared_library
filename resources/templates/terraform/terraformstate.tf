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
data "github_repository" "terraform_customers" {
    full_name = "gtorres777/terraform_customers"
}

resource "github_repository_file" "terraform_state" {
  repository          = data.github_repository.terraform_customers.name
  branch              = "main"
  file                = "terraform.tfstate.d/${var.customer_name}-${var.app_name}/terraform.tfstate"
  content             = file("previoustfstate")
  commit_message      = "terraform state file"
  overwrite_on_create = true
}
