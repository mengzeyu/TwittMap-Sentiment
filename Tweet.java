package TweetProcess.TweetProcess;

import java.util.List;

public class Tweet {
	List<Double> geo;
	String user;
	long time;
	String text;
	String sentiment;
	public List<Double> getGeo(){
		return geo;
	}
	public String getUser(){
		return user;
	}
	public long getTime(){
		return time;
	
	}
	public String getText(){
		return text;
	}
	public void setSentiment(String s){
		this.sentiment=s;
	}
}
