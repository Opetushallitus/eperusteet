package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ammatillisten yhteisten osaamistavoitteet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class OsaamistavoiteDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private boolean pakollinen;
    private BigDecimal laajuus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Kieli kieli;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;

    public String getKoodiUri() {
        KoodiDto koodi = this.getKoodi();
        if (koodi != null) {
            return koodi.getUri();
        } else {
            return koodiUri;
        }
    }

    public String getKoodiArvo() {
        KoodiDto koodi = this.getKoodi();
        if (koodi != null) {
            return koodi.getArvo();
        } else {
            return koodiArvo;
        }
    }
}
