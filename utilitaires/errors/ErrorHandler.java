import org.apache.camel.builder.*;
import org.apache.camel.*;
import org.apache.camel.processor.errorhandler.*;

public class ErrorHandler extends RouteBuilder {
  
  @Override
  public void configure() throws Exception {

  }

  @BindToRegistry
  public static DeadLetterChannelBuilder globalErrorHandler() {

        DeadLetterChannelBuilder deadLetterChannelBuilder = new DeadLetterChannelBuilder();

        deadLetterChannelBuilder.setDeadLetterUri("seda:exceptions-manageException");
        deadLetterChannelBuilder.setRedeliveryPolicy(
          new RedeliveryPolicy()
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN));
        deadLetterChannelBuilder.useOriginalMessage();
        
        return deadLetterChannelBuilder;

  }
}
