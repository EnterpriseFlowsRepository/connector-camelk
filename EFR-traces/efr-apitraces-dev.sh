export ENVIRONMENT=REC

# Force quarkus.application.name. Not read into file !
kamel run \
--name efr-apitraces \
routes/efr-addTrace.xml  \
--property file:config/$ENVIRONMENT/run.properties \
--property quarkus.application.name=efr-apitraces \
-t logging.level=INFO \
--build-property file:config/build.properties \
--open-api file:openapi/EFR-Traces-v1.yaml \
--dev