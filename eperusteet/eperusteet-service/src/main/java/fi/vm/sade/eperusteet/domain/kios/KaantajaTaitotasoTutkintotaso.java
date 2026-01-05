package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
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
@Table(name = "kaantaja_taitotaso_tutkintotaso")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class KaantajaTaitotasoTutkintotaso extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaTaitotasoTutkintotaso", orphanRemoval = true)
    private List<KaantajaTaitotasoTutkintotasoOsa> osat = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private KaantajaTaitotasokuvaus kaantajaTaitotasokuvaus;

    public KaantajaTaitotasoTutkintotaso(KaantajaTaitotasoTutkintotaso other) {
        if (other != null) {
            this.nimi = other.getNimi();
            this.osat = other.getOsat().stream()
                    .map(KaantajaTaitotasoTutkintotasoOsa::new)
                    .peek(osa -> osa.setKaantajaTaitotasoTutkintotaso(this))
                    .collect(Collectors.toList());
        }
    }
}

