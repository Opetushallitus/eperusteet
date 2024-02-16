package fi.vm.sade.eperusteet.dto.perusteprojekti;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class PerusteprojektiInfoDto implements Serializable {
    private Long id;
    private String nimi;
    private ProjektiTila tila;
    private PerusteDto peruste;
    private String diaarinumero;
    private String ryhmaOid;
    private String koulutustyyppi;
}
