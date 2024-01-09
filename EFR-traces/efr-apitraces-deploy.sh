echo "Déploiement vers $ENVIRONMENT."

MEDIATION=efr-apitraces
WHEN=$(date +%D-%R)

echo "Mediation $MEDIATION version $VERSION."
kamel run \
    --name $MEDIATION \
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

kubectl delete hpa/$MEDIATION
kubectl autoscale it $MEDIATION --min=1 --max=3 --cpu-percent=80
