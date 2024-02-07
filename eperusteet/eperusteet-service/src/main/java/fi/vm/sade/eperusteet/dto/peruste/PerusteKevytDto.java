package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteKevytDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private PerusteTila tila;
    private PerusteTyyppi tyyppi;
    private String koulutustyyppi;
    private boolean esikatseltavissa;
    private Date voimassaoloAlkaa;
    private Date voimassaoloLoppuu;
    private Reference perusteprojekti;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<SuoritustapaDto> suoritustavat;
}
