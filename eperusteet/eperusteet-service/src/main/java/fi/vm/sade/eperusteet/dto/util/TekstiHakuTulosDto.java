package fi.vm.sade.eperusteet.dto.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TekstiHakuTulosDto implements Serializable {
    private EntityReference perusteprojekti;
    private EntityReference peruste;
    private EntityReference tekstipalanen;
    private Kieli kieli;
    private String teksti;
}
