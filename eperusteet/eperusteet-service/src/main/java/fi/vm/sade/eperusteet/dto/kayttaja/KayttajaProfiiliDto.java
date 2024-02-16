package fi.vm.sade.eperusteet.dto.kayttaja;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KayttajaProfiiliDto {
    private long id;
    private String oid;
    private List<SuosikkiDto> suosikit;
    private List<KayttajaprofiiliPreferenssiDto> preferenssit;
}
