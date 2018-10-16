package fi.vm.sade.eperusteet.dto.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" })
public class TekstiHakuTulosDto implements Serializable {
    private Long id;
    private EntityReference perusteprojekti;
    private EntityReference peruste;
    private EntityReference suoritustapa;
    private EntityReference pov;
    private EntityReference tov;
    private Kieli kieli;
    private String teksti;
}
