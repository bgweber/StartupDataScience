
import java.io.Serializable;
import java.util.HashMap;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.regression.RegressionModel;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.regression.RegressionModelEvaluator;
import org.jpmml.model.PMMLUtil;

import com.google.common.io.Resources;

public class PmmlPubSub {

  /** The GCS project name */
  private static final String PROJECT_ID = "gcp_project_id";
  
  /**	The topic to subscribe to for game events */
  private static String inboundTopic = "projects/your_project_id/topics/natality_inbound";
  private static String outboundTopic = "projects/your_project_id/topics/natality_outbound";

  /** Provide an interface for setting theGCS temp location */
  interface Options extends PipelineOptions, Serializable {
    String getTempLocation();
    void setTempLocation(String value);
    
    boolean isStreaming();
    void setStreaming(boolean value);    
  }

  public static void main(String[] args) {

    // create the data flow pipeline
    // Enforce that this pipeline is always run in streaming mode.
	PmmlPubSub.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(PmmlPubSub.Options.class);
    options.setStreaming(true);
    Pipeline pipeline = Pipeline.create(options);
    
    // Read messages from PubSub
    PCollection<PubsubMessage> events = pipeline
    	.apply(PubsubIO.readMessages().fromTopic(inboundTopic));
    // apply the PMML model to each of the records in the message
    events.apply("PMML Application", new PTransform<PCollection<PubsubMessage>, PCollection<PubsubMessage>>() {

      // define a transform that loads the PMML specification and applies it to all of the records
      public PCollection<PubsubMessage> expand(PCollection<PubsubMessage> input) {

        // load the PMML model
        final ModelEvaluator<RegressionModel> evaluator;
        try {
          evaluator = new RegressionModelEvaluator(
                  PMMLUtil.unmarshal(Resources.getResource("natality.pmml").openStream()));
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }

        // create a DoFn for applying the PMML model to instances
        return input.apply("To Predictions", ParDo.of(new DoFn<PubsubMessage, PubsubMessage>() {

          @ProcessElement
          public void processElement(ProcessContext c) throws Exception {
        	PubsubMessage row = c.element();

            // create a map of inputs for the pmml model
            HashMap<FieldName, Double> inputs = new HashMap<>();
            for (String key : row.getAttributeMap().keySet()) {
              if (!key.equals("weight_pounds")) {
                inputs.put(FieldName.create(key), Double.parseDouble(row.getAttribute(key)));
              }
            }

            // get the estimate
            Double estimate = (Double)evaluator.evaluate(inputs).get(FieldName.create("weight_pounds"));

            // create a message with the prediction
            String message = "Prediction:" + estimate; 
      	    PubsubMessage msg = new PubsubMessage(message.getBytes(), new HashMap());
            c.output(msg);
          }
        }));
      }
    })
    .apply(PubsubIO.writeMessages().to(outboundTopic));
    
    // run it
    pipeline.run();
  }
}
