echo "Déploiement vers $ENVIRONMENT."

kamel run \
--name sample \
../utilitaires/errors/exceptions.xml \
../utilitaires/errors/ErrorHandler.java \
../utilitaires/processors/PrepareTrace.java \
../utilitaires/processors/SetCorrelationId.java \
../utilitaires/traces/traces.xml \
routes/Routing.java  \
--property file:config/$ENVIRONMENT/run.properties \
-t logging.level=INFO \
-t error-handler.ref=globalErrorHandler \
--build-property file:config/build.properties \
--open-api file:openapi/NEOTOA-Evenements-v1.json \
--wait

kubectl delete hpa/sample
kubectl autoscale it sample --min=1 --max=3 --cpu-percent=80

