FROM alpine:latest

LABEL "com.github.actions.name"="GitHub CLI"
LABEL "com.github.actions.description"="Access to the `hub` CLI"
LABEL "com.github.actions.icon"="command"
LABEL "com.github.actions.color"="blue"

LABEL "repository"="http://github.com/lsegal/jenkins-codebuilder-plugin"
LABEL "maintainer"="Loren Segal <lsegal@soen.ca>"

RUN apk add -U \
  --repository http://dl-cdn.alpinelinux.org/alpine/edge/testing \
  hub git

WORKDIR /github/workspace
COPY ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT [ "/entrypoint.sh" ]
