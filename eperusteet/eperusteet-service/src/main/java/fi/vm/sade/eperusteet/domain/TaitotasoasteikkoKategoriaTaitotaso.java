package fi.vm.sade.eperusteet.domain;

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
@Table(name = "taitotasoasteikko_kategoria_taitotaso")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class TaitotasoasteikkoKategoriaTaitotaso extends AbstractAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ManyToOne(fetch = FetchType.LAZY)
    private TaitotasoasteikkoKategoria taitotasoasteikkoKategoria;

    public TaitotasoasteikkoKategoriaTaitotaso(TaitotasoasteikkoKategoriaTaitotaso other) {
        if (other != null) {
            this.otsikko = other.getOtsikko();
            this.kuvaus = other.getKuvaus();
        }
    }
}

