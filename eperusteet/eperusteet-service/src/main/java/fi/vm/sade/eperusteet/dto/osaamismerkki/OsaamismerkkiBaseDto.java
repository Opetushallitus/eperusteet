package fi.vm.sade.eperusteet.dto.osaamismerkki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.osaamismerkki.OsaamismerkkiTila;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Osaamismerkin perustiedot, osaamistavoitteet ja arviointikriteerit.")
public class OsaamismerkkiBaseDto {

    @Schema(description = "Osaamismerkin tunniste.")
    private Long id;

    @Schema(description = "Osaamismerkin nimi useilla kielillä.")
    private LokalisoituTekstiDto nimi;

    @Schema(description = "Osaamismerkin kuvaus useilla kielillä.")
    private LokalisoituTekstiDto kuvaus;

    @Schema(description = "Osaamismerkin tila (esim. LAADINTA, JULKAISTU). Julkisessa rajapinnassa palautetaan vain julkaistut.")
    private OsaamismerkkiTila tila;

    @Schema(description = "Osaamismerkin kategoria.")
    private OsaamismerkkiKategoriaDto kategoria;

    @Schema(description = "Osaamismerkin koodin URI (esim. osaamismerkit_<koodi>). "
            + "Käytetään yksittäisen merkin haussa polkuparametrina (/api/external/osaamismerkki/koodi/{uri}).")
    private String koodiUri;

    @Schema(description = "Osaamismerkin osaamistavoitteet.")
    private List<OsaamismerkkiOsaamistavoiteDto> osaamistavoitteet = new ArrayList<>();

    @Schema(description = "Osaamismerkin arviointikriteerit.")
    private List<OsaamismerkkiArviointikriteeriDto> arviointikriteerit = new ArrayList<>();

    @Schema(description = "Osaamismerkin voimassaolon alkamispäivä.")
    private Date voimassaoloAlkaa;

    @Schema(description = "Osaamismerkin voimassaolon päättymispäivä.")
    private Date voimassaoloLoppuu;
}
