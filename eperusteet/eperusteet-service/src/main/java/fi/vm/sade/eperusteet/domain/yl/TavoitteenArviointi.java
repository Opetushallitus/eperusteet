package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "yl_tavoitteen_arviointi")
@Audited
@Getter
@Setter
public class TavoitteenArviointi extends AbstractReferenceableEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen arvioinninKohde;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen osaamisenKuvaus;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Integer arvosana;

    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_opetuksen_tavoite_yl_tavoitteen_arviointi",
            joinColumns = @JoinColumn(name = "arvioinninkohteet_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id", nullable = false))
    private Set<OpetuksenTavoite> opetuksenTavoitteet = new HashSet<>();

    public TavoitteenArviointi kloonaa() {
        TavoitteenArviointi klooni = new TavoitteenArviointi();
        klooni.setArvioinninKohde(arvioinninKohde);
        klooni.setOsaamisenKuvaus(osaamisenKuvaus);
        klooni.setArvosana(arvosana);
        return klooni;
    }

    @Deprecated
    public TekstiPalanen getHyvanOsaamisenKuvaus() {
        if (arvosana == null || arvosana == 8) {
            return osaamisenKuvaus;
        }

        return null;
    }

}
