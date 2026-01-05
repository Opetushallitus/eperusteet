package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kaantaja_taito_kohdealue")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class KaantajaTaitoKohdealue extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohdealueOtsikko;

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "kaantaja_taito_kohdealue_tutkintovaatimus",
            joinColumns = @JoinColumn(name = "kaantaja_taito_kohdealue_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    private List<TekstiPalanen> tutkintovaatimukset = new ArrayList<>();

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "kaantaja_taito_kohdealue_arviointikriteeri",
            joinColumns = @JoinColumn(name = "kaantaja_taito_kohdealue_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    private List<TekstiPalanen> arviointikriteerit = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private KaantajaTaito kaantajaTaito;

    public KaantajaTaitoKohdealue(KaantajaTaitoKohdealue other) {
        if (other != null) {
            this.kohdealueOtsikko = other.getKohdealueOtsikko();
            this.tutkintovaatimukset = new ArrayList<>(other.getTutkintovaatimukset());
            this.arviointikriteerit = new ArrayList<>(other.getArviointikriteerit());
        }
    }
}

