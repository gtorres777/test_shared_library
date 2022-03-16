def call(Map config = [:]) {

    import org.ganemo.BuildImages

    def utils = new BuildImages(this)

    if(config.stage.toString() == "test"){

        def build_tag
        def tag_for_test

        pipeline{
            
            agent any
            
            options {
                buildDiscarder(logRotator(numToKeepStr: '5')) 
            }
            
            environment { 
                registry = "odoopartners/$config.dockerhub_repo_name:$config.test_deploy_name" 
                registryCredential = 'dockerhubtestid' 
                dockerImage = '' 
            }
            
            stages{
                stage("Obtaining production version Tag"){
                    steps{
                        script{
                            (build_tag, tag_for_test) = utils.getTagFromProduction(git_credentials:"odoopartnersid",registry:"${registry}",prod_deploy_name:"${config.prod_deploy_name}",deploy_name:"${config.test_deploy_name}")
                        }
                    }
                }
                
                stage("Cloning Repositories"){
                    steps{
                        script{
                            utils.cloneRepositories(git_credentials:"odoopartnersid",branch_to_clone:"${BRANCH_NAME}")
                        }
                    }
                }
                
                stage("Building Image"){
                    steps{
                        script{
                            dockerImage = utils.buildImage(odoo_version:"${config.odoo_version}",registryCredential:"${registryCredential}",tagname_sanitized:"${build_tag}")
                        }
                    }
                }
                
                stage("Publishing Image to Docker Hub"){
                    steps{
                        script{
                            utils.publishImage(registryCredential:"${registryCredential}")
                        }
                    }
                }
                
                stage("Cleaning Up"){
                    steps{
                        script{
                            utils.cleanUp "${build_tag}"
                        }
                    }
                }
              
                stage("Updating Image for deployment for Development"){
                    steps{
                        script{
                            utils.updateImageForDev(build_tag:"${build_tag}",deploy_name:"${config.test_deploy_name}",tag_for_test:"${tag_for_test}",repo_name:"${config.dockerhub_repo_name}")
                        }
                    }
                }     
                
            }
            
            post {
                always {
                    deleteDir()
                }
            }
            
        }

    } else {
        print("PROD")

    } 

}
