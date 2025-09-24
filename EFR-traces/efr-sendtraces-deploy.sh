MEDIATION=efr-sendtraces

[[ -z ${NAMESPACE+x} ]] && echo "[X][efr-traces] Variable non définie: NAMESPACE." && exit 8
[[ -z ${ENVIRONMENT+x} ]] && echo "[X][efr-traces] Variable non définie: ENVIRONMENT." && exit 8
[[ -z ${VERSION+x} ]] && echo "[X][efr-traces] Variable non définie: VERSION." && exit 8

echo "Déploiement vers $ENVIRONMENT."
echo "Mediation $MEDIATION version $VERSION."

kamel run \
    --name $MEDIATION \
    -n $NAMESPACE -x $NAMESPACE \
    routes/servicesEFR.xml \
    routes/servicesOIDC.xml \
    routes/efr-sendtraces.xml \
    routes/RetryProcessor.java \
    ../utilitaires/processors/MessageAggregator.java \
    --property file:config/$ENVIRONMENT/run.properties \
    --property route.id=$MEDIATION \
    -t logging.level=INFO \
    --build-property file:config/build.properties \
    --label mediation=$MEDIATION --label version=$VERSION \
    -t toleration.enabled=true \
    -t toleration.taints="kubernetes.azure.com/scalesetpriority=spot:NoSchedule" \
    --wait

kubectl delete -n $NAMESPACE hpa/$MEDIATION
kubectl autoscale -n $NAMESPACE it $MEDIATION --min=1 --max=10 --cpu-percent=10
