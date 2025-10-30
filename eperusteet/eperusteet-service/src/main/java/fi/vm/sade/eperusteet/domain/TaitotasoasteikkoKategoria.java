package fi.vm.sade.eperusteet.domain;

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
@Table(name = "taitotasoasteikko_kategoria")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class TaitotasoasteikkoKategoria extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taitotasoasteikkoKategoria", orphanRemoval = true)
    private List<TaitotasoasteikkoKategoriaTaitotaso> taitotasoasteikkoKategoriaTaitotasot = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private KaantajaTaitotasoasteikko kaantajaTaitotasoasteikko;

    public TaitotasoasteikkoKategoria(TaitotasoasteikkoKategoria other) {
        if (other != null) {
            this.otsikko = other.getOtsikko();
            this.taitotasoasteikkoKategoriaTaitotasot = other.getTaitotasoasteikkoKategoriaTaitotasot().stream()
                    .map(TaitotasoasteikkoKategoriaTaitotaso::new)
                    .peek(taitotaso -> taitotaso.setTaitotasoasteikkoKategoria(this))
                    .collect(Collectors.toList());
        }
    }
}

