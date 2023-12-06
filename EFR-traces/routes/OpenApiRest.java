//Camel API
// camel-k: dependency=camel-platform-http

import org.apache.camel.*;
import org.apache.camel.builder.*;
import java.util.*;
import org.eclipse.microprofile.config.*;
import org.slf4j.*;

public class OpenApiRest extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(OpenApiRest.class);
    private final Config config = ConfigProvider.getConfig();

    private static final Map<String, String> ALLOWED_TRACES = Map.of(
        "listTraces", "GET" ,
        "bulkTraces", "PUT" ,
        "addTrace"  , "POST"
    );
    
    @Override
    public void configure() throws Exception {
        Optional<String> enabled = config.getOptionalValue("openapi.enable", String.class);
        if(enabled.isEmpty()) {
            LOG.error("Please, define 'openapi.enable' property.");
            return;
        }
        List<String> enableds = List.of(enabled.get().split(","));

        for(String id : ALLOWED_TRACES.keySet()) {
            if(!enableds.contains(id)) continue;

            String path = "platform-http:/traces?httpMethodRestrict=" + ALLOWED_TRACES.get(id);

            LOG.info("Enabling [" + path + "] -> [direct:" + id + "]");
            from(path)
                .to("direct:" + id);
        }
    }

}
