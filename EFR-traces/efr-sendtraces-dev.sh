ENVIRONMENT=REC
MEDIATION=efr-sendtraces
VERSION=latest

[[ -z ${NAMESPACE+x} ]] && echo "[X][efr-traces] Variable non définie: NAMESPACE." && exit 8

kamel run \
    --name $MEDIATION \
    -n $NAMESPACE -x $NAMESPACE \
    routes/servicesEFR.xml \
    routes/servicesOIDC.xml \
    routes/efr-sendtraces.xml \
    --property file:config/$ENVIRONMENT/run.properties \
    --property route.id=$MEDIATION \
    -t logging.level=INFO \
    --build-property file:config/build.properties \
    --label version=$VERSION \
    --dev
