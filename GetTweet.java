package TweetStreamFilter.TweetStreamFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class App 
{
    
    	 public static void main(String[] args) throws Exception {

    	        /*
    	         * The ProfileCredentialsProvider will return your [default]
    	         * credential profile by reading from the credentials file located at
    	         * (~/.aws/credentials).
    	         */
    	        AWSCredentials credentials = null;
    	        try {
    	            credentials = new ProfileCredentialsProvider().getCredentials();
    	        } catch (Exception e) {
    	            throw new AmazonClientException(
    	                    "Cannot load the credentials from the credential profiles file. " +
    	                    "Please make sure that your credentials file is at the correct " +
    	                    "location (~/.aws/credentials), and is in valid format.",
    	                    e);
    	        }

    	        final AmazonSQS sqs = new AmazonSQSClient(credentials);
    	        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
    	        sqs.setRegion(usEast1);

    	        

    	        try {
    	            // Create a queue
    	            CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue");
    	            final String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
    	            StatusListener listener = new StatusListener(){
         	        public void onStatus(Status status) {
    	    					if(status.getGeoLocation()!=null){
								List<Double> geo=new ArrayList<Double>();
								geo.add(status.getGeoLocation().getLongitude());
								geo.add(status.getGeoLocation().getLatitude());
								Map<String, Object> json = new HashMap<String, Object>();
								json.put("geo", geo);
								json.put("user", status.getUser().getName());
								json.put("time", status.getCreatedAt().getTime());
								json.put("text", status.getText());
								json.put("sentiment","");
								String message=new Gson().toJson(json);
								//MessageAttributeValue value=new MessageAttributeValue().withStringValue("True");
								// Send a message
								sqs.sendMessage(new SendMessageRequest(myQueueUrl, message)); 
								//.addMessageAttributesEntry("processed",value));
								System.out.println("good");
								}
    	        	    		
    	        	        
    	        	         
    	        	        }
    	        	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
    	        	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
    	        	        public void onException(Exception ex) {
    	        	            ex.printStackTrace();
    	        	        }
    	        			public void onScrubGeo(long arg0, long arg1) {
    	        				// TODO Auto-generated method stub
    	        				
    	        			}
    	        			public void onStallWarning(StallWarning arg0) {
    	        				// TODO Auto-generated method stub
    	        				
    	        			}
    	        	    };
    	        	    
                    String consumerKey = "***", consumerSecret = "***", accessToken = "***", accessTokenSecret = "***";
    	        	    ConfigurationBuilder cb = new ConfigurationBuilder();
    	        	    cb.setDebugEnabled(true)
    	        	      .setOAuthConsumerKey(consumerKey)
    	        	      .setOAuthConsumerSecret(consumerSecret)
    	        	      .setOAuthAccessToken(accessToken)
    	        	      .setOAuthAccessTokenSecret(accessTokenSecret);
    	        	    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    	        	    twitterStream.addListener(listener);
    	      
    	        	    String[] keywordsArray={"Google","New York","Trump","Hillary","Brooklyn","NBA","football","cloud"};
    	        	    String[] languages={"en"};
    	        	    twitterStream.filter(new FilterQuery().track(keywordsArray).language(languages));
    	            
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
