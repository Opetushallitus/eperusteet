package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_painopiste")
public class Lops2019Painopiste extends AbstractAuditedReferenceableEntity {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    private Integer jarjestys;

    public boolean structureEquals(Lops2019Painopiste other) {
        return Objects.equals(this.getId(), other.getId());
    }
}
