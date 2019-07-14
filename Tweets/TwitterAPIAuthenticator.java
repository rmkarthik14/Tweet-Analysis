package services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import twitter4j.*;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import twitter4j.conf.*;
import play.libs.ws.WSClient;

import play.libs.ws.*;


/**
 * Service for retrieving a security token required for authentication when using Twitter API
 *@author Vivek Haribal
 * @author Karthik Ramachandran Murugesan
 * @author Gaganpreet Singh
 * @author Dilsher Dhami
 * @version 1.0.0
  */

@Singleton
public class TwitterAPIAuthenticator {

	/**
	 * Consumer key of the Twitter application developer account
	 */
	private static final String TWITTER_CONSUMER_KEY = "eyIgNHnPXKVkKi0haryWI7UNZ";

	/**
	 * Consumer secret of the Twitter application developer account
	 */
	private static final String TWITTER_CONSUMER_SECRET = "3WEQjOblxMqSut5Hyr5bSPrvuNpDbfDeFDzafKOJUXxBBcUxKf";

	
	/**
	 * Web services client
	 */
	private final WSClient wsClient;

	@Inject
	public TwitterAPIAuthenticator(WSClient wsClient) {
		this.wsClient = wsClient;
	}

	/**
	 * Verifies the Twitter access credentials
	 * 
	 * @return twitter object 
	 */
	public Twitter verifyCredentials() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
		.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET)
		.setOAuthAccessToken("415485107-6rE2JkuLAppBQXmlH4J9XSRkyzgkxsuAe68IvpZI")
		.setOAuthAccessTokenSecret("fcRIMzhrQrsPLf7YV7LDdQcfJgjGIW0xVssIIFflW1cM6");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		return twitter;
	}
}
