package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_laaja_alainen_osaaminen_kokonaisuus")
public class Lops2019OppiaineLaajaAlainenOsaaminenKokonaisuus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_oppiaine_laaja_alainen_osaaminen_kokonaisuus_laaja_alainen_osaaminen",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_kokonaisuus_id"),
            inverseJoinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"))
    private List<Lops2019OppiaineLaajaAlainenOsaaminen> laajaAlaisetOsaamiset = new ArrayList<>();
}
