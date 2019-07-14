package controllers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import views.html.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.api.Logger;
import io.jsonwebtoken.lang.Arrays;

import models.UserTweet;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import services.TweetsService;
import services.UserService;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * Implements controller that handles requests for searching tweets displays Tweeter's user profiles.
 *  
 * @author Vivek Haribal
 * @author Karthik Ramachandran Murugesan
 * @author Gaganpreet Singh
 * @author Dilsher Dhami
 * @version 1.0.0
 *
 */
public class HomeController extends Controller {


	/**
	 * Form Factory binds the request objects
	 */
	private FormFactory formFactory;

	/**
	 * httpExecutionContext manages HTTP thread local state.
	 */
	private HttpExecutionContext httpExecutionContext;

	/**
	 * Tweets Status search service
	 */
	private TweetsService tweetsService;

	/**
	 * Search keywords entered by the user
	 */
	private List<String> searchKeywords = new ArrayList<>();	

	/**
	 * Creates a new controller
	 * @param tweetsService 			Tweets search service
	 * @param formFactory           	Form Factory
	 * 
	 */
	@Inject
	public HomeController(
			FormFactory formFactory,
			TweetsService tweetsService,
			HttpExecutionContext httpExecutionContext) {
		this.formFactory = formFactory;
		this.tweetsService = tweetsService;
		this.httpExecutionContext = httpExecutionContext;

	}


	/**
	 * Displays the index
	 * 
	 * @return  	completionStage result page
	 * 
	 */
	public CompletionStage<Result> index() {
		//return ok(index.render("Your new application is ready."));

		return CompletableFuture.supplyAsync(() -> {
			Form<String> tweetForm = formFactory.form(String.class);
			searchKeywords.clear();
			return ok(home.render(tweetForm, null));
		});
	}

	/**
	 * Displays the tweets based on the search string
	 * 
	 * @return  	completionStage result page with the tweets
	 * 
	 */
	
	public CompletionStage<Result> getTweets() {

		CompletionStage<Form<String>> tweetFormFuture = 
				CompletableFuture.supplyAsync(() -> {

					Form<String> tweetForm = formFactory.form(String.class).bindFromRequest();
					String searchString = tweetForm.field("tweetSearch").getValue().get();

					if (searchKeywords != null) {
						searchKeywords.add(searchString);
					}

					return tweetForm;
				}, httpExecutionContext.current());

		CompletionStage<Map<Map<String, String>, List<Status>>> tweetMap = 
				tweetFormFuture.thenCompose(r -> {

					if (searchKeywords != null) {
						CompletionStage<Map<Map<String, String>, List<Status>>> searchTweets = tweetsService.getTweets(searchKeywords);
						return searchTweets;
					}
					return CompletableFuture.supplyAsync(() -> {
						return null;
					});
				});  		 

		return tweetFormFuture.thenCombine(tweetMap, (form, map) -> ok(home.render(form, map)));
	}

	/**
	 * Displays the tweets of the particular user
	 * 
	 * @return  	completionStage result page with the tweets of a particular user
	 * 
	 */
	public CompletionStage<Result> getUserTweets(String screenName) {

		CompletionStage<User> userInfo = tweetsService.getUserInfo(screenName);

		CompletionStage<List<Status>> userTweets = tweetsService.getUserTweets(screenName);

		return userInfo.thenCombine(userTweets, (user, tweets) -> ok(userpage.render(user, tweets)));

	}


	/**
	 * Displays the overall emotion of the tweets for the search string
	 * 
	 * @return  	completionStage result page with an emoticon
	 * 
	 */
	public CompletionStage<Result> getHashTags(String hashTag) {

		List<String> hashTags = new ArrayList<>();
		hashTags.clear();
		hashTags.add(hashTag);
		Form<String> tweetForm = formFactory.form(String.class).bindFromRequest();

		CompletionStage<Map<Map<String, String>, List<Status>>>  hashTagTweets = tweetsService.getTweets(hashTags);	

		CompletionStage<Result> userTweetsResult = hashTagTweets.thenApply(tweets ->ok(home.render(tweetForm, tweets)));
		return userTweetsResult;
	}

	/**
	 * Displays the number of unique words in the tweets for the search query 
	 * 
	 * @param allTweetText 	 string that stores the tweet text
	 * @return  			 completionStage result page with the word statistics
	 * 
	 */
	public CompletionStage<Result> getWordStats() {

		String allTweetText = "";

		for(Status s : TweetsService.statuses) {
			allTweetText = allTweetText + " " + s.getText();
		}
		
		final String TWEETTEXT = allTweetText;

		return CompletableFuture.supplyAsync(() -> {

			Stream<String> stream = Stream.of(TWEETTEXT.split(" ")).parallel();

			Map<String, Long> wordFreq = stream
					.collect(Collectors.groupingBy(String::toString,Collectors.counting()));

			return ok(wordstats.render(wordFreq));
		});	
	}

}
