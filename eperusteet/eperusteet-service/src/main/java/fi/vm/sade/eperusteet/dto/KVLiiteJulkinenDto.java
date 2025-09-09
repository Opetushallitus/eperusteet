package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteTasoDto;
import fi.vm.sade.eperusteet.dto.peruste.MaarayskirjeDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KVLiiteJulkinenDto extends KVLiiteDto {
    private LokalisoituTekstiDto nimi;
    private String koulutustyyppi;
    private LokalisoituTekstiDto kuvaus;
    private MaarayskirjeDto maarayskirje;
    private String diaarinumero;
    private Date voimassaoloAlkaa;
    private List<KVLiiteTasoDto> tasot = new ArrayList<>();
    private Map<Suoritustapakoodi, LokalisoituTekstiDto> muodostumisenKuvaus;
    private Boolean periytynyt;
}
