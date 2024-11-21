package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.AIPEVaihe;
import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "yl_aipe_opetuksensisalto")
public class AIPEOpetuksenSisalto extends AbstractOppiaineOpetuksenSisalto implements PerusteenSisalto {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(name = "peruste_id", nullable = false, updatable = false, unique = true)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name = "sisalto_id")
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Getter
    @JoinTable(name = "yl_aipe_opetuksensisalto_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "yl_aipe_opetuksensisalto_id"),
            inverseJoinColumns = @JoinColumn(name = "laajaalaisetosaamiset_id"))
    @OrderBy("jarjestys, id")
    private List<LaajaalainenOsaaminen> laajaalaisetosaamiset = new ArrayList<>();

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipe_opetuksensisalto_vaihe",
               joinColumns = @JoinColumn(name = "opetus_id"),
               inverseJoinColumns = @JoinColumn(name = "vaihe_id"))
    @OrderBy("jarjestys, id")
    private List<AIPEVaihe> vaiheet = new ArrayList<>();

    public Optional<AIPEVaihe> getVaihe(Long vaiheId) {
        return vaiheet.stream()
                .filter(vaihe -> Objects.equals(vaihe.getId(), vaiheId))
                .findFirst();
    }

    public Optional<LaajaalainenOsaaminen> getLaajaalainenOsaaminen(Long laajaalainenosaaminenId) {
        return laajaalaisetosaamiset.stream()
                .filter(vaihe -> Objects.equals(vaihe.getId(), laajaalainenosaaminenId))
                .findFirst();
    }

    public AIPEOpetuksenSisalto kloonaa(Peruste peruste) {
        AIPEOpetuksenSisalto kopio = new AIPEOpetuksenSisalto();
        kopio.peruste = peruste;
        kopio.sisalto = this.sisalto.copy();
        return kopio;
    }

    @Override
    public Set<Oppiaine> getOppiaineet() {
        return new HashSet<>();
    }
}

