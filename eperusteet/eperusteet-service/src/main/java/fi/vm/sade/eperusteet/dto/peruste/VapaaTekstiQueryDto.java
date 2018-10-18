package fi.vm.sade.eperusteet.dto.peruste;


import fi.vm.sade.eperusteet.domain.ProjektiTila;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, of = { "teksti" })
public class VapaaTekstiQueryDto extends PageableQueryDto implements Serializable {
    private ProjektiTila tila;
    private Long perusteprojekti;
    private Long peruste;
    private String teksti = "";
}
