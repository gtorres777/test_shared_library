terraform {

  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 4.0"
    }

    dockerhub = {
      source = "magenta-aps/dockerhub"
      version = "0.0.14"
    }

    jenkins = {
      source = "taiidani/jenkins"
      version = "0.9.0"
    }
  }

}


# GITHUB

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


# DOCKERHUB

provider "dockerhub" {
  username = var.dockerhub_user
  password = var.dockerhub_token
}

resource "dockerhub_repository" "project" {
  name        = var.customer_name
  namespace   = "odoopartners"
  description = "Client ${var.customer_name} repository"
  private = true
}


# JENKINS

provider "jenkins" {
  server_url = "https://jenkins-prod.ganemo.co/"
  username = var.jenkins_user
  password = var.jenkins_token
}

data "jenkins_folder" "customers" {
    name = "CUSTOMER_PIPELINES"
}

resource "jenkins_job" "customer_job" {
  name     = "${var.customer_name}-${var.app_name}"
  folder   = data.jenkins_folder.customers.id
  template = templatefile("job.xml", {
    description = "Customer ${var.customer_name} multibranch pipeline"
  })
}

