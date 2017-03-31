#!/usr/bin/env groovy

node {
  stage 'SCM Checkout'
  checkout scm
  
  stage 'Build'
  def mvnHome = tool 'Maven'
  sh "${mvnHome}/bin/mvn clean package -pl bom"
  
  stage 'Tests'
  junit 'target/surefire-reports/**/*.xml'
  junit 'target/failsafe-reports/**/*.xml'
  
  stage 'Artifacts'
  archiveArtifacts artifacts: 'target/**/*.jar'
}
