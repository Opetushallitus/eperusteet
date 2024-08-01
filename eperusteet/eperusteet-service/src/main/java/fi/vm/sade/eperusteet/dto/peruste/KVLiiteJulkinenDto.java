package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
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
    private PerusteprojektiInfoDto pohjaProjekti;
}
