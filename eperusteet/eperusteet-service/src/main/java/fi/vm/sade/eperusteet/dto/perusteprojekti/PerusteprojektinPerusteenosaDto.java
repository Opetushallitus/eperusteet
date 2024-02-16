package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import java.io.Serializable;
import java.util.Date;

import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteprojektinPerusteenosaDto implements Serializable  {
    private Long id;
    private String nimi;
    private ProjektiTila tila;
    private String perusteendiaarinumero;
    private String diaarinumero;
    private String koulutustyyppi;
    private PerusteTyyppi tyyppi;
    private Date luotu;
    private PerusteInfoDto peruste;
}
