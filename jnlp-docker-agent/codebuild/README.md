# lsegal/jnlp-docker-agent:codebuild

This directory contains a `Dockerfile` that builds the `lsegal/jnlp-docker-agent`
image based on [`aws/codebuild/java:openjdk-8`][jdkimage], adding in extra
JNLP [remoting][remoting] support from [jenkins/jnlp-slave][jnlpimage].

## Building `codebuild`

To build this image, run:

```sh
docker build -t lsegal/jnlp-docker-agent:codebuild .
```

[jdkimage]: https://github.com/aws/aws-codebuild-docker-images/tree/master/ubuntu/java/openjdk-8
[remoting]: https://github.com/jenkinsci/remoting
[jnlpimage]: https://hub.docker.com/r/jenkins/jnlp-slave/
