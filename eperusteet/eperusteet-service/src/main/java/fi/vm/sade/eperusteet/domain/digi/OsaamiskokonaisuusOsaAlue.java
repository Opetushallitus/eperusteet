package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "osaamiskokonaisuus_osa_alue")
@Audited
@Getter
@Setter
public class OsaamiskokonaisuusOsaAlue extends AbstractAuditedReferenceableEntity {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "osaamiskokonaisuus_osa_alue_tasokuvaukset_join")
    @OrderColumn
    private List<OsaamiskokonaisuusOsaAlueTasoKuvaus> tasokuvaukset = new ArrayList<>();;

    public void setTasokuvaukset(List<OsaamiskokonaisuusOsaAlueTasoKuvaus> tasokuvaukset) {
        this.tasokuvaukset.clear();
        this.tasokuvaukset.addAll(tasokuvaukset);
    }

}
