#!groovy

// https://github.com/jenkins-infra/pipeline-library
//buildPlugin(jenkinsVersions: ['2.150'], findbugs: [], failFast: false)
buildPlugin(findbugs: [], failFast: false, configurations: [
    [ platform: "linux", jdk: "8", jenkins: null ],
    [ platform: "windows", jdk: "8", jenkins: null ],
])
