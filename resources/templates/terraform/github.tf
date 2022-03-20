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

resource "github_repository" "customer_name" {
  name        = var.customer_name
  description = "Repository created with terraform to test customers"

  visibility = "public"
  auto_init   = true

}
resource "github_branch" "prodbranch" {
  repository = github_repository.customer_name.name
  branch     = var.odoo_version
}

resource "github_branch" "testbranch" {
  repository = github_repository.customer_name.name
  branch     = var.test_branch
}

resource "github_repository_file" "jenkinsfile-prod" {
  repository          = github_repository.customer_name.name
  branch              = github_branch.prodbranch.branch
  file                = "Jenkinsfile"
  content             = file("Jenkinsfile.prod")
  commit_message      = "Jenkinsfile for prod"
  overwrite_on_create = true
}

resource "github_repository_file" "jenkinsfile-test" {
  repository          = github_repository.customer_name.name
  branch              = github_branch.testbranch.branch
  file                = "Jenkinsfile"
  content             = file("Jenkinsfile.test")
  commit_message      = "Jenkinsfile for test"
  overwrite_on_create = true
}

resource "github_repository_file" "dockerfile-prod" {
  repository          = github_repository.customer_name.name
  branch              = github_branch.prodbranch.branch
  file                = "Dockerfile"
  content             = file("Dockerfile")
  commit_message      = "Dockerfile for test and prod"
  overwrite_on_create = true
}


resource "github_repository_file" "dockerfile-test" {
  repository          = github_repository.customer_name.name
  branch              = github_branch.testbranch.branch
  file                = "Dockerfile"
  content             = file("Dockerfile")
  commit_message      = "Dockerfile for test and prod"
  overwrite_on_create = true
}

resource "github_repository_file" "repos-prod" {
  repository          = github_repository.customer_name.name
  branch              = github_branch.prodbranch.branch
  file                = "repos.csv"
  content             = file("repos.csv")
  commit_message      = "repos for test and prod"
  overwrite_on_create = true
}


resource "github_repository_file" "repos-test" {
  repository          = github_repository.customer_name.name
  branch              = github_branch.testbranch.branch
  file                = "repos.csv"
  content             = file("repos.csv")
  commit_message      = "repos for test and prod"
  overwrite_on_create = true
}

