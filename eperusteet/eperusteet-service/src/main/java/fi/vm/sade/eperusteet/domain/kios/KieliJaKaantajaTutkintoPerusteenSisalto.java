package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "kielijakaantajatutkinto_perusteen_sisalto")
public class KieliJaKaantajaTutkintoPerusteenSisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    public KieliJaKaantajaTutkintoPerusteenSisalto kloonaa(Peruste peruste) {
        KieliJaKaantajaTutkintoPerusteenSisalto kieliKaantajasisalto = new KieliJaKaantajaTutkintoPerusteenSisalto();
        kieliKaantajasisalto.setPeruste(peruste);
        kieliKaantajasisalto.setSisalto(sisalto.copy());
        return kieliKaantajasisalto;
    }

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && sisalto.getId().equals(viite.getRoot().getId());
    }
}

