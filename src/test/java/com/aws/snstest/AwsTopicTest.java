package com.aws.snstest;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformApplicationRequest;
import com.amazonaws.services.sns.model.CreatePlatformApplicationResult;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AwsTopicTest {

	private String topicName = "sns_logo_test";
	private String topicArn = "arn:aws:sns:us-east-1:xxxxxxxxxxxxxxxxxxxxxxxx:sns_logo_test";
	private String applicationArn = "arn:aws:sns:us-east-1:xxxxxxxxxxxxxxxxxxxxxxxx:app/GCM/firebase-application";
	private String deviceToken = "xxxxxxxxxxxxxxxxxxxxxxxx";
	private String targetEndpointArn = "arn:aws:sns:us-east-1:xxxxxxxxxxxxxxxxxxxxxxxx:endpoint/GCM/firebase-application/c9f1f894-cae8-34e7-bd8b-35a14e90e0db";

	@Value("${aws.access_key_id}")
	private String accessKeyId;

	@Value("${aws.secret_access_key}")
	private String secretAccessKey;

	private AmazonSNS getSnsClient() {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
		return AmazonSNSClientBuilder
				.standard()
				.withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.build();
	}

	@Test
	public void createTopic() {
		try{
			//create a new SNS client and set endpoint
			AmazonSNSClient snsClient = (AmazonSNSClient) getSnsClient();
			//create a new SNS topic
			CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
			CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
			//print TopicArn
			System.out.println(createTopicResult);
			//get request id for CreateTopicRequest from SNS metadata
			System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));

			Assert.assertTrue(true);
		} catch(Exception e){
			fail();
		}
	}

	@Test
	public void subscribeTopicAsApplication() {
		try{
			AmazonSNS snsClient = getSnsClient();

			//subscribe to an SNS topic
			SubscribeRequest subRequest = new SubscribeRequest(topicArn, "application", "");
			snsClient.subscribe(subRequest);

			//get request id for SubscribeRequest from SNS metadata
			System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));

			Assert.assertTrue(true);
		} catch(Exception e){
			fail();
		}
	}

	@Test
	public void createApplication() {
		try{
			AmazonSNS snsClient = getSnsClient();

			CreatePlatformApplicationRequest request = new CreatePlatformApplicationRequest()
					.withName("firebase-application")
					.withPlatform("GCM")
					//you can use one of them from firebase project setting(cloud messaging tab)
					.addAttributesEntry("PlatformCredential", "Legacy_Server_key Or Serve_Key");

			CreatePlatformApplicationResult response = snsClient.createPlatformApplication(request);

			System.out.println(response.getPlatformApplicationArn());
			assertTrue(true);
		} catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void createEndpoint() {
		try{
			AmazonSNS snsClient = getSnsClient();

			System.out.println("Creating platform endpoint with token " + deviceToken);
			CreatePlatformEndpointRequest request = new CreatePlatformEndpointRequest()
					.withPlatformApplicationArn(applicationArn)
					.withToken(deviceToken);

			request.setCustomUserData("Ercan");
			CreatePlatformEndpointResult respont = snsClient.createPlatformEndpoint(request);

			System.out.println(respont.getEndpointArn());

			assertTrue(true);
		} catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void subscribeTopic() {
		try{
			AmazonSNS snsClient = getSnsClient();

			//subscribe to an SNS topic
			//Protocal names
			//https://docs.aws.amazon.com/sns/latest/api/API_Subscribe.html
			SubscribeRequest subRequest = new SubscribeRequest(topicArn, "email", "badaa@badaa.com");
			snsClient.subscribe(subRequest);

			//get request id for SubscribeRequest from SNS metadata
			System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));

			System.out.println("Check your email and confirm subscription.");
			Assert.assertTrue(true);
		} catch(Exception e){
			fail();
		}
	}

	@Test
	public void publishTopic() {
		try{
			AmazonSNS snsClient = getSnsClient();

			//publish to an SNS topic
			PublishRequest publishRequest = new PublishRequest(topicArn,"hello");
			PublishResult publishResult = snsClient.publish(publishRequest);

			//print MessageId of message published to SNS topic
			System.out.println("MessageId - " + publishResult.getMessageId());
			Assert.assertTrue(true);
		} catch(Exception e){
			fail();
		}
	}


	@Test
	public void publishEndpoint() {
		try{
			AmazonSNS snsClient = getSnsClient();

			// Insert your desired value (in seconds) of TTL here. For example, a TTL of 1 day would be 86,400 seconds.
			Map<String, MessageAttributeValue> messageAttributes = new HashMap<String, MessageAttributeValue>();
			messageAttributes.put("AWS.SNS.MOBILE.GCM.TTL", new MessageAttributeValue().withDataType("String").withStringValue("86400"));

			//publish to an SNS topic
			PublishRequest publishRequest = new PublishRequest();
			publishRequest.setMessageAttributes(messageAttributes);
			String message = "{\"GCM\": \"{ \\\"notification\\\": { \\\"title\\\": \\\"asdf\\\",\\\"text\\\": \\\"asdf\\\" ,\\\"description\\\": \\\"Ercan\\\"} }\"}";
			publishRequest.setMessage(message);
			publishRequest.setMessageStructure("json");
			publishRequest.setTargetArn(targetEndpointArn);
			PublishResult publishResult = snsClient.publish(publishRequest);

			//print MessageId of message published to SNS topic
			System.out.println("MessageId - " + publishResult.getMessageId());

			Assert.assertTrue(true);
		} catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void publishEndpointTimeToLive() {
		try{
			AmazonSNS snsClient = getSnsClient();

			// Insert your desired value (in seconds) of TTL here. For example, a TTL of 1 day would be 86,400 seconds.
			Map<String, MessageAttributeValue> messageAttributes = new HashMap<String, MessageAttributeValue>();
//			https://docs.aws.amazon.com/sns/latest/dg/sns-ttl.html
			messageAttributes.put("AWS.SNS.MOBILE.GCM.TTL", new MessageAttributeValue().withDataType("String").withStringValue("86400"));

			//publish to an SNS topic
			PublishRequest publishRequest = new PublishRequest();
			publishRequest.setMessageAttributes(messageAttributes);
			String message = "{\"GCM\": \"{ \\\"time_to_live\\\": 5, \\\"notification\\\": { \\\"title\\\": \\\"asdf\\\",\\\"text\\\": \\\"asdf\\\" } }\"}";
			publishRequest.setMessage(message);
			publishRequest.setMessageStructure("json");
			publishRequest.setTargetArn(targetEndpointArn);
			PublishResult publishResult = snsClient.publish(publishRequest);

			//print MessageId of message published to SNS topic
			System.out.println("MessageId - " + publishResult.getMessageId());

			Assert.assertTrue(true);
		} catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void publishEndpointAsData() {
		try{
			AmazonSNS snsClient = getSnsClient();

			// Insert your desired value (in seconds) of TTL here. For example, a TTL of 1 day would be 86,400 seconds.
			Map<String, MessageAttributeValue> messageAttributes = new HashMap<String, MessageAttributeValue>();
			// https://docs.aws.amazon.com/sns/latest/dg/sns-ttl.html
			messageAttributes.put("AWS.SNS.MOBILE.GCM.TTL", new MessageAttributeValue().withDataType("String").withStringValue("86400"));

			//publish to an SNS topic
			PublishRequest publishRequest = new PublishRequest();
			publishRequest.setMessageAttributes(messageAttributes);
			String message = "{\"GCM\": \"{ \\\"data\\\": { \\\"message\\\": \\\"sample message\\\"} }\"}";
			publishRequest.setMessage(message);
			publishRequest.setMessageStructure("json");
			publishRequest.setTargetArn(targetEndpointArn);
			PublishResult publishResult = snsClient.publish(publishRequest);

			//print MessageId of message published to SNS topic
			System.out.println("MessageId - " + publishResult.getMessageId());

			Assert.assertTrue(true);
		} catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void publishData() {
		try{
			AmazonSNS snsClient = getSnsClient();

			//publish to an SNS topic
			String msg = "My text published to SNS topic with email endpoint";
			PublishRequest publishRequest = new PublishRequest(topicArn, msg);
			PublishResult publishResult = snsClient.publish(publishRequest);

			//print MessageId of message published to SNS topic
			System.out.println("MessageId - " + publishResult.getMessageId());
			Assert.assertTrue(true);
		} catch(Exception e){
			fail();
		}
	}


	@Test
	public void removeTopic() {
		try{
			AmazonSNS snsClient = getSnsClient();

			//delete an SNS topic
			DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicArn);
			snsClient.deleteTopic(deleteTopicRequest);

			//get request id for DeleteTopicRequest from SNS metadata
			System.out.println("DeleteTopicRequest - " + snsClient.getCachedResponseMetadata(deleteTopicRequest));

			Assert.assertTrue(true);
		} catch(Exception e){
			fail();
		}
	}
}
