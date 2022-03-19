def call(Map config = [:]) { 
  def scriptcontents = libraryResource "templates/terraform/${config.name}"    
  writeFile file: "${config.name}", text: scriptcontents 
} 
