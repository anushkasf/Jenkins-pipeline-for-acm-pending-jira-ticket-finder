pipeline {
    agent any
    
    environment {
        JIRA_TOKEN = credentials(<'JIRA_TOKEN'>)
        JIRA_URL = "https://<COMPANY>.atlassian.net/rest/api/2/search"
        TICKET_URL = "https://<COMPANY>.atlassian.net/browse/"
    }

    stages {
        stage('Get JIRA Tickets') {
            steps {
                script {
                    def jqlQuery = 'project=DOPS AND summary ~ "SSL Certificate expiration notice for Certificate" AND status not in ("Done", "Cancelled")'
                    def startAt = 0
                    def maxResults = 1000
                    def pageCount = 0

                    def curlCommand = """
                        curl -X GET -H 'Authorization: $env.JIRA_TOKEN' -H 'Content-Type: application/json' -H 'Accept: application/json' '${JIRA_URL}?jql=${URLEncoder.encode(jqlQuery, 'UTF-8')}&startAt=${startAt}&maxResults=${maxResults}'
                    """
                    def issues = sh(script: curlCommand, returnStdout: true).trim()

                    def issuesJson = readJSON text: issues
                    def issueKeys = issuesJson.issues.collect { "${it.key}: ${it.fields.summary} - https://<COMPANY>.atlassian.net/browse/${it.key}"}
                    
                    issueKeys.each { println it }
                }
            }
        }
    }
}