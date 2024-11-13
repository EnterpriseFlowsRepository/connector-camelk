echo "Déploiement vers $ENVIRONMENT."

MEDIATION=efr-apitraces
WHEN=$(date +%D-%R)

[[ -z ${NAMESPACE+x} ]] && echo "[X][efr-traces] Variable non définie: NAMESPACE." && exit 8

echo "Mediation $MEDIATION version $VERSION."
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
    --label mediation=$MEDIATION --label version=$VERSION \
    -t toleration.enabled=true \
    --trait toleration.taints="kubernetes.azure.com/scalesetpriority=spot:NoSchedule" \
    --wait

kubectl delete -n $NAMESPACE hpa/$MEDIATION
kubectl autoscale -n $NAMESPACE it $MEDIATION --min=1 --max=3 --cpu-percent=80
