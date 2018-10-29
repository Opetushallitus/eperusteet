package fi.vm.sade.eperusteet.dto.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
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
    private ProjektiTila tila;
    private PerusteprojektiInfoDto perusteprojekti;
    private PerusteInfoDto peruste;
    private SuoritustapaDto suoritustapa;
    private PerusteenOsaViiteDto pov;
    private TutkinnonOsaViiteDto tov;
    private PerusteenOsaDto.Suppea tekstiKappale;
    private PerusteenOsaDto.Suppea tutkinnonOsa;
    private Kieli kieli;
    private String teksti;
    private String kuvaus;
}
