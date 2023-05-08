# S3 client test

To run the test execute the following:

```shell
./mvnw package
cd target
java -jar s3-test-1.0-SNAPSHOT-exec.jar
```

And supply the necessary information as prompted. The tester will then attempt to write a test object to the bucket
using the standard AWS async client as well as with the new CRT client.

Look for lines in the output like the following:

```
SUCCESS: The standard async client successfully wrote...
FAILURE: The CRT client failed to write... 
```