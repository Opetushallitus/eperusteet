package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class Lops2019ModuuliDto implements ReferenceableDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LokalisoituTekstiDto kuvaus;
    private Boolean pakollinen;
    private BigDecimal laajuus;
    private KoodiDto koodi;
    private Lops2019ModuuliTavoiteDto tavoitteet;
    private List<Lops2019ModuuliSisaltoDto> sisallot;
}
