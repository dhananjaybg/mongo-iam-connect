package org.example;
import com.amazonaws.auth.*;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoCredential;
import org.bson.Document;

public class StsTest{
        static private MongoClient mongoClient = null;

        static MongoClient connect_mongo(String roleARN,String connectionString,int refresh_duration){

            String roleSessionName = "SESSION_X";
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().build();
            AssumeRoleRequest roleRequest = new AssumeRoleRequest().withRoleArn(roleARN).withRoleSessionName(roleSessionName).withDurationSeconds (refresh_duration);
            AssumeRoleResult assumeResult = stsClient.assumeRole(roleRequest) ;
            Credentials creds = assumeResult.getCredentials();

            String ACCESS_KEY_ID = creds.getAccessKeyId();
            String SECRET_ACCESS_KEY = creds.getSecretAccessKey();
            String SESSION_TOKEN = creds.getSessionToken();


            MongoCredential credential = MongoCredential.createAwsCredential(ACCESS_KEY_ID, SECRET_ACCESS_KEY.toCharArray()).withMechanismProperty("AWS_SESSION_TOKEN", SESSION_TOKEN);


            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .credential(credential)
                    .build();
            try {
                mongoClient = MongoClients.create(settings);
            } catch (MongoException me) {
                System.err.println("Unable to connect to the MongoDB instance due to an error: " + me);
                System.exit(1);
            }

            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!123 ");
            } catch (MongoException me) {
                System.err.println("Unable to connect to the MongoDB instance due to an error: " + me);
                System.exit(1);
            }

            return mongoClient;
        }

        public String fetch()
        {
            String out_str ="";

            String roleARN = System.getenv("ROLE_ARN");
            String connectionString = System.getenv("ATLAS_URI");
            int duration = Integer.valueOf(System.getenv("DURATION"));

            mongoClient = connect_mongo(roleARN,connectionString,duration);

            if (mongoClient != null) {
                MongoDatabase database = mongoClient.getDatabase("test");
                for (String name : database.listCollectionNames()) {
                    out_str += name + "\n";
                }

            }
            return "market_here_aa : "+out_str;
        }
}
