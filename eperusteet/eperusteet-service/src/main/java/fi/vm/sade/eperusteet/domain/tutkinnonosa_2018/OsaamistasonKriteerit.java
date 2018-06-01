package fi.vm.sade.eperusteet.domain.tutkinnonosa_2018;

import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Audited
@Table(name = "osaamistason_kriteerit")
public class OsaamistasonKriteerit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private Osaamistaso osaamistaso;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderColumn
    @JoinTable(name = "osaamistasonkriteerit_tekstipalanen",
            joinColumns = @JoinColumn(name = "osaamistasonkriteerit_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @BatchSize(size = 25)
    @Getter
    private List<TekstiPalanen> kriteerit = new ArrayList<>();

    @Getter
    @Column(updatable = false)
    private UUID tunniste;

    public OsaamistasonKriteerit() {
        this.tunniste = this.tunniste != null ? this.tunniste : UUID.randomUUID();
    }

    public void setKriteerit(List<TekstiPalanen> kriteerit) {
        this.kriteerit.clear();
        this.kriteerit.addAll(kriteerit);
    }

}
