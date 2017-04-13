package TweetProcess.TweetProcess;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
      String accessKey = "***", secretKey = "###";
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
    	final AlchemyLanguage alchemy = new AlchemyLanguage();
      String alchemyKey = "***";
    	alchemy.setApiKey(alchemyKey);
    	final AmazonSQS sqs = new AmazonSQSClient(awsCreds);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usEast1);
        final AmazonSNSClient sns=new AmazonSNSClient(awsCreds);
        sns.setRegion(usEast1);
       // CreateTopicRequest createTopicRequest = new CreateTopicRequest("MyNewTopic");
        //CreateTopicResult createTopicResult = sns.createTopic(createTopicRequest);
        //final String topicArn = createTopicResult.toString();
        final String topicArn="arn:aws:sns:us-east-1:728697798279:MyNewTopic";
        Stirng endpoint = "***";
        JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		        .Builder(endpoint) 
		        .multiThreaded(true)
		        .build());
		final String myQueueUrl = sqs.getQueueUrl("MyQueue").getQueueUrl();
		final JestClient client = factory.getObject();
        try {
            
            while(true){
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
            int threads = Runtime.getRuntime().availableProcessors();
            receiveMessageRequest.setMaxNumberOfMessages(threads);
        	receiveMessageRequest.setVisibilityTimeout(5);
            //System.out.println(receiveMessageRequest.getMaxNumberOfMessages());
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            
            ExecutorService service = Executors.newFixedThreadPool(threads);
            //System.out.println(threads);
            for (final Message message : messages) {
            	//System.out.println("good");
                Runnable runnable = new Runnable() {
                    public void run() {
                     Tweet tweet=new Gson().fromJson(message.getBody(),Tweet.class);
                     Map<String,Object> params = new HashMap<String, Object>();
                     params.put(AlchemyLanguage.TEXT, tweet.getText());
                     DocumentSentiment sentiment = alchemy.getSentiment(params).execute();
                     //System.out.println(sentiment.getSentiment());
                     tweet.setSentiment(sentiment.getSentiment().getType().toString());
                     String source=new Gson().toJson(tweet);
                     Index index = new Index.Builder(source).index("tweets").type("tweets").build();
						try {
							client.execute(index);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String msg = "New Tweet!:"+tweet.getText();
						PublishRequest publishRequest = new PublishRequest(topicArn, msg);
						sns.publish(publishRequest);
						System.out.println("nice");
						String messageReceiptHandle = message.getReceiptHandle();
				        sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));
						
                    }
                };
              service.submit(runnable);
            }

            service.shutdown();
            
 	        
            }
       } catch (AmazonServiceException ase) {
          System.out.println("Caught an AmazonServiceException, which means your request made it " +
                  "to Amazon SQS, but was rejected with an error response for some reason.");
          System.out.println("Error Message:    " + ase.getMessage());
          System.out.println("HTTP Status Code: " + ase.getStatusCode());
          System.out.println("AWS Error Code:   " + ase.getErrorCode());
          System.out.println("Error Type:       " + ase.getErrorType());
          System.out.println("Request ID:       " + ase.getRequestId());
      } catch (AmazonClientException ace) {
          System.out.println("Caught an AmazonClientException, which means the client encountered " +
                  "a serious internal problem while trying to communicate with SQS, such as not " +
                  "being able to access the network.");
          System.out.println("Error Message: " + ace.getMessage());
      }
    }
}
