package fi.vm.sade.eperusteet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.ResumableFileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.TransferProgress;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Profile("local")
@Configuration
public class LampiConfigurationLocal {

    @Bean
    public S3TransferManager.Builder s3tranferManagerBuilder() {
        return mockedTransferManagerBuilder();
    }

    private S3TransferManager.Builder mockedTransferManagerBuilder() {
        return new S3TransferManager.Builder() {
            @Override
            public S3TransferManager.Builder s3Client(S3AsyncClient s3AsyncClient) {
                return this;
            }

            @Override
            public S3TransferManager.Builder executor(Executor executor) {
                return this;
            }

            @Override
            public S3TransferManager.Builder uploadDirectoryFollowSymbolicLinks(Boolean aBoolean) {
                return this;
            }

            @Override
            public S3TransferManager.Builder uploadDirectoryMaxDepth(Integer integer) {
                return this;
            }

            @Override
            public S3TransferManager.Builder transferDirectoryMaxConcurrency(Integer integer) {
                return null;
            }

            @Override
            public S3TransferManager build() {
                return mockedTransferManager();
            }
        };
    }

    private S3TransferManager mockedTransferManager() {
        return new S3TransferManager() {
            @Override
            public void close() {
            }

            @Override
            public FileUpload uploadFile(UploadFileRequest uploadFileRequest) {
                return mockedFileUpload();
            }
        };
    }

    private FileUpload mockedFileUpload() {
        return new FileUpload() {

            @Override
            public TransferProgress progress() {
                return null;
            }

            @Override
            public ResumableFileUpload pause() {
                return null;
            }

            @Override
            public CompletableFuture<CompletedFileUpload> completionFuture() {
                return mockedFuture();
            }
        };
    }

    private CompletableFuture<CompletedFileUpload> mockedFuture() {
        return CompletableFuture.completedFuture(
                CompletedFileUpload
                        .builder()
                        .response(PutObjectResponse.builder()
                                .eTag("mock-etag")
                                .build())
                        .build());
    }
}
