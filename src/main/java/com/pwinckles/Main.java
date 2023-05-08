package com.pwinckles;

import java.net.URI;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var config = readConfig();

        testOriginalClient(config);
        testCrtClient(config);
    }

    private static void runTest(String type, String key, S3AsyncClient client, Config config) {
        try {
            log.info("Attempting to write object {} to bucket {}", key, config.bucket);
            client.putObject(request -> request.bucket(config.bucket).key(key), AsyncRequestBody.fromString("testing"))
                    .get();
            log.info("SUCCESS: The {} client successfully wrote {} to {}", type, key, config.bucket);
        } catch (Exception e) {
            log.error("FAILURE: The {} client failed to write {} to {}", type, key, config.bucket, e);
        }
    }

    private static void testOriginalClient(Config config) {
        log.info("Testing standard async client...");

        var clientBuilder = S3AsyncClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.builder()
                        .profileName(config.profile)
                        .build())
                .region(Region.of(config.region));

        if (config.endpoint != null) {
            clientBuilder.endpointOverride(URI.create(config.endpoint));
        }

        var client = clientBuilder.build();

        var key = config.prefix + "test-standard-" + System.currentTimeMillis() + ".txt";

        runTest("standard async", key, client, config);
    }

    private static void testCrtClient(Config config) {
        log.info("Testing CRT client...");

        Log.initLoggingToFile(Log.LogLevel.Trace, "aws-sdk.log");
        var clientBuilder = S3AsyncClient.crtBuilder()
                .credentialsProvider(ProfileCredentialsProvider.builder()
                        .profileName(config.profile)
                        .build())
                .region(Region.of(config.region));

        if (config.endpoint != null) {
            clientBuilder.endpointOverride(URI.create(config.endpoint));
        }

        var client = clientBuilder.build();

        var key = config.prefix + "test-crt-" + System.currentTimeMillis() + ".txt";

        runTest("CRT", key, client, config);
    }

    private static Config readConfig() {
        var reader = new Scanner(System.in);

        System.out.print("Profile [default]: ");
        var profile = defaulted(reader.nextLine(), "default");

        System.out.print("Region [us-east-2]: ");
        var region = defaulted(reader.nextLine(), "us-east-2");

        System.out.print("Endpoint: ");
        var endpoint = defaulted(reader.nextLine(), null);

        String bucket = null;
        while (bucket == null) {
            System.out.print("Bucket: ");
            bucket = defaulted(reader.nextLine(), null);
        }

        System.out.print("Prefix: ");
        var prefix = defaulted(reader.nextLine(), "");
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        return new Config(profile, region, endpoint, bucket, prefix);
    }

    private static String defaulted(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static class Config {
        private final String profile;
        private final String region;
        private final String endpoint;
        private final String bucket;
        private final String prefix;

        private Config(String profile, String region, String endpoint, String bucket, String prefix) {
            this.profile = profile;
            this.region = region;
            this.endpoint = endpoint;
            this.bucket = bucket;
            this.prefix = prefix;
        }
    }
}
