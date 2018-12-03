package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaiteenalaOsaViiteDto {
    private Long id;
    private Integer jarjestys;

    @JsonProperty("_taiteenala")
    private Reference taiteenala;

    @JsonProperty("tutkinnonOsa")
    private TaiteenalaDto taiteenalaDto;

    private Date muokattu;
    private LokalisoituTekstiDto nimi;

    public TaiteenalaOsaViiteDto() {
    }

    public TaiteenalaOsaViiteDto(Integer jarjestys, LokalisoituTekstiDto nimi) {
        this.jarjestys = jarjestys;
        this.nimi = nimi;
    }
}
