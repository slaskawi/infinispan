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
        
        //stage('Record test results') {
          //junit 'target/*-reports/**/*.xml'
        //}
    }
}
