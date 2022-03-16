import org.ganemo.BuildImages

def call(Map config = [:]) {


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

    } else if(config.stage.toString() == "prod") {

        def tagname
        def tagname_sanitized
        def tagname_for_github

        pipeline{

            agent any

                options {
                    buildDiscarder(logRotator(numToKeepStr: '5')) 
                }

            environment { 
                registry = "odoopartners/$config.dockerhub_repo_name:$config.prod_deploy_name" 
                    registryCredential = 'dockerhubtestid' 
                    dockerImage = '' 
            }

            stages{

                stage("Validating Tag"){
                    steps{
                        script{
                            (tagname_sanitized, tagname, tagname_for_github) = utils.validateTag(git_credentials:"odoopartnersid",registryCredential:"${registryCredential}",registry:"${registry}",BRANCH_NAME:"${config.prod_deploy_name}",repo_name:"${config.dockerhub_repo_name}")
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
                            dockerImage = utils.buildImage(odoo_version:"${config.odoo_version}",registryCredential:"${registryCredential}",tagname_sanitized:"${tagname_sanitized}")
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
                            utils.cleanUp "${tagname}"
                        }
                    }
                }

                stage("Creating and Pushing Tag for GitHub repository"){
                    steps{
                        script{
                            utils.createTags(git_credentials:"odoopartnersid",tagname_for_github:"${tagname_for_github}")
                        }
                    }
                }

                stage("Updating Image for deployment"){
                    steps{
                        script{
                            utils.updateImageDeployment(BRANCH_NAME:"${config.prod_deploy_name}",k8s_credentials:"34.197.227.39",ip_from_master_node:"34.197.227.39",tagname_for_github:"${tagname_for_github}",repo_name:"${config.dockerhub_repo_name}")
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


    } 

}
