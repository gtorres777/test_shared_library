package org.ganemo

class BuildImages implements Serializable {
  def steps
  BuildImages(steps) {this.steps = steps}
  def mvn(args) {
    echo "${args}"
  }
}
