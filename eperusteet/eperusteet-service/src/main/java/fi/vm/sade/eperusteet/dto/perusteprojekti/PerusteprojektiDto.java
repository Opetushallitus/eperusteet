package fi.vm.sade.eperusteet.dto.perusteprojekti;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.ProjektiKuvaus;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class PerusteprojektiDto implements Serializable {
    private Long id;
    private String nimi;
    private Reference peruste;
    private String diaarinumero;
    private Date paatosPvm;
    private Date toimikausiAlku;
    private Date toimikausiLoppu;
    private String tehtavaluokka;
    private String tehtava;
    private String yhteistyotaho;
    private ProjektiKuvaus projektiKuvaus = ProjektiKuvaus.PERUSTEEN_KORJAUS;
    private LokalisoituTekstiDto kuvaus;
    private ProjektiTila tila;
    private String ryhmaOid;
    private boolean esikatseltavissa = false;
    private List<TavoitepaivamaaraDto> tavoitepaivamaarat = new ArrayList<>();

    public PerusteprojektiDto(String nimi, Reference peruste, String diaarinumero, Date paatosPvm, Date toimikausiAlku, Date toimikausiLoppu, String tehtavaluokka, String tehtava, String yhteistyotaho, ProjektiTila tila, String ryhmaOid) {
        this.nimi = nimi;
        this.peruste = peruste;
        this.diaarinumero = diaarinumero;
        this.paatosPvm = paatosPvm;
        this.toimikausiAlku = toimikausiAlku;
        this.toimikausiLoppu = toimikausiLoppu;
        this.tehtavaluokka = tehtavaluokka;
        this.tehtava = tehtava;
        this.yhteistyotaho = yhteistyotaho;
        this.tila = tila;
        this.ryhmaOid = ryhmaOid;
    }

}
