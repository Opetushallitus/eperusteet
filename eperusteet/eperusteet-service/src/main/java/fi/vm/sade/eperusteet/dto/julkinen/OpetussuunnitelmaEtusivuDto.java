package fi.vm.sade.eperusteet.dto.julkinen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpetussuunnitelmaEtusivuDto {
    private Long id;
    private KoulutusTyyppi koulutustyyppi;
    private Map<String, String> nimi;
    private Date voimaantulo;
    private Date voimassaoloLoppuu;
    private AmosaaKoulutustoimijaDto koulutustoimija;
    private AmosaaCachedPeruste peruste;
    private Set<YlopsOrganisaatioDto> organisaatiot;
    private Set<Kieli> julkaisukielet;

    public KoulutusTyyppi getKoulutustyyppi() {
        if (peruste != null) {
            return peruste.getKoulutustyyppi();
        }

        return koulutustyyppi;
    }
}
