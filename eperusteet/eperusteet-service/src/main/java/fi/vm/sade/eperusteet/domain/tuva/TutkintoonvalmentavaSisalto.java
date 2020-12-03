package fi.vm.sade.eperusteet.domain.tuva;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@Table(name = "tutkintoonvalmentava_perusteen_sisalto")
public class TutkintoonvalmentavaSisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    public TutkintoonvalmentavaSisalto kloonaa(Peruste peruste) {
        TutkintoonvalmentavaSisalto tuvaSisalto = new TutkintoonvalmentavaSisalto();
        tuvaSisalto.setPeruste(peruste);
        tuvaSisalto.setSisalto(sisalto.copy());
        return tuvaSisalto;
    }

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && sisalto.getId().equals(viite.getRoot().getId());
    }

}
