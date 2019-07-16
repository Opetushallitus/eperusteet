package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author mikkom
 */
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
    private Date luotu;
    private String luoja;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nimi;
    private Date muokattu;
    private String muokkaaja;
}
