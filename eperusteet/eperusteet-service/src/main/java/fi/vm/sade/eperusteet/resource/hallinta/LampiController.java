package fi.vm.sade.eperusteet.resource.hallinta;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.export.LampiExportService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@InternalApi
@RestController
@RequestMapping(value = "/api/lampi")
@Profile("!test")
public class LampiController {

    @Autowired
    private LampiExportService lampiExportService;

    @GetMapping(value = "/export")
    @ResponseStatus(HttpStatus.CREATED)
    public void exportLampi() throws JSONException, IOException {
        lampiExportService.export();
    }

    @GetMapping(value = "/export/result")
    public List<String> exportResultsLampi() {
        return lampiExportService.listUploadedFiles();
    }

    @GetMapping("/download/manifest")
    public ResponseEntity<byte[]> downloadManifest() throws IOException {
        byte[] data = lampiExportService.downloadManifest();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=manifest.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @GetMapping("/download/csv/{csvFileName}")
    public ResponseEntity<byte[]> downloadCsv(@PathVariable String csvFileName) throws IOException {
        byte[] data = lampiExportService.downloadCsv(csvFileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
