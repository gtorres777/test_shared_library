variable "github_token" {
  description = "Github token to access repo."
}

variable "dockerhub_token" {
  description = "Dockerhub token to access repo."
}

variable "jenkins_token" {
  description = "Jenkins token to access server."
}

variable "dockerhub_user" {
  description = "Dockerhub user to access repo."
}

variable "jenkins_user" {
  description = "Jenkins user to access server."
}

variable "customer_name" {
  description = "Customer deployment name."
}

variable "odoo_version" {
  description = "Odoo version for the customer."
}

variable "test_branch" {
  description = "Name for the test branch in the github repository."
}

variable "app_name" {
  description = "Odoo version for the customer."
}
