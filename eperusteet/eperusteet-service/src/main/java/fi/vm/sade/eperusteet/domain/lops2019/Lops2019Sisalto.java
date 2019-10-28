package fi.vm.sade.eperusteet.domain.lops2019;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuus;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.dto.peruste.Navigable;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_sisalto")
public class Lops2019Sisalto extends AbstractAuditedReferenceableEntity implements
        PerusteenSisalto,
        StructurallyComparable<Lops2019Sisalto>,
        Copyable<Lops2019Sisalto> {

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

    public boolean containsViite(final PerusteenOsaViite viite) {
        return viite != null && viite.getRoot() != null && Objects.equals(sisalto.getId(), viite.getRoot().getId());
    }

    @Override
    public boolean structureEquals(Lops2019Sisalto other) {
        boolean result = true;
        result &= refXnor(this.getLaajaAlainenOsaaminen(), other.getLaajaAlainenOsaaminen());

        if (this.getOppiaineet().size() == other.getOppiaineet().size()) {
            result &= IntStream.range(0, this.getOppiaineet().size())
                    .allMatch(idx -> this.getOppiaineet().get(idx)
                            .structureEquals(other.getOppiaineet().get(idx)));
        }
        else {
            return false;
        }

        return result;
    }

    @Override
    public Lops2019Sisalto copy(boolean deep) {
        Lops2019Sisalto result = new Lops2019Sisalto();
        result.setPeruste(null);
        result.setLaajaAlainenOsaaminen(this.getLaajaAlainenOsaaminen().copy());
        if (deep) {
            if (this.sisalto != null) {
                PerusteenOsaViite kopio = this.sisalto.copy();
                result.setSisalto(kopio);
            }
            result.setOppiaineet(this.oppiaineet.stream()
                    .map(oa -> oa.copy(true))
                    .collect(Collectors.toList()));
        }
        return result;
    }
}
