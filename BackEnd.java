package Backend.Backend;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.google.gson.Gson;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.core.search.sort.Sort;



/**
 * 
 *
 */
public class App extends HttpServlet
{   private AmazonSNSClient snsClient;
	
    public AtomicReference<String> newestTweet =new AtomicReference<String>("");
    
    public String[] keywords={"Google","New York","Trump","Hillary","Brooklyn","NBA","football","cloud"};
    public App ()
    {   
    	String accessKey = "***", secretKey = "###";
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
    	snsClient = new AmazonSNSClient(awsCreds);	
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    	String topicArn = "arn:aws:sns:us-east-1:728697798279:MyNewTopic";
    	
    }
    
    		        
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException{
		//Get the message type header.
		String messagetype = request.getHeader("x-amz-sns-message-type");
		//If message doesn't have the message type header, don't process it.
		if (messagetype == null)
			return;
		if (messagetype.equals("SubscriptionConfirmation"))
		{
       //TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
       //that you want to enable to add this endpoint as a subscription.
			Scanner scan = new Scanner(request.getInputStream());
		    StringBuilder builder = new StringBuilder();
		    while (scan.hasNextLine()) {
		      builder.append(scan.nextLine());
		    }
		   
				Map<String,String> msg = new Gson().fromJson(builder.toString(),Map.class);
       //Confirm the subscription by going to the subscribeURL location 
       //and capture the return value (XML message body as a string)
       Scanner sc = new Scanner(new URL(msg.get("SubscribeURL")).openStream());
       StringBuilder sb = new StringBuilder();
       while (sc.hasNextLine()) {
         sb.append(sc.nextLine());
       }
       
		}
		else{
    // Parse the JSON message in the message body
    // and hydrate a Message object with its contents 
    // so that we have easy access to the name/value pairs 
    // from the JSON message.
    Scanner scan = new Scanner(request.getInputStream());
    StringBuilder builder = new StringBuilder();
    while (scan.hasNextLine()) {
      builder.append(scan.nextLine());
    }
   
		Map<String,String> msg = new Gson().fromJson(builder.toString(),Map.class);
		
		newestTweet.set(msg.get("Message"));
    }
    }
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{ 
    	String res="{";
    	String update=req.getParameter("update");
    	resp.setContentType("application/json");
    	resp.setCharacterEncoding("UTF-8");
    	PrintWriter out=resp.getWriter();
    	if (update==null){
    	
    	String endpoint = "****";
    	JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
		        .Builder(endpoint)
		        
		        .multiThreaded(true)
		        .build());
		JestClient client = factory.getObject();
		for(String Keyword:keywords){
			res=output(resp,Keyword,client,res);
		}
		
		
		StringBuilder res1 = new StringBuilder(res);
		res1.setCharAt(res.length()-1, '}');
		out.write(res1.toString());
		//out.println("</script><script async defer src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDX9tC6vOBjmZJFjSTvoI-kTwZX2kEyrVo&callback=initMap\"></script></body></html>");
		out.close();

    	}
    	else{
    		out.write(newestTweet.get());
    		//out.write("nice");
    		out.close();
    	}
    	}
     public String output( HttpServletResponse resp,String Keyword,JestClient client,String res) throws IOException{
    	 SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
 		searchSourceBuilder.query(QueryBuilders.matchQuery("text", Keyword)).size(50);
 	
 		Search search = new Search.Builder(searchSourceBuilder.toString())
 		                                // multiple index or types can be added.
 		                                .addIndex("tweets")
 		                                .addType("tweets")
 		                                .addSort(new Sort("time",Sort.Sorting.DESC))
 		                                .build();

 		SearchResult result = client.execute(search);
 		String res1=res;
 		if(Keyword=="New York") res1=res1+"\"NewYork\":[";
 		else res1=res1+"\""+Keyword+"\":[";
List<Hit<Map, Void>> hits = result.getHits(Map.class);
	    for(int i=0;i<hits.size();i++ ){
	    	Hit hit=hits.get(i);
			Map<String, Object> json = new HashMap<String, Object>();
			Map source= (Map)hit.source;
			List geo=(List)source.get("geo");
			json.put("longitude",geo.get(0));
			json.put("latitude",geo.get(1));
			json.put("text",source.get("text"));
			json.put("sentiment", source.get("sentiment"));
			String json1= new Gson().toJson(json);
			if(res1.charAt(res1.length()-1)=='[') res1=res1+json1;
			else res1=res1+","+json1;
		}
		    res1=res1+"],";
		    return res1;
		    
		
     }
     
}