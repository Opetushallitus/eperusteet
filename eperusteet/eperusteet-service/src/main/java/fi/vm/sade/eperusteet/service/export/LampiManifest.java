package fi.vm.sade.eperusteet.service.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LampiManifest {
    private List<LampiManifestTableEntry> tables;
}
