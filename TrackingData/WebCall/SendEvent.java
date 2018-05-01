import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class SendEvent {
	
	public static void main(String[] args) throws Exception {
		
		String endPoint = "http://alumni.soe.ucsc.edu/~bweber/mario/mariolog.php";
		String message = "Hello_World_" + System.currentTimeMillis();

		URL url = new URL(endPoint + "?message=" + message);
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		while (in.readLine() != null) {}
		in.close();		
	}
}
