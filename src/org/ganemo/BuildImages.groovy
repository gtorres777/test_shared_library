package org.ganemo

class BuildImages implements Serializable {
  def steps
  Utilities(steps) {this.steps = steps}
  def mvn(args) {
    steps.echo "args"
  }
}
