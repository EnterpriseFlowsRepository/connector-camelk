echo "Déploiement vers $ENVIRONMENT."

MEDIATION=efr-sendtraces

echo "Mediation $MEDIATION version $VERSION."

kamel run \
    --name $MEDIATION \
    routes/servicesEFR.xml \
    routes/servicesOIDC.xml \
    routes/efr-sendtraces.xml \
    --property file:config/$ENVIRONMENT/run.properties \
    --property route.id=$MEDIATION \
    -t logging.level=INFO \
    --build-property file:config/build.properties \
    --label mediation=$MEDIATION --label version=$VERSION \
    -t toleration.enabled=true \
    -t toleration.taints="kubernetes.azure.com/scalesetpriority=spot:NoSchedule" \
    --wait

kubectl delete hpa/$MEDIATION
kubectl autoscale it $MEDIATION --min=1 --max=3 --cpu-percent=80
