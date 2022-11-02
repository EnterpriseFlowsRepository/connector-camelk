# Presentation

Connector between CamelK and EFR.
Routes and java code to use easyless EFR.  

# Add submodule

Add this project as a submodule of your project.

```
git submodule add git@github.com:EnterpriseFlowsRepository/connector-camelk.git
```

# Configuration

## For Azure

By default, configuration is done for Azure and Service Bus.

# For Azure ServiceBus

in properties file:

```
azure.servicebus.cs=Endpoint=sb://xxx.servicebus.windows.net/;SharedAccessKeyName=xxx;SharedAccessKey=xxx
...
messaging.queue.traces-traces=azure-servicebus:traces.traces?exchangePattern=InOnly&serviceBusType=queue&connectionString=${azure.servicebus.cs}
messaging.queue.traces-errors=azure-servicebus:traces.errors?exchangePattern=InOut&serviceBusType=queue&connectionString=${azure.servicebus.cs}
messaging.exceptions.exception=azure-servicebus:exceptions.exceptions?exchangePattern=InOut&serviceBusType=queue&connectionString=${azure.servicebus.cs}
...
```