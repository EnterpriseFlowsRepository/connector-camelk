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
    public void configure() throws Exception {
    }

    @BindToRegistry
    public static Processor setAzureCorrelationId() {

        return new Processor() {
    
            // Retreive Quarkus config
            private Config config = ConfigProvider.getConfig();

            private String PROP_BUSINESS = "traces.business";

            private String AzureCorrelationId = "correlation-id";
            private String AzureProperties = "CamelAzureServiceBusApplicationProperties";

            /**
             * Put properties to sources property.
             * 
             * @param ex exchange
             */
            public void process(Exchange exchange) throws Exception {
                
                // 1. Read all headers to generate an array
                Map<String, Object> map = null;
                String correlationId = exchange.getProperty(AzureCorrelationId, String.class);

                for (String key: exchange.getIn().getHeaders().keySet()) {
                    try {
                        
                        if (key.equals(AzureProperties)) {
                            map = exchange.getIn().getHeader(key, Map.class);
                        }

                        if (key.equals(AzureCorrelationId)) {
                            correlationId = exchange.getIn().getHeader(key, String.class);
                        }
                        
                    } catch (Exception e) {
                        LOG.error("erreur sur "+key+": "+e.getMessage());
                    }
                }

                if (map == null) {
                    map = new HashMap<String, Object>();
                    exchange.getIn().setHeader(AzureProperties, map);
                }

                if ((map != null) && (correlationId != null))
                    map.put(AzureCorrelationId, correlationId);

            }
        };

    }
}
