export ENVIRONMENT=REC

MEDIATION=efr-apitraces
VERSION=latest

# Force quarkus.application.name. Not read into file !
kamel run \
--name $MEDIATION \
routes/efr-addTrace.xml  \
--property file:config/$ENVIRONMENT/run.properties \
--property quarkus.application.name=$MEDIATION \
-t logging.level=INFO \
--build-property file:config/build.properties \
--open-api file:openapi/EFR-Traces-v1.yaml \
--label version=$VERSION \
--dev