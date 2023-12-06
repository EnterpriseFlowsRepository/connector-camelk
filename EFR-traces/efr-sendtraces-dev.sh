ENVIRONMENT=REC
MEDIATION=efr-sendtraces
VERSION=latest

kamel run \
    --name $MEDIATION \
    routes/servicesEFR.xml \
    routes/servicesOIDC.xml \
    routes/efr-sendtraces.xml \
    --property file:config/$ENVIRONMENT/run.properties \
    --property quarkus.application.name=$MEDIATION \
    -t logging.level=INFO \
    --build-property file:config/build.properties \
    --label version=$VERSION \
    --dev
