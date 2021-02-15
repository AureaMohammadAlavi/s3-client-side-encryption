
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.*;


import com.amazonaws.services.s3.AmazonS3EncryptionClientV2Builder;
import com.amazonaws.services.s3.AmazonS3EncryptionV2;
import com.amazonaws.ClientConfiguration;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class ClientSideEncryption {

    public static AmazonS3EncryptionV2 getS3Encryption(String region, String profile, SecretKey secretKey) {
        Regions clientRegion = Regions.fromName(region);
        return AmazonS3EncryptionClientV2Builder.standard()
            .withRegion(clientRegion)
            .withCredentials(new ProfileCredentialsProvider(profile))
            .withClientConfiguration(new ClientConfiguration())
            .withCryptoConfiguration(new CryptoConfigurationV2().withCryptoMode(CryptoMode.AuthenticatedEncryption))
            .withEncryptionMaterialsProvider(new StaticEncryptionMaterialsProvider(new EncryptionMaterials(secretKey)))
            .build();
    }

    private String profile;
    private String region;
    private String bucketName;
    private String objectName;
    private String objectContent;
    private String secretKeyBase64;

    private void encrypt() throws NoSuchAlgorithmException {
        readEnvVariables(false);
        
        // Create an encryption key.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom());
        SecretKey secretKey =  keyGenerator.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        try {
            AmazonS3EncryptionV2 s3Encryption = getS3Encryption(region, profile, secretKey);
            s3Encryption.putObject(bucketName, objectName, objectContent);
            s3Encryption.shutdown();
            System.out.println(objectName + " uploaded successfully to " + bucketName);
            System.out.println("Encryption Key: " + encodedKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decrypt() throws NoSuchAlgorithmException {
        readEnvVariables(true);
        try {
            // decode the base64 encoded string
            byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
            // rebuild key using SecretKeySpec
            SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); 

            AmazonS3EncryptionV2 s3Encryption = getS3Encryption(region, profile, secretKey);
            System.out.println("Object Content: " + s3Encryption.getObjectAsString(bucketName, objectName));
            s3Encryption.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    private void readEnvVariables(boolean decrypt) {
        profile = System.getenv("AWS_PROFILE");
        if (profile == null || profile.trim().length() == 0) {
            profile = "default";
        }
        region = System.getenv("AWS_REGION");
        if (region == null || region.trim().length() == 0) {
            System.err.println("Please specify AWS_REGION enviornment variable");
            System.exit(1);
        }
        bucketName = System.getenv("BUCKET_NAME");
        if (bucketName == null || bucketName.trim().length() == 0) {
            System.err.println("Please specify BUCKET_NAME enviornment variable");
            System.exit(1);
        }
        objectName = System.getenv("OBJECT_NAME");
        if (objectName == null || objectName.trim().length() == 0) {
            System.err.println("Please specify OBJECT_NAME enviornment variable");
            System.exit(1);
        }
        objectContent = System.getenv("OBJECT_CONTENT");
        if (objectContent == null || objectContent.trim().length() == 0) {
            objectContent = "sample content for client side encryption";
        }
        
        secretKeyBase64 = System.getenv("ENCRYPTION_KEY");
        if (decrypt) {
            if (secretKeyBase64 == null || secretKeyBase64.trim().length() == 0) {
                System.err.println("Please specify ENCRYPTION_KEY enviornment variable");
                System.exit(1);
            }
        }
    }


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length > 0) {
            if ("--decrypt".equalsIgnoreCase(args[0])) {
                new ClientSideEncryption().decrypt();
            } else {
                System.err.println("only '--decrypt' is accepted");
                System.exit(1);
            }
        } else {
            new ClientSideEncryption().encrypt();
        }
    }
}


