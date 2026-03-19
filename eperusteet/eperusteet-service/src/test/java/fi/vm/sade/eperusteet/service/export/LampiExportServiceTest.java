package fi.vm.sade.eperusteet.service.export;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

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
        ReflectionTestUtils.setField(lampiExportService, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(lampiExportService, "lampiS3Client", lampiS3Client);
        ReflectionTestUtils.setField(lampiExportService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(lampiExportService, "enabled", true);

        List<LampiExportDataProvider> dataProviders = List.of(
                new PerusteLampiExportDataProvider(),
                new PerusteArkistoituLampiExportDataProvider()
        );
        ReflectionTestUtils.setField(lampiExportService, "dataProviders", dataProviders);
    }

    @Test
    void testListUploadedFiles_ReturnsKeys() {
        S3Object s3Object = S3Object.builder()
                .key("fulldump/eperusteet/v1/peruste.csv")
                .build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(s3Object)
                .build();

        CompletableFuture<ListObjectsV2Response> future = CompletableFuture.completedFuture(response);

        when(lampiS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(future);

        List<String> files = lampiExportService.listUploadedFiles();

        assertEquals(1, files.size());
        assertEquals("fulldump/eperusteet/v1/peruste.csv", files.get(0));

        ArgumentCaptor<ListObjectsV2Request> requestCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(lampiS3Client).listObjectsV2(requestCaptor.capture());
        ListObjectsV2Request request = requestCaptor.getValue();
        assertEquals("test-bucket", request.bucket());
        assertEquals("fulldump/eperusteet/v1/", request.prefix());
    }

    @Test
    void testListUploadedFiles_WhenDisabled_ReturnsEmptyList() {
        ReflectionTestUtils.setField(lampiExportService, "enabled", false);

        List<String> files = lampiExportService.listUploadedFiles();
        assertTrue(files.isEmpty());

        verifyNoMoreInteractions(lampiS3Client);
    }

    @Test
    void testExport_WhenDisabled_DoesNothing() throws IOException, JSONException {
        ReflectionTestUtils.setField(lampiExportService, "enabled", false);

        lampiExportService.export();

        verifyNoMoreInteractions(jdbcTemplate);
        verifyNoMoreInteractions(lampiS3Client);
    }

    @Test
    void testExport_WithEmptyBucketName_ThrowsException() {
        ReflectionTestUtils.setField(lampiExportService, "bucketName", "");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> lampiExportService.export());
        assertEquals("Bucket name is required", exception.getMessage());
    }

    @Test
    void testGenerateCsvFile() throws Exception {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("peruste_id", 1);
        row1.put("data", "{\"name\":\"Test\"}");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("peruste_id", 2);
        row2.put("data", "{\"name\":\"Test2\"}");

        List<Map<String, Object>> queryResults = Arrays.asList(row1, row2);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(queryResults);

        File result = (File) ReflectionTestUtils.invokeMethod(
                lampiExportService, "generateCsvFile", "SELECT 1", "peruste.csv");

        assertNotNull(result);
        assertEquals("peruste.csv", result.getName());

        verify(jdbcTemplate).queryForList(anyString());

        result.delete();
    }
}
