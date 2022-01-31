package org.ganemo

class BuildImages implements Serializable {

    def steps

    BuildImages(steps) {
      this.steps = steps
    }

    def validateTag(Map config = [:]) {

        def existing_tags_dockerhub_repository
        def existing_tags_github_repository
        
        def current_version

        steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.git_credentials}",
                    gitToolName: 'git-tool')]) {
            steps.sh "git fetch --all --tags"
        }

        existing_tags_github_repository = steps.sh (
            script: 'git tag',
            returnStdout: true
        ).replaceAll('\n', ', ')

        steps.withCredentials([steps.usernamePassword(credentialsId: "${config.registryCredential}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            existing_tags_dockerhub_repository = steps.sh (
                    script: ''' wget -q --user $USERNAME --password $PASSWORD https://registry.hub.docker.com/v1/repositories/odoopartners/"${config.repo_name}"/tags -O -  | sed -e 's/[][]//g' -e 's/"//g' -e 's/ //g' | tr '}' '\n'  | awk -F: '{print $3}' ''',
                    returnStdout: true
                    ).replaceAll('\n', ', ')
        }

        List<String> list_existing_tags_github = Arrays.asList(existing_tags_github_repository.split("\\s*,\\s*"))

        List<String> list_existing_tags_docker = Arrays.asList(existing_tags_dockerhub_repository.split("\\s*,\\s*"))
        
        // Increasing version

        if (list_existing_tags_github.find { it.contains("${config.BRANCH_NAME}") } ){
            
            def last_tag = list_existing_tags_github.findAll { it.contains("${config.BRANCH_NAME}") }

            def last_version = last_tag[-1].split('-')[-1]
		
	    steps.echo "LIST TAGS ---> ${last_tag}"
	    steps.echo "Last version ---> ${last_version}"

            current_version = increaseVersion("${last_version}")
            
            steps.echo "New Version ---> ${current_version}"
            
            
        }else{

            steps.echo "New Client"
            
            current_version = increaseVersion("")
            
            steps.echo "New Version ---> ${current_version}"
            
        }



        // Validating

        def tagname = "${config.registry}"+"-$current_version"                     
        def tagname_sanitized = tagname.trim()

        def tagname_for_github = "${config.BRANCH_NAME}-$current_version"

        if (tagname_for_github.trim() in list_existing_tags_github) {
            steps.error("Build failed because of the tagname for GitHub already exists in the repository")
        } else {
            steps.echo 'New Tag for Github Repository ---> '+ tagname_for_github
        }

        if (tagname_for_github.trim() in list_existing_tags_docker) {
            steps.error("Build failed because of the tagname for DockerHub already exists in the repository")
        } else {
            steps.echo 'New Tag for DockerHub Repository ---> '+ tagname_for_github
        }


        return [tagname_sanitized, tagname, tagname_for_github]


    }
    

    def cloneRepositories(Map config = [:]) {
        
        steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.git_credentials}",
                    gitToolName: 'git-tool')]) {
            steps.loadLinuxScript(name: 'clone_repositories.sh')
            steps.loadPythonScript(name: 'check_repos_extra.py')
            steps.sh "./clone_repositories.sh ${config.branch_to_clone}"
        }
        
    }


    def buildImage(Map config = [:]) {

        def dockerImage

        steps.docker.withRegistry( '', "${config.registryCredential}" ) { 
            dockerImage = steps.docker.build("${config.tagname_sanitized}", ".") 
        }

        return dockerImage

    }

    
    def publishImage(Map config = [:]) {

        steps.docker.withRegistry( '', "${config.registryCredential}" ) { 
            steps.dockerImage.push() 
        }

    }
    

    def cleanUp(tagname) {

        steps.sh "docker rmi ${tagname}" 
        steps.sh "rm -rf repositories/"

    }
    

    def createTags(Map config = [:]) {

        steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.git_credentials}",
                    gitToolName: 'git-tool')]) {
            steps.sh "git tag ${config.tagname_for_github}" 
            steps.sh "git push origin ${config.tagname_for_github}" 
        }

    }


    def updateImageDeployment(Map config = [:]) {

        def branch_original_name = "${config.BRANCH_NAME}"
        def basename = branch_original_name.substring(0, branch_original_name.lastIndexOf("-"))

        steps.sshagent(credentials: ["${config.k8s_credentials}"]) {
            steps.sh """ 
                ssh -o StrictHostKeyChecking=no -l ubuntu "${config.ip_from_master_node}" -A "kubectl -n odoo set image deployment/${config.BRANCH_NAME} odoo-${basename}=odoopartners/${config.repo_name}:${config.tagname_for_github}" 
                """
        }

    }

    def getTagFromProduction(Map config = [:]) {

        def existing_tags_github_repository
        
        def current_version

        steps.withCredentials([steps.gitUsernamePassword(credentialsId: "${config.git_credentials}",
                    gitToolName: 'git-tool')]) {
            steps.sh "git fetch --all --tags"
        }

        existing_tags_github_repository = steps.sh (
            script: 'git tag',
            returnStdout: true
        ).replaceAll('\n', ', ')


        List<String> list_existing_tags_github = Arrays.asList(existing_tags_github_repository.split("\\s*,\\s*"))

		def production_tags = list_existing_tags_github.findAll { it.contains("${config.prod_deploy_name}") }

		def last_prod_tag_version = production_tags[-1].split('-')[-1]

        steps.echo "lastprodtagversion"
        steps.echo "${last_prod_tag_version}"

        // Validating

        def tagname = "${config.registry}"+"-$last_prod_tag_version"                     
        def tagname_sanitized = tagname.trim()

        def tagname_for_github = "${config.deploy_name}-$last_prod_tag_version"


        return [tagname_sanitized, tagname_for_github]


    }
    
    // Extra functions 
    
    def increaseVersion(String version){

        def version_updated = []

        def splited = version.split('\\.').reverse()

        def finished
        def yet_finished

        def aumento = false

        def count = 0


        if (version == ""){

            finished = "1.0.0"

        } else {

            for (num in splited){
                count = count + 1
                def parsednum = Integer.parseInt(num)

                if (parsednum >= 0 && parsednum < 99 && aumento == false){

                    parsednum = parsednum + 1
                    version_updated.add(0,parsednum)
                    aumento = true

                } else {

                    if (splited[2] != "99"){

                        if (parsednum == 99){

                            version_updated.add(0,0)    

                        } else {

                            version_updated.add(0,parsednum)

                        }

                    } else {

                        if (parsednum == 99 && count == 3){

                            version_updated.add(0,99)

                        } else if (parsednum == 99 && count != 3){

                            if (splited[1] == "99" && count == 2){

                                version_updated.add(0,99)    

                            } else {

                                version_updated.add(0,0)    

                            }

                        } else {

                            version_updated.add(0,parsednum)
                        }

                    }
                }

            }

            yet_finished = version_updated.join(".")
            finished = yet_finished
        }

        return finished

    }

    

}
