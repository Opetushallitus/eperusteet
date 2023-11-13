package fi.vm.sade.eperusteet.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

    @Value("${sqs.aws_region:''}")
    private String awsRegion;

    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }
}
