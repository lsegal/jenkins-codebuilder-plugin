# lsegal/jnlp-docker-agent:alpine

This directory contains a `Dockerfile` that builds the `lsegal/jnlp-docker-agent`
image based on [jenkins/jnlp-slave][jnlpimage], adding in extra Docker support.

## Building `alpine`

To build this image, run:

```sh
docker build -t lsegal/jnlp-docker-agent:alpine .
```

[jnlpimage]: https://hub.docker.com/r/jenkins/jnlp-slave/
