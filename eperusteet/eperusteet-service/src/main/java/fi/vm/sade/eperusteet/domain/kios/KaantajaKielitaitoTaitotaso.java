package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "kaantaja_kielitaito_taitotaso")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class KaantajaKielitaitoTaitotaso extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi taitotaso;

    @ManyToOne(fetch = FetchType.LAZY)
    private KaantajaKielitaito kaantajaKielitaito;

    public KaantajaKielitaitoTaitotaso(KaantajaKielitaitoTaitotaso other) {
        if (other != null) {
            this.kuvaus = other.getKuvaus();
            this.taitotaso = other.getTaitotaso();
        }
    }
}
