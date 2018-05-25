import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;

public class QueryDataStore {
	
	public static void main(String[] args) {
		Datastore datastore = DatastoreOptions.getDefaultInstance().getService(); 
		KeyFactory keyFactory = datastore.newKeyFactory().setKind("Profile");     
		
		// get a user profile
		Entity profile = datastore.get(keyFactory.newKey("User101"));
		System.out.println(profile.getString("Experiment"));		
	}
}
