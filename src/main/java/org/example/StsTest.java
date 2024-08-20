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
        static Credentials get_creds( String roleARN ){
            String roleSessionName = "SESSION_X";
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().build();
            // must be 900+
            AssumeRoleRequest roleRequest = new AssumeRoleRequest().withRoleArn(roleARN).withRoleSessionName(roleSessionName).withDurationSeconds (900);
            AssumeRoleResult assumeResult = stsClient.assumeRole(roleRequest) ;
            return  assumeResult.getCredentials();
        }

        static void fetch_roles(Credentials temporaryCredentials){
            AWSCredentials credentials = new BasicSessionCredentials(temporaryCredentials.getAccessKeyId(), temporaryCredentials.getSecretAccessKey(), temporaryCredentials.getSessionToken());
            AWSCredentialsProvider credProvider = new AWSStaticCredentialsProvider(credentials);
            AmazonIdentityManagement client = AmazonIdentityManagementClientBuilder.standard().withCredentials(credProvider).build();

            System.out.println("\n********LIST-ROLES********************");
            client.listRoles().getRoles().forEach(r -> System.out.println(r.getArn()));
        }
        static boolean connect_mongo(Credentials temporaryCredentials){
            String ACCESS_KEY_ID = temporaryCredentials.getAccessKeyId();
            String SECRET_ACCESS_KEY = temporaryCredentials.getSecretAccessKey();
            String SESSION_TOKEN = temporaryCredentials.getSessionToken();

            MongoCredential credential = MongoCredential.createAwsCredential(ACCESS_KEY_ID, SECRET_ACCESS_KEY.toCharArray()).withMechanismProperty("AWS_SESSION_TOKEN", SESSION_TOKEN);
            String connectionString = "mongodb+srv://democluster.c1xrj.mongodb.net/?" +
                    "authSource=external&authMechanism=MONGODB-AWS&retryWrites=true&w=majority&appName=DemoCluster";

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .credential(credential)
                    .build();
            // Create a new client and connect to the server
            try (MongoClient mongoClient = MongoClients.create(settings)) {
                try {
                    // Send a ping to confirm a successful connection
                    MongoDatabase database = mongoClient.getDatabase("admin");
                    database.runCommand(new Document("ping", 1));
                    System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
                    return true;

                } catch (MongoException e) {
                    e.printStackTrace();
                }
            }
            return false;

        }
        public static void main(String[] args)
        {
            String roleARN = "arn:aws:iam::XXXXXXX:role/dev_mdb_role";
            Credentials temporaryCredentials = get_creds(roleARN);
            //fetch_roles(temporaryCredentials);
            boolean flg = connect_mongo(temporaryCredentials);

            System.out.println("connection Complete Status: "+ flg );
        }
}
