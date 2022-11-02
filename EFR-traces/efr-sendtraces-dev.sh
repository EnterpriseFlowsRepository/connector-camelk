export ENVIRONMENT=REC

# Force quarkus.application.name. Not read into file !
kamel run \
--name efr-sendtraces \
routes/servicesEFR.xml  \
routes/servicesOIDC.xml  \
routes/efr-sendtraces.xml  \
--property file:config/$ENVIRONMENT/run.properties \
--property quarkus.application.name=efr-sendtraces \
-t logging.level=INFO \
--build-property file:config/build.properties \
--dev