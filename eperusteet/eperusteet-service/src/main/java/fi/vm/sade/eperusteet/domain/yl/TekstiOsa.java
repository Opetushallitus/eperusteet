package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.Identifiable;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import jakarta.persistence.*;

import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Audited
@Table(name = "yl_tekstiosa")
@Entity
@RelatesToPeruste.FromAnywhereReferenced
public class TekstiOsa implements Serializable, Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    private Long id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen otsikko;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml
    private TekstiPalanen teksti;

    public TekstiOsa() {
    }

    public TekstiOsa(TekstiPalanen otsikko, TekstiPalanen teksti) {
        this.otsikko = otsikko;
        this.teksti = teksti;
    }

    public TekstiOsa(TekstiOsa other) {
        this.otsikko = other.getOtsikko();
        this.teksti = other.getTeksti();
    }

    public static void validateChange(TekstiOsa a, TekstiOsa b) {
        if (a == null) {
            return;
        }

        if (b == null) {
            throw new BusinessRuleViolationException("tekstiosaa-ei-voi-poistaa");
        }

        if (a.getOtsikko() != null && b.getOtsikko() == null) {
            throw new BusinessRuleViolationException("otsikkoa-ei-voi-poistaa");
        }

        if (a.getTeksti() != null && b.getTeksti() == null) {
            throw new BusinessRuleViolationException("tekstia-ei-voi-poistaa");
        }

    }


}
