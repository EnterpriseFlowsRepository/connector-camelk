ENVIRONMENT=REC
MEDIATION=efr-apitraces
VERSION=latest

[[ -z ${NAMESPACE+x} ]] && echo "[X][efr-traces] Variable non définie: NAMESPACE." && exit 8

echo "Starting $MEDIATION"
kamel run \
    --name $MEDIATION \
    -n $NAMESPACE -x $NAMESPACE \
    routes/OpenApiRest.java \
    routes/efr-addTrace.xml \
    --property openapi.enable=addTrace \
    --property file:config/$ENVIRONMENT/run.properties \
    --property route.id=$MEDIATION \
    -t logging.level=INFO \
    --build-property file:config/build.properties \
    --label version=$VERSION \
    -t toleration.enabled=true \
    --trait toleration.taints="kubernetes.azure.com/scalesetpriority=spot:NoSchedule" \
    --dev


# Does not work... --open-api file:openapi/EFR-Traces-v1.yaml \