package fi.vm.sade.eperusteet.dto.perusteprojekti;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class PerusteprojektiKevytDto implements Serializable  {
    private Long id;
    private String nimi;
    private ProjektiTila tila;
    private String perusteendiaarinumero;
    private String diaarinumero;
    private String koulutustyyppi;
    private KoulutustyyppiToteutus toteutus;
    private PerusteTyyppi tyyppi;
    private Set<String> suoritustavat;
    private Date luotu;
    private PerusteVersionDto globalVersion;
    private PerusteBaseDto peruste;

    public PerusteprojektiKevytDto(Long id, String nimi, Diaarinumero perusteDiaari, Diaarinumero diaarinumero,
                                   String koulutustyyppi, PerusteTyyppi tyyppi, ProjektiTila tila) {
        this.id = id;
        this.nimi = nimi;
        this.tila = tila;
        this.perusteendiaarinumero = (perusteDiaari != null) ? perusteDiaari.toString() : null;
        this.diaarinumero = (diaarinumero != null) ? diaarinumero.toString() : null;
        this.koulutustyyppi = koulutustyyppi;
        this.tyyppi = tyyppi;
    }
}
