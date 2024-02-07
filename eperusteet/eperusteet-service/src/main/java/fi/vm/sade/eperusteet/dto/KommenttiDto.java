package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KommenttiDto {
    private String nimi;
    private String muokkaaja;
    private String sisalto;
    private Date luotu;
    private Date muokattu;
    private Long id;
    private Long ylinId;
    private Long parentId;
    private Long perusteprojektiId;
    private Boolean poistettu;
    private Long perusteenOsaId;
    private String suoritustapa;
}
