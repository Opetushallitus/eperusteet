package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "ammattitaitovaatimus2019")
@Audited
public class Ammattitaitovaatimus2019 extends AbstractAuditedReferenceableEntity {

    @ValidKoodisto(koodisto = "ammattitaitovaatimukset")
    @Setter
    @Getter
    @Audited(targetAuditMode = NOT_AUDITED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Koodi koodi;

    @ValidHtml
    @Setter
    @Getter
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen vaatimus;
}
