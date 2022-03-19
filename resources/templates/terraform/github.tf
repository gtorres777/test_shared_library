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
    token = "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"
}

resource "github_repository" "test_customer" {
  name        = "test_customer"
  description = "Repository created with terraform to test customers"

  visibility = "public"
  auto_init   = true

}
resource "github_branch" "prodbranch" {
  repository = github_repository.test_customer.name
  branch     = "13.0"
}

resource "github_branch" "testbranch" {
  repository = github_repository.test_customer.name
  branch     = "13-dev"
}

resource "github_repository_file" "jenkinsfile-prod" {
  repository          = github_repository.test_customer.name
  branch              = github_branch.prodbranch.branch
  file                = "Jenkinsfile"
  content             = file("Jenkinsfile.prod")
  commit_message      = "Jenkinsfile for prod"
  overwrite_on_create = true
}

resource "github_repository_file" "jenkinsfile-test" {
  repository          = github_repository.test_customer.name
  branch              = github_branch.testbranch.branch
  file                = "Jenkinsfile"
  content             = file("Jenkinsfile.test")
  commit_message      = "Jenkinsfile for test"
  overwrite_on_create = true
}

resource "github_repository_file" "dockerfile-prod" {
  repository          = github_repository.test_customer.name
  branch              = github_branch.prodbranch.branch
  file                = "Dockerfile"
  content             = file("Dockerfile")
  commit_message      = "Dockerfile for test and prod"
  overwrite_on_create = true
}


resource "github_repository_file" "dockerfile-test" {
  repository          = github_repository.test_customer.name
  branch              = github_branch.testbranch.branch
  file                = "Dockerfile"
  content             = file("Dockerfile")
  commit_message      = "Dockerfile for test and prod"
  overwrite_on_create = true
}

resource "github_repository_file" "repos-prod" {
  repository          = github_repository.test_customer.name
  branch              = github_branch.prodbranch.branch
  file                = "repos.csv"
  content             = file("repos.csv")
  commit_message      = "repos for test and prod"
  overwrite_on_create = true
}


resource "github_repository_file" "repos-test" {
  repository          = github_repository.test_customer.name
  branch              = github_branch.testbranch.branch
  file                = "repos.csv"
  content             = file("repos.csv")
  commit_message      = "repos for test and prod"
  overwrite_on_create = true
}
