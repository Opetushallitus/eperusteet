package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlueTyyppi;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsaAlueDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;

    @ApiModelProperty("Määrittää osaamisalueiden tyypit (vanha vai uusi)")
    private OsaAlueTyyppi tyyppi;
    private KoodiDto koodi;
    private String koodiUri;
    private String koodiArvo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KoodiDto kielikoodi;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ValmaTelmaSisaltoDto valmaTelmaSisalto;

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
