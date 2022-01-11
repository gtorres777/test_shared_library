package org.ganemo

class BuildImages implements Serializable {
  
  def steps
  
  BuildImages(steps) {
    this.steps = steps
  }
  
  def mvn(args) {
    steps.echo "${args}"
  }
  
  def aea(args) {
    steps.echo "HOLAAA ${args}"
  }
  
  
  
}
