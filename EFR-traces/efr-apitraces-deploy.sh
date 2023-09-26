echo "Déploiement vers $ENVIRONMENT."

MEDIATION=efr-apitraces
WHEN=$(date +%D-%R)

echo "Mediation $MEDIATION version $VERSION."

# Force quarkus.application.name. Not read into file !
kamel run \
--name $MEDIATION \
routes/efr-addTrace.xml  \
--property file:config/$ENVIRONMENT/run.properties \
--property quarkus.application.name=$MEDIATION \
-t logging.level=INFO \
--build-property file:config/build.properties \
--open-api file:openapi/EFR-Traces-v1.yaml \
--label mediation=$MEDIATION --label version=$VERSION \
-t toleration.enabled=true \
--trait toleration.taints="kubernetes.azure.com/scalesetpriority=spot:NoSchedule" \
--wait

kubectl delete hpa/$MEDIATION
kubectl autoscale it $MEDIATION --min=1 --max=3 --cpu-percent=80