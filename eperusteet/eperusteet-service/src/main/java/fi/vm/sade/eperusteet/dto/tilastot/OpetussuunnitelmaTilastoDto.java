package fi.vm.sade.eperusteet.dto.tilastot;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class OpetussuunnitelmaTilastoDto {

    private List<Object> data;
    @JsonProperty("kokonaismäärä")
    private Integer kokonaismaara;
    private Integer sivu;
    private Integer sivuja;
    private Integer sivukoko;

}
