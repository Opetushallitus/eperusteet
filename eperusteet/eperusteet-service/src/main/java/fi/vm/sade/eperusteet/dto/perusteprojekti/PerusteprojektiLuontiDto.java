package fi.vm.sade.eperusteet.dto.perusteprojekti;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteAikatauluDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Set;
import lombok.*;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown=true)
public class PerusteprojektiLuontiDto extends PerusteprojektiDto {
    private String koulutustyyppi;
    private LaajuusYksikko laajuusYksikko;
    private Long perusteId;
    private ProjektiTila tila;
    private KoulutustyyppiToteutus toteutus;
    private PerusteTyyppi tyyppi;
    private String ryhmaOid;
    private boolean reforminMukainen = true;
    private Date voimassaoloAlkaa;
    private Date lausuntakierrosAlkaa;
    private Date johtokunnanKasittely;
    private Set<PerusteAikatauluDto> perusteenAikataulut;
    private LokalisoituTekstiDto kuvaus;
    private MaaraysDto maarays;

    public PerusteprojektiLuontiDto(String koulutustyyppi, LaajuusYksikko laajuusYksikko, Long perusteId, ProjektiTila tila, PerusteTyyppi tyyppi, String ryhmaOid) {
        this.koulutustyyppi = koulutustyyppi;
        this.laajuusYksikko = laajuusYksikko;
        this.perusteId = perusteId;
        this.tila = tila;
        this.tyyppi = tyyppi;
        this.ryhmaOid = ryhmaOid;
    }
}
