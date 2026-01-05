package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "kaantaja_taitotaso_tutkintotaso_osa")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class KaantajaTaitotasoTutkintotasoOsa extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi suorituksenOsa;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaTaitotasoTutkintotasoOsa", orphanRemoval = true)
    private List<KaantajaTaitotasoTutkintotasoOsaTaitotaso> taitotasot = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private KaantajaTaitotasoTutkintotaso kaantajaTaitotasoTutkintotaso;

    public KaantajaTaitotasoTutkintotasoOsa(KaantajaTaitotasoTutkintotasoOsa other) {
        if (other != null) {
            this.suorituksenOsa = other.getSuorituksenOsa();
            this.taitotasot = other.getTaitotasot().stream()
                    .map(KaantajaTaitotasoTutkintotasoOsaTaitotaso::new)
                    .peek(taitotaso -> taitotaso.setKaantajaTaitotasoTutkintotasoOsa(this))
                    .collect(Collectors.toList());
        }
    }
}
