//Camel API
// camel-k: dependency=mvn:org.apache.commons:commons-text:1.9
// camel-k: dependency=mvn:org.apache.commons:commons-lang3:3.12.0

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.microprofile.config.*;

public class PrepareTrace extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareTrace.class);

    @Override
    public void configure() throws Exception {
    }

    @BindToRegistry
    public static Processor prepareJsonTrace() {

        return new Processor() {
    
            // Retreive Quarkus config
            private Config config = ConfigProvider.getConfig();

            private String PROP_BUSINESS = "traces.business";
            private String PROP_BUSINESS_ASIS = "traces.business.asis";

            private int STACK_MAX_SIZE = 1000;
            private int BODY_MAX_SIZE = 200000; // 200Ko

            /**
             * Put properties to sources property.
             * 
             * @param ex exchange
             */
            public void process(Exchange exchange) throws Exception {
                
                // 1. Read all headers to generate an array
                String headers = "";

                for (String key: exchange.getIn().getHeaders().keySet()) {
                    try {
                        String value = exchange.getIn().getHeader(key, String.class);
                        value = StringEscapeUtils.escapeJson(value);
                        
                        // toutes les variables doivent avoir une valeur
                        if ( value == null )
                            value = "";
                            
                        headers += ",{ \"name\": \""+key+"\", \"value\": \""+value+"\" }";
                        
                    } catch (Exception e) {
                        LOG.warn("erreur sur "+key+": "+e.getMessage());
                    }
                }

                // produce an array of headers
                headers = "[ "+headers.substring(1) +" ]";
                exchange.setProperty("headers", headers);

                // 2. Convert body for json
                String body = "";
                
                try {
                    body = StringEscapeUtils.escapeJson(
                        exchange.getIn().getBody(String.class)
                    );
                } catch (Exception e) {
                    LOG.warn("erreur le parsing du Body. "+e.getMessage());
                }

                // Too big ?
                if ((body != null) && (body.length()>BODY_MAX_SIZE)) {
                    body = body.substring(0, BODY_MAX_SIZE-3)+"...";
                }

                String businessArray = "[]";

                try {
                    // 3. Read all properties to generate an array
                    String business = config.getValue(PROP_BUSINESS, String.class);

                    if ((business != null) && (business.length()>0)) {

                        String businessJson = "";

                        // Process each ref 
                        for (String businessRef: business.split(",")) {
                            String[] businessElement = businessRef.split("=") ;

                            
                            try {
                                String key = businessElement[0];
                                String valueRef = businessElement[1];
                                String value = StringEscapeUtils.escapeJson(exchange.getProperty(valueRef, String.class));
                                
                                if (value == null) {
                                    value = StringEscapeUtils.escapeJson(exchange.getIn().getHeader(valueRef, String.class));
                                }
                                if ((value == null) && (exchange.getOut() != null)) {
                                    value = StringEscapeUtils.escapeJson(exchange.getOut().getHeader(valueRef, String.class));
                                }

                                if (value != null) {
                                    businessJson += ",{ \"name\": \""+key+"\", \"value\": \""+value+"\" }";
                                } else {
                                    LOG.debug("No value for "+key+" on "+valueRef);
                                }

                            } catch (Exception e) {
                                LOG.warn("error on "+businessRef+": "+e.getMessage());
                            }
                        }
                        
                        // produce an array of values
                        if (businessJson.length()>0) {
                            businessArray = "[ "+businessJson.substring(1) +" ]";
                        } else {
                            businessArray = "[]";
                        }
                    }
                } catch (Exception e) {
                    LOG.debug("Add property=value in "+PROP_BUSINESS+" to use keys.");
                }

                try {
                    // properties business push as is without modification.
                    Boolean businessAsIs = config.getValue(PROP_BUSINESS_ASIS, Boolean.class);
                    String valueAsIs = exchange.getProperty(PROP_BUSINESS, String.class);
                    if (businessAsIs != null)
                        if ((businessAsIs) && (valueAsIs != null))
                            businessArray = valueAsIs;
                } catch (Exception e) {
                    LOG.debug("Add property "+PROP_BUSINESS_ASIS+" to force value.");
                }

                // escape description
                String propertyDescription = exchange.getProperty("description", String.class); 
                if (propertyDescription != null) {
                    propertyDescription = StringEscapeUtils.escapeJson(propertyDescription);
                    exchange.setProperty("description", propertyDescription);
                }
                    
                try {
                    // Format message to JSON
                    if (exchange.getProperty("exception-message") != null)
                        exchange.setProperty("exception-message", 
                            StringEscapeUtils.escapeJson(
                                exchange.getProperty("exception-message", String.class)
                                )
                            );

                    // 5. Exception strace
                    if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
                        Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);

                        LOG.info("Exception found: "+exception.getMessage());

                        String stack = ExceptionUtils.getStackTrace(exception);
                        if (stack.length()>STACK_MAX_SIZE) {
                            stack = stack.substring(0, STACK_MAX_SIZE-3)+"...";
                        }

                        exchange.setProperty("exception-stacktrace", 
                            StringEscapeUtils.escapeJson(
                                stack
                                )
                            );

                        String message = exception.getMessage();

                        exchange.setProperty("exception-message", 
                            StringEscapeUtils.escapeJson(
                                message
                                )
                            );

                        boolean codeFound = false;

                        // Retreive and test with content
                        for(String property: config.getPropertyNames()) {
                            try {
                                if (property.matches("traces\\.errors.*regex")) {
                                    LOG.info("Found property: "+property);
                                    String contentToTest = config.getValue(property, String.class);

                                    // Check regex with message
                                    if (message.matches(contentToTest)) {
                                        
                                        String[] parts = property.split("\\.");
                                        // set code
                                        exchange.setProperty("exception-code", parts[2]);
                                        // set type
                                        exchange.setProperty(
                                            "type", 
                                            config.getValue(parts[0]+"."+parts[1]+"."+parts[2]+".type", String.class)
                                        );
                                        codeFound = true;
                                    } else {
                                        LOG.info("Does not match with regEx: "+contentToTest);
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Impossible to parse "+property);
                            }
                        }

                        if (codeFound == false) {
                            LOG.warn("No error code for '"+message+"'. Add an traces.errors.CODE.regex=RegEx .");
                        }

                    } 
                    
                } catch (Exception e) {
                    LOG.error("Error dur Exception process", e);
                }

                try {

                    List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);

                    if ((list != null) && (!list.isEmpty())) {
                        String jsonHistory = new String();

                        for(MessageHistory history: list) {
                            String node = "";
                            node = "{" +
                                "'id:' '"+ history.getNode().getId() + "',"+
                                "'label:' '"+ history.getNode().getLabel() + "',"+
                                "'name:' '"+ history.getNode().getShortName() + "',"+
                                "'description:' '"+ history.getNode().getDescriptionText() + "',"+
                                "'location:' '"+ history.getNode().getLocation() + "',"+
                                "'line:' '"+ history.getNode().getLineNumber() + "'"+
                            "}";

                            jsonHistory += node+",";
                        }

                        // history
                        exchange.setProperty("json-history", 
                            StringEscapeUtils.escapeJson(
                                "[ "+jsonHistory.substring(0, jsonHistory.length()-1) +" ]"
                                )
                            );
                    }   

                } catch (Exception e) {
                    LOG.error("Error dur History process", e);
                }
                

                // end. put all into properties

                // put transformations into exchange.
                exchange.setProperty("body", body);
                exchange.setProperty("headers", headers);
                exchange.setProperty("business", businessArray);

                // Occurs during HTTP transformation
                String status = exchange.getProperty("status", String.class);
                if ((status == null) || (status.length()==0)) {
                    exchange.setProperty("status", "Error");
                }
                //json body
                //exchange.getIn().setBody(newBody);

            }
        };

    }
}
