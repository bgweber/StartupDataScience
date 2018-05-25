import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.regression.RegressionModel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.regression.RegressionModelEvaluator;
import org.jpmml.model.PMMLUtil;

import com.google.common.io.Resources;
//http://localhost:8080/?year=2000&plurality=1&apgar_5min=0&mother_age=30&father_age=28&gestation_weeks=40&ever_born=1&mother_married=1    		
public class JettyPmml  extends AbstractHandler {
	
    private final String[] modelFeatures = {
    		"year", "plurality", "apgar_5min", "mother_age",
    		"father_age", "gestation_weeks", "ever_born", "mother_married"    		
    };
    
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
        server.setHandler(new JettyPmml());
        server.start();
        server.join();
    }
		
	public void handle(String target,Request baseRequest, HttpServletRequest request, 
			HttpServletResponse response) throws IOException, ServletException {
		
        // load the PMML model
        final ModelEvaluator<RegressionModel> evaluator;
        try {
          evaluator = new RegressionModelEvaluator(
                  PMMLUtil.unmarshal(Resources.getResource("natality.pmml").openStream()));
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        // create a map of inputs for the pmml model
        HashMap<FieldName, Double> inputs = new HashMap<>();        
        for (String attribute : modelFeatures) {
        	String value = baseRequest.getParameter(attribute);
        	if (value != null) {
            	inputs.put(FieldName.create(attribute), Double.parseDouble(value));
        	}
        }
        				
        // output the estimate
        Double estimate = (Double)evaluator.evaluate(inputs).get(FieldName.create("weight_pounds"));        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Prediction: " + estimate);
        baseRequest.setHandled(true);
    }        
}
