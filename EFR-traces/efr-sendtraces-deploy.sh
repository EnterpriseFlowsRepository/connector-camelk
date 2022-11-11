echo "Déploiement vers $ENVIRONMENT."

MEDIATION=efr-sendtraces

echo "Mediation $MEDIATION version $VERSION."

kamel run \
--name $MEDIATION \
routes/servicesEFR.xml  \
routes/servicesOIDC.xml  \
routes/efr-sendtraces.xml  \
--property file:config/$ENVIRONMENT/run.properties \
--property quarkus.application.name=$MEDIATION \
-t logging.level=INFO \
--build-property file:config/build.properties \
--label mediation=$MEDIATION --label version=$VERSION \
--wait

kubectl delete hpa/$MEDIATION
kubectl autoscale it $MEDIATION --min=1 --max=3 --cpu-percent=80
