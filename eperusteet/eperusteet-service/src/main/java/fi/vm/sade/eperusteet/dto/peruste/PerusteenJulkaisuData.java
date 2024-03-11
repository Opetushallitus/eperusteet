package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.KoulutusDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import java.util.Date;
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

    private Long id;
    private Map<String, String> nimi;
    private Date voimassaoloAlkaa;
    private Date voimassaoloLoppuu;
    private Date siirtymaPaattyy;
    private Date paatospvm;
    private String diaarinumero;
    private Set<KoodiDto> osaamisalat;
    private List<TutkintonimikeKoodiDto> tutkintonimikkeet;
    private String koulutustyyppi;
    private Integer laajuus;
    private List<KoulutusDto> koulutukset;
    private Set<SuoritustapaDto> suoritustavat;
    private List<String> koodit;
    private Date julkaistu;
    private Long luotu;
    private String tyyppi;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TutkinnonOsaKaikkiDto tutkinnonosa;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PerusteenJulkaisuData> perusteet;

    private String sisaltotyyppi;
}
