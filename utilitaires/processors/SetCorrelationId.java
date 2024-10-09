//Camel API
// camel-k: dependency=mvn:org.apache.commons:commons-text:1.9
// camel-k: dependency=mvn:org.apache.commons:commons-lang3:3.12.0

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.microprofile.config.*;

public class SetCorrelationId extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SetCorrelationId.class);

    @Override
    public void configure() {}

    @BindToRegistry
    public static Processor setAzureCorrelationId() {
        return new Processor() {
    
            // Retreive Quarkus config
            private final Config config = ConfigProvider.getConfig();

            private String AzureCorrelationId = "correlation-id";
            private String CorrelationId = "CorrelationId";
            private String AzSBCorrelationId = "CamelAzureServiceBusApplicationProperties";
            
            /**
             * Put properties to sources property.
             * 
             * @param ex exchange
             */
            public void process(Exchange exchange) throws Exception {
                
                // 1. Read all headers to generate an array
                Map<String, Object> map = null;
                String azCorrelationId = exchange.getProperty(AzureCorrelationId, String.class);
                String correlationId = exchange.getProperty(CorrelationId, String.class);

                // null in properties ?
                if ((azCorrelationId == null) || azCorrelationId.equals("")) {
                    azCorrelationId = exchange.getIn().getHeader(AzureCorrelationId, String.class);
                    correlationId = azCorrelationId;

                    LOG.debug("Correlation ID from property "+AzureCorrelationId+"="+azCorrelationId);
                }

                // null in properties ?
                if ((correlationId == null) || correlationId.equals("")) {
                    correlationId = exchange.getIn().getHeader(CorrelationId, String.class);
                    azCorrelationId = correlationId;

                    LOG.debug("Correlation ID from property "+CorrelationId+"="+correlationId);
                }

                // Get from ServiceBus
                Map props = exchange.getIn().getHeader(AzSBCorrelationId, Map.class);
                if (props == null) {
                    props = new HashMap<String, String>();
                    exchange.getIn().setHeader(AzSBCorrelationId, props);
                } else {
                    LOG.debug(AzSBCorrelationId+"="+props);

                    if (props.containsKey(AzureCorrelationId)) {
                        azCorrelationId = (String)props.get(AzureCorrelationId);
                        correlationId = azCorrelationId;

                        LOG.debug("Correlation ID from property "+AzSBCorrelationId+"="+correlationId);
                    }

                    if (props.containsKey(CorrelationId)) {
                        azCorrelationId = (String)props.get(CorrelationId);
                        correlationId = azCorrelationId;

                        LOG.debug("Correlation ID from property "+AzSBCorrelationId+"="+correlationId);
                    }
                }


                // null in headers and property, generate a new one
                if ((correlationId == null) || (azCorrelationId == null)) {
                    azCorrelationId = java.util.UUID.randomUUID().toString();
                    correlationId = azCorrelationId;

                    LOG.debug("Correlation ID created ="+correlationId);
                }

                // Set Azure Correlation ID
                exchange.setProperty(AzureCorrelationId, azCorrelationId);
                exchange.getIn().setHeader(AzureCorrelationId, azCorrelationId);

                // Set classic Correlation ID
                exchange.setProperty(CorrelationId, correlationId);
                exchange.getIn().setHeader(CorrelationId, correlationId);

                // Put into ServiceBus
                props.put(AzureCorrelationId, azCorrelationId);
                props.put(CorrelationId, correlationId);

                exchange.getIn().setHeader("CamelAzureServiceBusCorrelationId", azCorrelationId);
            }
        };

    }
}
