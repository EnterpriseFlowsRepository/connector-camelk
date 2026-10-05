//Camel API

import java.util.*;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;

import org.eclipse.microprofile.config.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom aggregator, grouping traces together.
 */
@BindToRegistry(value = "efr_message_aggregator")
public class MessageAggregator implements AggregationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(MessageAggregator.class);

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        String newBody = newExchange.getIn().getBody(String.class);
        if (newBody == null || newBody.isBlank()) {
            return oldExchange != null ? oldExchange : newExchange;
        }
        newBody = newBody.trim();

        if (oldExchange == null) {
            newExchange.getIn().setBody("[" + newBody + "]");
            newExchange.setProperty("traces-count", 1);
            return newExchange;
        }

        int count = oldExchange.getProperty("traces-count", Integer.class) + 1;
        oldExchange.setProperty("traces-count", count);
        
        String oldBody = oldExchange.getIn().getBody(String.class).trim();
        if (oldBody.endsWith("]")) {
            oldBody = oldBody.substring(0, oldBody.length() - 1);
            oldExchange.getIn().setBody(oldBody + "," + newBody + "]");
        } else {
            oldExchange.getIn().setBody("[" + oldBody + "," + newBody + "]");
        }
        return oldExchange;
    }
}