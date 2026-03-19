package fi.vm.sade.eperusteet.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LampiExportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private S3AsyncClient lampiS3Client;

    @Autowired
    private S3TransferManager.Builder s3tranferManagerBuilder;

    @Autowired
    private List<LampiExportDataProvider> dataProviders;

    @Value("${eperusteet.export.lampi-bucket:''}")
    private String bucketName;

    @Value("${eperusteet.export.lampi.enabled:false}")
    private boolean enabled;

    @Value("${eperusteet.export.lampi.temp-files.delete:true}")
    private boolean deleteTempFiles;

    private final String bucketPath = "fulldump/eperusteet/v1/";
    private final String manifestFileName = "manifest.json";

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    public List<String> listUploadedFiles() {
        log.info("Listing files in S3 bucket: {}", bucketName);

        if (!enabled) {
            log.info("Lampi export is disabled");
            return List.of();
        }

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(bucketPath)
                .build();

        CompletableFuture<ListObjectsV2Response> futureResponse = lampiS3Client.listObjectsV2(listObjectsRequest);
        return futureResponse.thenApply(response ->
                response.contents().stream()
                        .map(S3Object::key)
                        .collect(Collectors.toList())
        ).join();
    }

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    public void export() throws IOException, JSONException {
        log.info("Exporting data to S3 bucket: {}", bucketName);
        if (!enabled) {
            log.info("Lampi export is disabled");
            return;
        }

        if (ObjectUtils.isEmpty(bucketName)) {
            throw new IllegalArgumentException("Bucket name is required");
        }

        List<File> tempFiles = new ArrayList<>();
        List<LampiManifestTableEntry> tableEntries = new ArrayList<>();

        try {
            for (LampiExportDataProvider provider : dataProviders) {
                File csvFile = generateCsvFile(provider.getQuery(), provider.getCsvFileName());
                tempFiles.add(csvFile);

                String s3Key = bucketPath + provider.getCsvFileName();
                PutObjectResponse response = uploadToS3(csvFile, s3Key);
                tableEntries.add(new LampiManifestTableEntry(s3Key, response.versionId()));
            }

            File manifestFile = createManifestFile(tableEntries);
            tempFiles.add(manifestFile);
            uploadToS3(manifestFile, bucketPath + manifestFileName);
        } finally {
            if (deleteTempFiles) {
                for (File file : tempFiles) {
                    if (file != null && file.exists()) {
                        Files.deleteIfExists(file.toPath());
                    }
                }
            }
        }
    }

    private File generateCsvFile(String query, String csvFileName) throws IOException {
        log.info("Generating CSV file {} from query", csvFileName);
        File csvFile = new File(csvFileName);
        try (FileWriter writer = new FileWriter(csvFile);
            CSVWriter csvWriter = new CSVWriter(writer)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
            log.info("Found {} rows for {}", rows.size(), csvFileName);

            if (!rows.isEmpty()) {
                csvWriter.writeNext(rows.get(0).keySet().toArray(new String[0]));
                for (Map<String, Object> row : rows) {
                    csvWriter.writeNext(row.values().stream().map(String::valueOf).toArray(String[]::new));
                }
            }

            log.info("CSV file generated: {}", csvFile.getAbsolutePath());
        }
        return csvFile;
    }

    private PutObjectResponse uploadToS3(File file, String key) {
        log.info("Uploading file to S3: {} -> {}", file.getAbsolutePath(), key);
        try (S3TransferManager uploader = s3tranferManagerBuilder.s3Client(lampiS3Client).build()) {
            FileUpload fileUpload = uploader.uploadFile(UploadFileRequest.builder()
                    .putObjectRequest(b -> b.bucket(bucketName).key(key))
                    .source(file)
                    .build());
            CompletedFileUpload result = fileUpload.completionFuture().join();
            return result.response();
        }
    }

    private File createManifestFile(List<LampiManifestTableEntry> tableEntries) throws IOException {
        log.info("Creating manifest.json with {} tables", tableEntries.size());

        LampiManifest manifest = new LampiManifest(tableEntries);
        ObjectMapper mapper = new ObjectMapper();

        File manifestFile = new File(manifestFileName);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(manifestFile), StandardCharsets.UTF_8)) {
            writer.write(mapper.writeValueAsString(manifest));
        }

        return manifestFile;
    }

    @PreAuthorize("hasPermission(null, 'pohja', 'LUKU')")
    public byte[] downloadManifest() throws IOException {
        if (!enabled) {
            throw new IOException("Lampi export is disabled");
        }
        String key = bucketPath + manifestFileName;
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            CompletableFuture<ResponseBytes<GetObjectResponse>> future = lampiS3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes());
            return future.get().asByteArray();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to download manifest.json from S3", e);
        }
    }

    @PreAuthorize("hasPermission(null, 'pohja', 'LUKU')")
    public byte[] downloadCsv(String csvFileName) throws IOException {
        if (!enabled) {
            throw new IOException("Lampi export is disabled");
        }
        String key = bucketPath + csvFileName;
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            CompletableFuture<ResponseBytes<GetObjectResponse>> future = lampiS3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes());
            return future.get().asByteArray();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to download " + csvFileName + " from S3", e);
        }
    }
}
