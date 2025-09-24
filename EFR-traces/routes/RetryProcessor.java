//Camel API

import org.apache.camel.*;
import org.apache.camel.builder.*;
import java.util.*;
import org.eclipse.microprofile.config.*;
import org.slf4j.*;

public class RetryProcessor extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(RetryProcessor.class);
    private static final Config config = ConfigProvider.getConfig();

    private static final String VAR_NAME = "_retries";

    @Override
    public void configure() throws Exception {
        // No route
    }

    @BindToRegistry
    public static Processor retry__initialize() {
        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                int retries = readConfiguredRetries();
                exchange.setProperty(VAR_NAME, retries);
            }
        };
    }

    @BindToRegistry
    public static Processor retry__incrementOrStop() {
        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Integer retries = exchange.getProperty(VAR_NAME, Integer.class);

                // Not initialized
                if(retries == null) {
                    LOG.warn("Retries not initialized.");
                    int currentRetries = readConfiguredRetries() - 1;
                    exchange.setProperty(VAR_NAME, currentRetries);
                    return;
                }

                // Normal
                int currentRetries = retries - 1;
                if(currentRetries > 0) {
                    LOG.info("Retrying... " + currentRetries + " retries left.");
                    exchange.setProperty(VAR_NAME, currentRetries);
                    return;
                }

                // Stop
                LOG.error("No more retries left. Stopping.");
                exchange.setRouteStop(true);
            }
        };
    }

    private static int readConfiguredRetries() {
        return config.getOptionalValue("efr-sendtraces.retry.count", Integer.class).orElse(3);
    }

}
