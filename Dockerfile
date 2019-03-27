FROM openjdk:8-jre
COPY svc /svc
CMD /svc/bin/start
