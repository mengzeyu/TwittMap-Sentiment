# TwittMap-sentiment

### Group member: Zeyu Meng(zm649) Bingxin Chen(bc1958) 

![alt text](https://raw.githubusercontent.com/ChronoResister/TwittMap-sentiment/master/sc1.jpeg "Screenshot1")
![alt text](https://raw.githubusercontent.com/ChronoResister/TwittMap-sentiment/master/sc2.jpeg "Screenshot2")
![alt text](https://raw.githubusercontent.com/ChronoResister/TwittMap-sentiment/master/sc3.jpeg "Screenshot3")

##Using Java Servlet and Tomcat 

- BacnEnd.java: Run on aws beanstalk worker environment. Backend of the Java Servlet. Load processed tweets from ElasticSearch.
- GetTweet.java: Streams tweets containing the selected key words and sends them to AWS SQS queue.
- TweetProcess.java: Worker uses Alchemy API to perform sentimental analysis, stores the processed tweets in ElasticSearch. It also Uses AWS SNS to notify the frontend when new tweets come.
- Tweet.java: defines a Tweet class, make it easier to convert the data into JSON format.
- frontend: uses markers with different colors to represent different sentiment. Green - positive, blue - neural, red - negative. Staying mouse on markers will show the twitter text. Initial webpage shows all the keywords. Uses jQuery API to get newest tweet in realtime.




