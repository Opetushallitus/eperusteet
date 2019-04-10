package fi.vm.sade.eperusteet.domain.lops2019;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "yl_lops2019_sisalto")
public class Lops2019Sisalto extends AbstractAuditedReferenceableEntity {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(name = "peruste_id", nullable = false, updatable = false, unique = true)
    private Peruste peruste;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="laaja_alainen_osaaminen_kokonaisuus_id")
    private Lops2019LaajaAlainenOsaaminenKokonaisuus laajaAlainenOsaaminen;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_sisalto_oppiaine",
            joinColumns = @JoinColumn(name = "sisalto_id"),
            inverseJoinColumns = @JoinColumn(name = "oppiaine_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Oppiaine> oppiaineet = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @JoinColumn(name="sisalto_id")
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && sisalto.getId().equals(viite.getRoot().getId());
    }
}
