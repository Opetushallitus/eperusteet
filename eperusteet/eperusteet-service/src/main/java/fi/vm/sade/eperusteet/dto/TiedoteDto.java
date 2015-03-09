package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mikkom
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TiedoteDto {
    private Long id;
    private EntityReference perusteprojekti;
    private boolean julkinen;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto sisalto;
    private Date luotu;
    private String luoja;
    private String nimi;
    private Date muokattu;
    private String muokkaaja;
}
