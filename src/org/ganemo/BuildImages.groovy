package org.ganemo

class BuildImages implements Serializable {
  def steps
  BuildImages(steps) {this.steps = steps}
  
  def mvn(args) {
    steps.echo "${args}"
  }
  
  def aea() {
    steps.echo "HOLAAA"
  }
  
  def cloneRepositories() {
    withCredentials([gitUsernamePassword(credentialsId: 'odoopartnersid',
                                        gitToolName: 'git-tool')]) {
      steps.sh "chmod +x scripts/clone_repositories.sh" 
      steps.sh "./scripts/clone_repositories.sh 15-dev" 
    }
  
  }
  
}

