import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

public class SendEvent {

	public static void main(String[] args) throws Exception {
		
		// Set up a publisher 
		TopicName topicName = TopicName.of("your_project_id", "raw-events");
		Publisher publisher = Publisher.newBuilder(topicName).build();
		
        //schedule a message to be published
		String message = "Hello World!";
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(
        		ByteString.copyFromUtf8(message)).build();
        
        // publish the message, and add this class as a callback listener 
        ApiFuture<String> future = publisher.publish(pubsubMessage);
        ApiFutures.addCallback(future, new ApiFutureCallback<String>() {
			public void onFailure(Throwable arg0) {
				System.out.println("Failure: " + arg0);
			}
			public void onSuccess(String arg0) {
				System.out.println("Success: " + arg0);
			}        	
        });          
        
        publisher.shutdown();
	}
}
