def call(Map config = [:]) { 
  def scriptcontents = libraryResource "templates/pipelines/${config.odoo_version}/${config.name}"    
  writeFile file: "${config.name}", text: scriptcontents 
} 
