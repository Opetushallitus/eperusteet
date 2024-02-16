package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsaamisalaDto {
    private LokalisoituTekstiDto nimi;
    private String osaamisalakoodiArvo;
    private String osaamisalakoodiUri;

    static public OsaamisalaDto of(String arvo) {
        OsaamisalaDto result = new OsaamisalaDto();
        result.setOsaamisalakoodiArvo(arvo);
        result.setOsaamisalakoodiUri("osaamisala_" + arvo);
        return result;
    }
}
