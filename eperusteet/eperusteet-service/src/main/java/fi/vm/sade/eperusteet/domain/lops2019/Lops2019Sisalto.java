package fi.vm.sade.eperusteet.domain.lops2019;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
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
import java.util.Objects;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_sisalto")
public class Lops2019Sisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto {

    @NotNull
    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "peruste_id", nullable = false, updatable = false, unique = true)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = false)
    @JoinColumn(name="laaja_alainen_osaaminen_kokonaisuus_id")
    private Lops2019LaajaAlainenOsaaminenKokonaisuus laajaAlainenOsaaminen;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_sisalto_oppiaine",
            joinColumns = @JoinColumn(name = "sisalto_id"),
            inverseJoinColumns = @JoinColumn(name = "oppiaine_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Oppiaine> oppiaineet = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name="sisalto_id")
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && viite.getRoot() != null && Objects.equals(sisalto.getId(), viite.getRoot().getId());
    }
}
