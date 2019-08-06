package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_tavoite")
public class Lops2019Tavoite extends AbstractAuditedReferenceableEntity {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohde;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_tavoite_tavoite_tavoite",
            joinColumns = @JoinColumn(name = "tavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoite_tavoite_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019TavoiteTavoite> tavoitteet = new ArrayList<>();

    private Integer jarjestys;

    public boolean structureEquals(Lops2019Tavoite other) {
        boolean result = Objects.equals(this.getId(), other.getId());
        result &= refXnor(this.getId(), other.getId());
        result &= refXnor(this.getTavoitteet(), other.getTavoitteet());

        if (this.getTavoitteet() != null && other.getTavoitteet() != null) {
            result &= this.getTavoitteet().size() == other.getTavoitteet().size();
            for (Lops2019TavoiteTavoite t : this.getTavoitteet()) {
                if (!result) {
                    break;
                }
                for (Lops2019TavoiteTavoite ot : other.getTavoitteet()) {
                    if (Objects.equals(t.getId(), ot.getId())) {
                        result &= t.structureEquals(ot);
                    }
                }
            }
        }

        return result;
    }
}
