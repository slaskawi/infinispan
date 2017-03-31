#!/usr/bin/env groovy

node {
  git url: 'https://github.com/slaskawi/infinispan.git'
  dev mvnHome = tool 'Maven'
  sh "${mvnHome}/bin/mvn -B verify"
}
