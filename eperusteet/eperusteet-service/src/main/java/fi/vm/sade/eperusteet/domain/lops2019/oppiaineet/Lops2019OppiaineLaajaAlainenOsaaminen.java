package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_laaja_alainen_osaaminen")
public class Lops2019OppiaineLaajaAlainenOsaaminen {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_oppiaine_laaja_alainen_osaaminen_koodi",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"),
            inverseJoinColumns = @JoinColumn(name = "koodi_id"))
    private Set<Koodi> koodit;

    private Integer jarjestys;
}
