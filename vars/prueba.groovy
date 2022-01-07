def call() {
    
        def tagname
        def tagname_sanitized
        def tagname_for_github
        def existing_tags_github_repository
        def existing_tags_dockerhub_repository
        def basename


        pipeline { 
            environment { 
                registry = "odoopartners/odoo:$BRANCH_NAME" 
                    registryCredential = 'dockerhubtestid' 
                    dockerImage = '' 
            }
            agent any 
                stages { 

                    stage('Validating Tag ') { 
                        steps { 
                            withCredentials([gitUsernamePassword(credentialsId: 'odoopartnersid',
                                        gitToolName: 'git-tool')]) {
                                sh "git fetch --all --tags" 
                            }

                            script {
                                    existing_tags_github_repository = sh (
                                        script: 'git tag',
                                        returnStdout: true
                                        ).replaceAll('\n', ', ')

                                    withCredentials([usernamePassword(credentialsId: registryCredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                        existing_tags_dockerhub_repository = sh (
                                                script: ''' wget -q --user $USERNAME --password $PASSWORD https://registry.hub.docker.com/v1/repositories/odoopartners/odoo/tags -O -  | sed -e 's/[][]//g' -e 's/"//g' -e 's/ //g' | tr '}' '\n'  | awk -F: '{print $3}' ''',
                                                returnStdout: true
                                                ).replaceAll('\n', ', ')
                                    }

                                    List<String> list_existing_tags_github = Arrays.asList(existing_tags_github_repository.split("\\s*,\\s*"))

                                    List<String> list_existing_tags_docker = Arrays.asList(existing_tags_dockerhub_repository.split("\\s*,\\s*"))

                                    def exists = fileExists 'version'
                                    current_version = readFile 'version'


                                    if (exists) {
                                        echo 'VERSION --> '+current_version
                                    } else {
                                        echo 'The file version does not exist'
                                    }

                                    tagname = registry+"-$current_version"                     
                                    tagname_sanitized = tagname.trim()

                                    tagname_for_github = "$BRANCH_NAME-$current_version"

                                    if (tagname_for_github.trim() in list_existing_tags_github) {
                                        error("Build failed because of the tagname for GitHub already exists in the repository")
                                    } else {
                                        echo 'New Tag for Github Repository ---> '+ tagname_for_github
                                    }

                                    if (tagname_for_github.trim() in list_existing_tags_docker) {
                                        error("Build failed because of the tagname for DockerHub already exists in the repository")
                                    } else {
                                        echo 'New Tag for DockerHub Repository ---> '+ tagname_for_github
                                    }
                            }
                        }
                    } 

                    stage('Cloning our Repositories') { 
                        steps { 

                            withCredentials([gitUsernamePassword(credentialsId: 'odoopartnersid',
                                        gitToolName: 'git-tool')]) {
                                sh "chmod +x scripts/clone_repositories.sh" 
                                    sh "./scripts/clone_repositories.sh 15-dev" 
                            }
                        }
                    } 

                    stage('Building Image') { 
                        steps { 
                            script { 
                                docker.withRegistry( '', registryCredential ) { 
                                    dockerImage = docker.build(tagname_sanitized, ".") 
                                }

                            }
                        } 
                    }

                    stage('Publishing Image to Docker Hub') { 
                        steps { 
                            script { 
                                docker.withRegistry( '', registryCredential ) { 
                                    dockerImage.push() 
                                }
                            } 
                        }
                    } 
                    stage('Cleaning up') { 
                        steps { 
                            sh "docker rmi $tagname" 
                                sh "rm -rf repositories/"
                        }
                    } 

                    stage('Creating and Pushing Tag for GitHub repository') { 
                        steps { 
                            withCredentials([gitUsernamePassword(credentialsId: 'odoopartnersid',
                                        gitToolName: 'git-tool')]) {
                                sh "git tag $tagname_for_github" 
                                    sh "git push origin $tagname_for_github" 
                            }
                        }
                    } 

                    stage('Updating Image for deployment') { 
                        steps {

                            script {
                                def branch_original_name = "$BRANCH_NAME"
                                    basename = branch_original_name.substring(0, branch_original_name.lastIndexOf("-"))

                            }

                            sshagent(credentials: ['34.197.227.39']) {
                                sh """ 
                                    ssh -o StrictHostKeyChecking=no -l ubuntu 34.197.227.39 -A "kubectl -n odoo set image deployment/${BRANCH_NAME} odoo-${basename}=odoopartners/odoo:${tagname_for_github}" 
                                    """
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
