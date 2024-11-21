package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "osaamiskokonaisuus_kasitteisto")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OsaamiskokonaisuusKasitteisto extends AbstractAuditedReferenceableEntity {

    @Enumerated(EnumType.STRING)
    @NotNull
    private DigitaalinenOsaaminenTaso taso;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    public OsaamiskokonaisuusKasitteisto(OsaamiskokonaisuusKasitteisto other) {
        this.taso = other.taso;
        this.kuvaus = other.kuvaus;
    }

    public OsaamiskokonaisuusKasitteisto(DigitaalinenOsaaminenTaso taso){
        this.taso = taso;
    }
}
