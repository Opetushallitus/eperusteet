package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ammattitaitovaatimukset2019")
@Audited
public class Ammattitaitovaatimukset2019 extends AbstractAuditedReferenceableEntity {

    @ValidHtml
    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohde;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "ammattitaitovaatimukset2019_ammattitaitovaatimus2019",
            joinColumns = @JoinColumn(name = "ammattitaitovaatimukset_id"),
            inverseJoinColumns = @JoinColumn(name = "ammattitaitovaatimus_id"))
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    private Set<Ammattitaitovaatimus2019> vaatimukset = new HashSet<>();
}
