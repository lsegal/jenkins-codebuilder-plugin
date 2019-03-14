FROM jenkins/jnlp-slave:alpine
LABEL maintainer="Loren Segal <lsegal@soen.ca>"

# Simplify permissions in this containerized instance
USER root

# Install Docker
RUN apk add docker

# Setup jenkins-agent
COPY ./dockerd-entrypoint.sh /usr/local/bin/dockerd-entrypoint.sh
RUN mv /usr/local/bin/jenkins-slave /usr/local/bin/jenkins-agent
RUN chmod +x /usr/local/bin/jenkins-agent /usr/local/bin/dockerd-entrypoint.sh

ENTRYPOINT [ "/usr/local/bin/jenkins-agent" ]
