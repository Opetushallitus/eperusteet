package fi.vm.sade.eperusteet.domain.tutkinnonosa_2018;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "tutkinnonosa_2018.Ammattitaitovaatimus")
@Audited
@Table(name = "ammattitaitovaatimus", schema = "tutkinnonosa_2018")
public class Ammattitaitovaatimus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen nimi;

    @ManyToOne(fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull
    @Getter
    @Setter
    private ArviointiAsteikko arviointiAsteikko;

//    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
//    @Getter
//    @BatchSize(size = 10)
//    private Set<OsaamistasonKriteerit> osaamistasonKriteerit = new HashSet<>();

    @Getter
    @Column(updatable = false)
    private UUID tunniste;

    public Ammattitaitovaatimus() {
        this.tunniste = this.tunniste != null ? this.tunniste : UUID.randomUUID();
    }

}
