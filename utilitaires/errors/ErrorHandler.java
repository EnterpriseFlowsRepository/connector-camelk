import org.apache.camel.builder.*;
import org.apache.camel.*;
import org.apache.camel.processor.errorhandler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.microprofile.config.*;

public class ErrorHandler extends RouteBuilder {

  @Override
  public void configure() throws Exception {}

  private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);

  private static final Config config = ConfigProvider.getConfig();

  @BindToRegistry
  public static DeadLetterChannelBuilder globalErrorHandler() {
    DeadLetterChannelBuilder deadLetterChannelBuilder = new DeadLetterChannelBuilder();

    deadLetterChannelBuilder.setDeadLetterUri("seda:exceptions-manageException");

    LOG.info("DeadLetterChannelBuilder :");
    LOG.info("- retry=" + config.getValue("errors.global.maximumRedeliveries", Integer.class));
    LOG.info("- patterns=" + config.getValue("errors.global.delayPattern", String.class));
    LOG.info("- delay=" + config.getValue("errors.global.delay", Integer.class));

    deadLetterChannelBuilder
      .maximumRedeliveries(config.getValue("errors.global.maximumRedeliveries", Integer.class))
//      .delayPattern(config.getValue("errors.global.delayPattern", String.class))
      .redeliveryDelay(config.getValue("errors.global.delay", Integer.class))
      .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
      .useOriginalMessage();

    return deadLetterChannelBuilder;
  }
}
