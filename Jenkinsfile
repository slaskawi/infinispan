#!/usr/bin/env groovy

node {
  stage 'Stage Checkout'
  checkout scm
  
  stage 'Build'
  def mvnHome = tool 'Maven'
  sh "${mvnHome}/bin/mvn -B verify"
}
