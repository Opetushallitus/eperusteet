package fi.vm.sade.eperusteet.dto.osaamismerkki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiTila;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class OsaamismerkkiBaseDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private OsaamismerkkiTila tila;
    private OsaamismerkkiKategoriaDto kategoria;
    private String koodiUri;
    private List<OsaamismerkkiOsaamistavoiteDto> osaamistavoitteet = new ArrayList<>();
    private List<OsaamismerkkiArviointikriteeriDto> arviointikriteerit = new ArrayList<>();
    private Date voimassaoloAlkaa;
    private Date voimassaoloLoppuu;
}
