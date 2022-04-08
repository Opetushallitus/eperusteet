package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerusteenJulkaisuData {

    private Map<String, String> nimi;
    private Long voimassaoloAlkaa;
    private Long voimassaoloLoppuu;
    private Long siirtymaPaattyy;
    private Long perusteId;
    private String diaarinumero;
    private Set<KoodiDto> osaamisalat;
    private List<TutkintonimikeKoodiDto> tutkintonimikkeet;
    private String koulutustyyppi;
    private Integer laajuus;
    private List<KoulutusDto> koulutukset;

}
