package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.Query;
import twitter4j.QueryResult;
import javax.inject.Inject;
import play.api.Logger;
import com.google.inject.Singleton;



import models.UserTweet;
import play.libs.Json;
import play.libs.ws.*;


import org.json.*;


public class TweetsService {

	
	/**
	 * Service for getting authentication token for Twitter API
	 *  @author Vivek Haribal
	 * @author Karthik Ramachandran Murugesan
	 * @author Gaganpreet Singh
	 * @author Dilsher Dhami
	 * @version 1.0.0
	 */
	private final TwitterAPIAuthenticator twitterAuthenticator;

	/**
	 * Creates this service
	 * @param twitterAuth Twitter Authentication service that provide auth token
	 */
	@Inject
	public TweetsService(TwitterAPIAuthenticator twitterAuthenticator) {		
		this.twitterAuthenticator = twitterAuthenticator;
	}

	public static List<Status> statuses = new ArrayList<>();

	/**
	 * Retrieves 10 most recent tweets for each of search phrases provided as input
	 * 
	 * @param searchStrings list of phrases for which to retrieve 10 most recent tweets  
	 * @return map of strings with list of latest tweets for the given search strings
	 */

	public CompletionStage<Map<Map<String, String>, List<Status>>> getTweets(List<String> searchKeywords) {

		return searchKeywords
				.stream()
				.map(searchKeyword ->  getTenTweets(searchKeyword))
				.reduce((a, b) -> a.thenCombine(b, (x, y) -> {
					x.putAll(y);
					return x;
				})).get();
	}


	private CompletionStage<Map<Map<String, String>, List<Status>>> getTenTweets(String searchString) {
		
		Twitter twitter = twitterAuthenticator.verifyCredentials();
		
		Query query = new Query(searchString);

		return (CompletionStage<Map<Map<String, String>, List<Status>>>) CompletableFuture.supplyAsync(() -> {
			try {
				QueryResult queryResult = twitter.search(query);
				List<Status> tweets = queryResult.getTweets();
				List<Status> tweetsLimited = tweets.stream().limit(10).collect(Collectors.toList());
				for(Status s : tweetsLimited) {
					TweetsService.statuses.add(s);	
				}				
				Map<Map<String, String>, List<Status>> searchSentimentStatusMap = new LinkedHashMap<>();

				String sentiment = getSentiment(tweetsLimited);
				Map<String, String> sentimentMap = new LinkedHashMap<>();
				sentimentMap.put(searchString, sentiment);	
				searchSentimentStatusMap.put(sentimentMap, tweetsLimited);
				return searchSentimentStatusMap;
			} catch(Exception e) {
				return null;
			}
		});


	}

	/**
	 * Retrieves whether the tweets retrieved for a search query are overall  happy, sad or neutral
	 * Counts the number of emoticons and displays happy emoticon if the total emoticons have 70% or more
	 * happy emoticons or displays sad emoticon if the total emoticons have 70% or more sad emoticons 
	 * else displays a neutral emoticon 
	 * 
	 * @param happyCount 	 number of happy emoticons
	 * @param sadCount 		 number of sad emoticons 
	 * @param sentimentCount number of emoticons 
	 * @return 				 string of happy, sad or neutral emoticon based on the number of emoticons present in the tweets
	 * 
	 */
	
	public String getSentiment(List<Status> tweets) {

		String allText = "";
		for(Status s : tweets) {
			allText = allText + " " + s.getText();
		}
		int sentimentCount = 0;
		int happyCount = 0;
		int sadCount = 0;
		for(String s : allText.split(" ")) {
			if(s.equals(":-)") || s.equals(":-(") || s.equals(":-|") ||s.equals(":)")  || s.equals(":("))
				sentimentCount++;

			if(s.equals(":-)") ||s.equals(":)")) 
				happyCount++;
			else if(s.equals(":-(") || s.equals(":(")) 
				sadCount++;
		}
		if(happyCount>= 0.7*sentimentCount) {
			return ":-)";
		}
		else if(sadCount >= 0.7*sentimentCount) {
			return ":-(";
		}
		else return ":-|";		
	}

	/**
	 * Retrieves the latest 10 tweets of the particular user 
	 * 
	 * @param statuses 
	 * @return			CompletionStage list of statuses		 		
	 */
	public CompletionStage<List<Status>> getUserTweets(String screenName) {
		
		Twitter twitter = twitterAuthenticator.verifyCredentials();
		List<Status> statuses = new ArrayList<>();

		return CompletableFuture.supplyAsync(() -> {
			try{
				return twitter.getUserTimeline(screenName).stream().limit(10).collect(Collectors.toList());
			} catch(Exception e) {
				return null;
			}
		});					
	}

	/**
	 * Retrieves the profile information of the particular user
	 * 
	 *
	 * @return 		 completionStage user object
	 * 
	 */
	
	public CompletionStage<User> getUserInfo(String screenName) {
		// TODO Auto-generated method stub
		Twitter twitter = twitterAuthenticator.verifyCredentials();
		return CompletableFuture.supplyAsync(() -> {
			try {
				return twitter.showUser(screenName);
			} catch(Exception e) {
				return null;
			}
		});
	}





}


