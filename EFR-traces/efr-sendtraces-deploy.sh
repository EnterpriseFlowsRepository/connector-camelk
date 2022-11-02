echo "Déploiement vers $ENVIRONMENT."

kamel run \
--name efr-sendtraces \
routes/servicesEFR.xml  \
routes/servicesOIDC.xml  \
routes/efr-sendtraces.xml  \
--property file:config/$ENVIRONMENT/run.properties \
--property quarkus.application.name=efr-sendtraces \
-t logging.level=INFO \
--build-property file:config/build.properties \
--wait

kubectl delete hpa/efr-sendtraces
kubectl autoscale it efr-sendtraces --min=1 --max=3 --cpu-percent=80
