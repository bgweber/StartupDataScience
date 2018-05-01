import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class TrackingServer extends AbstractHandler {

	public static void main(String[] args) throws Exception {
    	
		Server server = new Server(8080);
        server.setHandler(new TrackingServer());
        server.start();
        server.join();
    }
	
	public void handle(String target,Request baseRequest, HttpServletRequest request, 
			HttpServletResponse response) throws IOException, ServletException {
    	
		String message = baseRequest.getParameter("message");
		if (message != null) {
			BufferedWriter writer = new BufferedWriter(new FileWriter("tracking.log", true));
			writer.write(message + "\n");
			writer.close();
		}

        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }        
} 
