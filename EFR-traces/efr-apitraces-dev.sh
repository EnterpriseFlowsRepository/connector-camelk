ENVIRONMENT=REC
MEDIATION=efr-apitraces
VERSION=latest

echo "Starting $MEDIATION"
kamel run \
    --name $MEDIATION \
    routes/OpenApiRest.java \
    routes/efr-addTrace.xml \
    --property openapi.enable=addTrace \
    --property file:config/$ENVIRONMENT/run.properties \
    --property quarkus.application.name=$MEDIATION \
    -t logging.level=INFO \
    --build-property file:config/build.properties \
    --label version=$VERSION \
    --dev


# Does not work... --open-api file:openapi/EFR-Traces-v1.yaml \