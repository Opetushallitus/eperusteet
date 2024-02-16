package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@Table(name = "yl_oppaine_yl_lukiokurssi", schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"kurssi_id", "oppiaine_id"}),
                @UniqueConstraint(columnNames = {"oppiaine_id", "jarjestys"})
        })
public class OppiaineLukiokurssi extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kurssi_id", nullable = false)
    private Lukiokurssi kurssi;

    @Getter
    @Setter
    @RelatesToPeruste
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oppiaine_id", nullable = false)
    private Oppiaine oppiaine;

    @Getter
    @Setter
    @Column(nullable = false)
    private Integer jarjestys;
}
