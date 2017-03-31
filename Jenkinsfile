#!/usr/bin/env groovy

pipeline {
    agent any
    stages {
        stage('SCM Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                  def mvnHome = tool 'Maven'
                  sh "${mvnHome}/bin/mvn clean package -pl bom -pl license -pl commons"
                }
            }
        }
    }
    post {
        always {
            archive '**/target/*.jar'
            junit '**/target/*-reports/*.xml'
        }
    }
}
