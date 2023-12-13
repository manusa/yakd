FROM quay.io/quarkus/quarkus-micro-image:2.0

LABEL MAINTAINER="Marc Nuri <marc@marcnuri.com>"

COPY target/yakd-runner /

CMD ["/yakd-runner", "-Dquarkus.http.host=0.0.0.0"]

USER 10001
EXPOSE 8080
