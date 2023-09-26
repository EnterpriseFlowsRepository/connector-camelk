export ENVIRONMENT=REC

kamel run \
--name api-evenements \
../utilitaires/errors/exceptions.xml \
../utilitaires/errors/ErrorHandler.java \
../utilitaires/processors/PrepareTrace.java \
../utilitaires/processors/SetCorrelationId.java \
../utilitaires/traces/traces.xml \
routes/publishEvenement.xml  \
--property file:config/$ENVIRONMENT/run.properties \
-t logging.level=INFO \
-t error-handler.ref=globalErrorHandler \
--build-property file:config/build.properties \
--open-api file:openapi/NEOTOA-Evenements-v1.json \
--dev

# ../utilitaires/errors/ErrorHandler.java \