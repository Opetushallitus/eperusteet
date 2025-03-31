package fi.vm.sade.eperusteet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@Configuration
public class LampiConfiguration {

    @Value("${eperusteet.export.lampi-role-arn:''}")
    private String lampiRoleArn;
    @Value("${eperusteet.export.lampi-external-id:''}")
    private String lampiExternalId;

    private static final String ROLE_SESSION_NAME = "eperusteet-lampi-export";

    @Bean
    public S3AsyncClient lampiS3Client() {
        return S3AsyncClient.builder()
                .credentialsProvider(lampiCredentialsProvider())
                .region(Region.EU_WEST_1)
                .build();
    }

    private StsAssumeRoleCredentialsProvider lampiCredentialsProvider() {
        var stsClient = StsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.EU_WEST_1)
                .build();

        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(() -> AssumeRoleRequest.builder()
                        .roleArn(lampiRoleArn)
                        .externalId(lampiExternalId)
                        .roleSessionName(ROLE_SESSION_NAME)
                        .build())
                .build();
    }
}
