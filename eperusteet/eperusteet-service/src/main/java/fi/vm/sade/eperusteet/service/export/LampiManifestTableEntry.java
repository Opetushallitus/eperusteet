package fi.vm.sade.eperusteet.service.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LampiManifestTableEntry {
    private String key;
    private String s3Version;
}
