package org.ganemo

class BuildImages implements Serializable {
  
  def steps
  
  BuildImages(steps) {
    this.steps = steps
  }
  
  def validateTag(Map config = [:]) {

      def existing_tags_dockerhub_repository
      def existing_tags_github_repository

      steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.git_credentials}",
                  gitToolName: 'git-tool')]) {
          steps.sh "git fetch --all --tags"
      }

      existing_tags_github_repository = steps.sh (
              script: 'git tag',
              returnStdout: true
              ).replaceAll('\n', ', ')

      steps.withCredentials([steps.usernamePassword(credentialsId: "${config.registryCredential}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          existing_tags_dockerhub_repository = steps.sh (
                  script: ''' wget -q --user $USERNAME --password $PASSWORD https://registry.hub.docker.com/v1/repositories/odoopartners/odoo/tags -O -  | sed -e 's/[][]//g' -e 's/"//g' -e 's/ //g' | tr '}' '\n'  | awk -F: '{print $3}' ''',
                  returnStdout: true
                  ).replaceAll('\n', ', ')
      }

      List<String> list_existing_tags_github = Arrays.asList(existing_tags_github_repository.split("\\s*,\\s*"))

      List<String> list_existing_tags_docker = Arrays.asList(existing_tags_dockerhub_repository.split("\\s*,\\s*"))


      def exists = steps.fileExists "${config.file_name}"
      def current_version = steps.readFile "${config.file_name}"


      if (exists) {
          steps.echo 'VERSION --> '+current_version
      } else {
          steps.echo 'The file version does not exist'
      }


      def tagname = "${config.registry}"+"-$current_version"                     
      def tagname_sanitized = tagname.trim()

      def tagname_for_github = "${config.BRANCH_NAME}-$current_version"

      if (tagname_for_github.trim() in list_existing_tags_github) {
          steps.error("Build failed because of the tagname for GitHub already exists in the repository")
      } else {
          steps.echo 'New Tag for Github Repository ---> '+ tagname_for_github
      }

      if (tagname_for_github.trim() in list_existing_tags_docker) {
          steps.error("Build failed because of the tagname for DockerHub already exists in the repository")
      } else {
          steps.echo 'New Tag for DockerHub Repository ---> '+ tagname_for_github
      }

      return [tagname_sanitized, tagname, tagname_for_github]


  }
  
  def cloneRepositories(Map config = [:]) {
    steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.git_credentials}",
                                        gitToolName: 'git-tool')]) {
      steps.sh "chmod +x scripts/clone_repositories.sh" 
      steps.sh "./scripts/clone_repositories.sh ${config.branch_to_clone}" 
    }
  
  }
  
}
