package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.domain.ProjektiTila;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaarinumeroHakuDto implements Serializable {
    private Boolean loytyi;
    private ProjektiTila tila;
    private Long id;
    private String diaarinumero;
}
