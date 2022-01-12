def call(Map config = [:]) { 
  def scriptcontents = libraryResource "scripts/${config.name}"    
  writeFile file: "${config.name}", text: scriptcontents 
} 
