package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.TiedoteJulkaisuPaikka;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author mikkom
 */
@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TiedoteDto {
    private Long id;
    private Reference perusteprojekti;
    private PerusteKevytDto peruste; // Käytetään ainoastaan haettaessa tiedotteita
    private boolean julkinen;
    private boolean yleinen;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto sisalto;
    private Set<TiedoteJulkaisuPaikka> julkaisupaikat;
    private Set<KoulutusTyyppi> koulutustyypit;
    private Set<PerusteKevytDto> perusteet;
    private Date luotu;
    private String luoja;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nimi;
    private Date muokattu;
    private String muokkaaja;
}
