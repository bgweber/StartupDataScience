package tracking;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.SubscriptionName;

public class ReadEvent {

	public static void main(String[] args) throws Exception {
		
		// set up a message handler 
		MessageReceiver receiver = new MessageReceiver() {
			public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
					
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter("tracking.log", true));
					writer.write(message.getData().toStringUtf8() + "\n");
					writer.close();
					consumer.ack();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		// start the listener for 1 minute 
		SubscriptionName subscriptionName = SubscriptionName.of("your_project_id", "raw-events");
		Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
		subscriber.startAsync(); 
		Thread.sleep(60000);
		subscriber.stopAsync();		
	}
}
