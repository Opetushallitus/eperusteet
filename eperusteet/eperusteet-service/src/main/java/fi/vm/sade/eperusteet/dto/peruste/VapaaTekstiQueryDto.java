package fi.vm.sade.eperusteet.dto.peruste;


import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.tekstihaku.TekstihakuTyyppi;
import lombok.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VapaaTekstiQueryDto extends PageableQueryDto implements Serializable {
    private ProjektiTila tila;
    private Long perusteprojekti;
    private Long peruste;
    private String teksti = "";
    private Set<TekstihakuTyyppi> kohteet = new HashSet(Arrays.asList(TekstihakuTyyppi.values()));
}
