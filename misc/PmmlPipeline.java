import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.common.io.Resources;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.regression.RegressionModel;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.regression.RegressionModelEvaluator;
import org.jpmml.model.PMMLUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Sample DataFlow pipeline for applying a model specifed as PMML to data stored in BiqQuery.
 * The pipeline has the following steps:
 * - Load data from BigQuery
 * - Apply the PMML specification to the data in each tow
 * - Save the results back to BigQuery
 *
 * To run this script, you'll need to specify a GCP project ID.
 */
public class PmmlPipeline {

  /** The GCS project name */
  private static final String PROJECT_ID = "gcp_project_id";

  /** The dataset name for the BigQuery output table */
  private static final String dataset = "natality";

  /** The table name for the BigQuery output table */
  private static final String table = "predicted_weights";

  /** Provide an interface for setting theGCS temp location */
  interface Options extends PipelineOptions, Serializable {
    String getTempLocation();
    void setTempLocation(String value);
  }

  /* The query used to pull instances to predicting weights.  */
  private static final String featureQuery =
      "SELECT year, plurality, apgar_5min, mother_age, father_age, gestation_weeks, ever_born\n" +
      "  ,case when mother_married then 1 else 0 end as mother_married, weight_pounds\n" +
      "FROM `bigquery-public-data.samples.natality`\n" +
      "where year is not null and plurality is not null and apgar_5min is not null \n" +
      "  and mother_age is not null and father_age is not null and gestation_weeks is not null\n" +
      "  and ever_born is not null and mother_married is not null and weight_pounds is not null\n" +
      "order by rand() \n" +
      "LIMIT 1000";

  /**
   * Creates and runs a data flow pipeline that reads data from BigQuery, applies a PMML model to make
   * a prediction, and write the results back to BigQuery.
   */
  public static void main(String[] args) {

    // create the schema for the output table
    List<TableFieldSchema> fields = new ArrayList<>();
    fields.add(new TableFieldSchema().setName("actual_weight").setType("FLOAT64"));
    fields.add(new TableFieldSchema().setName("predicted_weight").setType("FLOAT64"));
    TableSchema schema = new TableSchema().setFields(fields);

    // create the data flow pipeline
    PmmlPipeline.Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(PmmlPipeline.Options.class);
    Pipeline pipeline = Pipeline.create(options);

    // get the data from BigQuery
    pipeline.apply(BigQueryIO.read().fromQuery(featureQuery)
            .usingStandardSql().withoutResultFlattening()
    )
    // apply the PMML model to each of the records in the result set
    .apply("PMML Application", new PTransform<PCollection<TableRow>, PCollection<TableRow>>() {

      // define a transform that loads the PMML specification and applies it to all of the records
      public PCollection<TableRow> expand(PCollection<TableRow> input) {

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
        return input.apply("To Predictions", ParDo.of(new DoFn<TableRow, TableRow>() {

          @ProcessElement
          public void processElement(ProcessContext c) throws Exception {
            TableRow row = c.element();

            // create a map of inputs for the pmml model
            HashMap<FieldName, Double> inputs = new HashMap<>();
            for (String key : row.keySet()) {
              if (!key.equals("weight_pounds")) {
                inputs.put(FieldName.create(key), Double.parseDouble(row.get(key).toString()));
              }
            }

            // get the estimate
            Double estimate = (Double)evaluator.evaluate(inputs).get(FieldName.create("weight_pounds"));

            // create a table row with the prediction
            TableRow prediction = new TableRow();
            prediction.set("actual_weight", Double.parseDouble(row.get("weight_pounds").toString()));
            prediction.set("predicted_weight", estimate);

            // output the prediction to the data flow pipeline
            c.output(prediction);
          }
        }));
      }
    })
    // write the results to Big Query
    .apply(BigQueryIO.writeTableRows() .to(String.format("%s:%s.%s", PROJECT_ID, dataset, table))
            .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
            .withSchema(schema)
    );

    // run it
    pipeline.run();
  }
}
