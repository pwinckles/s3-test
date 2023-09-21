# S3 client test

1. Download a copy of the latest jar from the [releases page](https://github.com/pwinckles/s3-test/releases).
2. Execute (with the correct version number) `java -jar s3-test-1.0.0-exec.jar`

And supply the necessary information as prompted. The tester will then attempt to write a test object to the bucket
using the standard AWS async client as well as with the new CRT client. It will **not** delete any objects that are
successfully written -- you will need to manually delete them.

Look for lines in the output like the following:

```
SUCCESS: The standard async client successfully wrote...
FAILURE: The CRT client failed to write... 
```