package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "ammattitaitovaatimus2019")
@Audited
public class Ammattitaitovaatimus2019 extends AbstractAuditedReferenceableEntity {

    @ValidKoodisto(koodisto = KoodistoUriArvo.AMMATTITAITOVAATIMUKSET)
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

    public boolean structureEquals(Ammattitaitovaatimus2019 other) {
        if (this == other) {
            return true;
        }
        boolean result = Objects.equals(getKoodi(), other.getKoodi());

        result &= refXnor(getVaatimus(), other.getVaatimus());

        return result;
    }
}
