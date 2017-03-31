#!/usr/bin/env groovy

node {
  stage 'SCM Checkout'
  checkout scm
  
  stage 'Build'
  def mvnHome = tool 'Maven'
  sh "${mvnHome}/bin/mvn clean package -pl bom -pl commons"
  
  stage 'Tests'
  junit 'target/*-reports/**/*.xml'
  
  stage 'Artifacts'
  archiveArtifacts artifacts: 'target/**/*.jar'
}
