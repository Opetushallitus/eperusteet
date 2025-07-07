package fi.vm.sade.eperusteet.service.export;

import com.opencsv.CSVWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LampiExportServiceTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private S3AsyncClient lampiS3Client;

    private LampiExportService lampiExportService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        lampiExportService = new LampiExportService();
        // Set dependencies using ReflectionTestUtils since fields are @Autowired
        ReflectionTestUtils.setField(lampiExportService, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(lampiExportService, "lampiS3Client", lampiS3Client);

        // Initialize the service configuration values
        ReflectionTestUtils.setField(lampiExportService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(lampiExportService, "enabled", true);
    }

    @Test
    void testListUploadedFiles_ReturnsKeys() {
        // Setup mock S3 response
        S3Object s3Object = S3Object.builder()
                .key("fulldump/eperusteet/v1/peruste.csv")
                .build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(s3Object)
                .build();

        CompletableFuture<ListObjectsV2Response> future = CompletableFuture.completedFuture(response);

        when(lampiS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(future);

        // Execute the method
        List<String> files = lampiExportService.listUploadedFiles();

        // Verify results
        assertEquals(1, files.size());
        assertEquals("fulldump/eperusteet/v1/peruste.csv", files.get(0));

        // Verify the correct bucket and prefix were used
        ArgumentCaptor<ListObjectsV2Request> requestCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(lampiS3Client).listObjectsV2(requestCaptor.capture());
        ListObjectsV2Request request = requestCaptor.getValue();
        assertEquals("test-bucket", request.bucket());
        assertEquals("fulldump/eperusteet/v1/", request.prefix());
    }

    @Test
    void testListUploadedFiles_WhenDisabled_ReturnsEmptyList() {
        // Disable the service
        ReflectionTestUtils.setField(lampiExportService, "enabled", false);

        // Execute and verify
        List<String> files = lampiExportService.listUploadedFiles();
        assertTrue(files.isEmpty());

        // Verify S3 client was not called
        verifyNoMoreInteractions(lampiS3Client);
    }

    @Test
    void testExport_WhenDisabled_DoesNothing() throws IOException, JSONException {
        // Disable the service
        ReflectionTestUtils.setField(lampiExportService, "enabled", false);

        // Execute
        lampiExportService.export();

        // Verify no interactions
        verifyNoMoreInteractions(jdbcTemplate);
        verifyNoMoreInteractions(lampiS3Client);
    }

    @Test
    void testExport_WithEmptyBucketName_ThrowsException() {
        // Set empty bucket name
        ReflectionTestUtils.setField(lampiExportService, "bucketName", "");

        // Execute and verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> lampiExportService.export());
        assertEquals("Bucket name is required", exception.getMessage());
    }

    @Test
    void testGenerateCsvFile() throws Exception {
        // Mock database results
        Map<String, Object> row1 = new HashMap<>();
        row1.put("peruste_id", 1);
        row1.put("data", "{\"name\":\"Test\"}");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("peruste_id", 2);
        row2.put("data", "{\"name\":\"Test2\"}");

        List<Map<String, Object>> queryResults = Arrays.asList(row1, row2);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(queryResults);

        // Use reflection to access the private method
        File result = (File) ReflectionTestUtils.invokeMethod(lampiExportService, "generateCsvFile");

        // Verify
        assertNotNull(result);
        assertTrue(result.getName().equals("peruste.csv"));

        // Verify database was queried
        verify(jdbcTemplate).queryForList(anyString());
    }

    // Helper method for testing uploadToS3 that allows injecting a mock S3TransferManager
    private PutObjectResponse uploadToS3WithManager(File file, String key, S3TransferManager transferManager) {
        try {
            FileUpload fileUpload = transferManager.uploadFile(UploadFileRequest.builder()
                    .putObjectRequest(b -> b.bucket("test-bucket").key(key))
                    .source(file)
                    .build());
            CompletedFileUpload result = fileUpload.completionFuture().join();
            return result.response();
        } finally {
            // No need to close the manager as it's provided externally
        }
    }
}
