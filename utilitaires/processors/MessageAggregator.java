//Camel API
// camel-k: dependency=mvn:org.json:json:20220924

import java.util.*;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;

import org.eclipse.microprofile.config.*;
import org.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom aggregator, grouping traces together.
 */
@BindToRegistry(value = "efr_message_aggregator")
public class MessageAggregator implements AggregationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(MessageAggregator.class);

    /**
     * Exchange body to JSON.
     */
    private JSONObject extractJson(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        return new JSONObject(body);
    }

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // Transform the first element to a JSON array
        if (oldExchange == null) {
            JSONArray array = new JSONArray();
            array.put(extractJson(newExchange));
            newExchange.getIn().setBody(array);
            return newExchange;
        }

        // Append the current body to the existing JSON array.
        JSONArray array =  oldExchange.getIn().getBody(JSONArray.class);
        JSONObject newJson = extractJson(newExchange);
        array.put(newJson);

        return oldExchange;
    }
}