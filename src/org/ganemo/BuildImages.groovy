package org.ganemo

class BuildImages implements Serializable {
  
  def steps
  
  BuildImages(steps) {
    this.steps = steps
  }
  
  def mvn(args) {
    steps.echo "${args}"
  }
  
  def validateTag(Map config = [:]) {
    steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.credentials}",
                                        gitToolName: 'git-tool')]) {
      steps.sh "git fetch --all --tags"
    }
    
    config.existing_tags_github_repository = steps.sh (
            script: 'git tag',
            returnStdout: true
            ).replaceAll('\n', ', ')


    return config.existing_tags_github_repository


  }
  
  def cloneRepositories(credentials) {
    steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${credentials}",
                                        gitToolName: 'git-tool')]) {
      steps.sh "chmod +x scripts/clone_repositories.sh" 
      steps.sh "./scripts/clone_repositories.sh 15-dev" 
    }
  
  }
  
}
