# s3-client-side-encryption

This is a sample application for uploading a text file that is encrypted using Amazon S3 client-side encryption to an S3 bucket and also to downoad and decrypt the file.

### Encryption
To encrypt and upload a sample text file you first need to define the following environemnt variables (use your own values):
```
export AWS_REGION=us-east-1
export AWS_PROFILE=default
export BUCKET_NAME=my-bucket-name
export OBJECT_NAME=client-side-encryption.txt
export OBJECT_CONTENT="hello world"
```

and then run the application using the following command:
```
./gradlew run
```
The application prints out the encryption key used to encrypt the file. Take a note of the encryption key, you will need it for decrypting the file.
```
client-side-encryption.txt uploaded successfully to my-bucket-name
Encryption Key: 8v1tSLOr/x0Goy3XgHJ0+IbVezdzCWffC4fp0ExNfUI=
```


### Decryption
To decrypt a file that is encrypted using Amazon S3 client-side encryption, you first need to define the following environemnt variables (use your own values).
Please make sure you specify the same encyption key you used when encrypting the file.
```
export AWS_REGION=us-east-1
export AWS_PROFILE=default
export BUCKET_NAME=my-bucket-name
export OBJECT_NAME=client-side-encryption.txt
export ENCRYPTION_KEY=8v1tSLOr/x0Goy3XgHJ0+IbVezdzCWffC4fp0ExNfUI=
```

and then run the application using the following command:
```
./gradlew run --args="--decrypt"
```

If everything goes well, you will see the original file content.
```
Object Content: hello world
```
