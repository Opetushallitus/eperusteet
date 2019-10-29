package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;


import fi.vm.sade.eperusteet.domain.Copyable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_laaja_alainen_osaaminen_kokonaisuus")
public class Lops2019LaajaAlainenOsaaminenKokonaisuus implements Copyable<Lops2019LaajaAlainenOsaaminenKokonaisuus> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OrderBy("jarjestys, id")
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_lao_kokonaisuus_lao",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_kokonaisuus_id"),
            inverseJoinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"))
    private Set<Lops2019LaajaAlainenOsaaminen> laajaAlaisetOsaamiset = new HashSet<>();

    public void setLaajaAlaisetOsaamiset(Collection<Lops2019LaajaAlainenOsaaminen> laajaAlaiset) {
        this.laajaAlaisetOsaamiset.clear();
        if (laajaAlaiset != null) {
            this.laajaAlaisetOsaamiset.addAll(laajaAlaiset);
        }
    }

    public boolean structureEquals(Lops2019LaajaAlainenOsaaminenKokonaisuus other) {
        boolean result = Objects.equals(this.getId(), other.getId());
        result &= refXnor(this.getLaajaAlaisetOsaamiset(), other.getLaajaAlaisetOsaamiset());

        if (this.getLaajaAlaisetOsaamiset() != null && other.getLaajaAlaisetOsaamiset() != null) {
            result &= this.getLaajaAlaisetOsaamiset().size() == other.getLaajaAlaisetOsaamiset().size();
            for (Lops2019LaajaAlainenOsaaminen lao : this.getLaajaAlaisetOsaamiset()) {
                if (!result) {
                    break;
                }
                for (Lops2019LaajaAlainenOsaaminen olao : other.getLaajaAlaisetOsaamiset()) {
                    if (Objects.equals(lao.getId(), olao.getId())) {
                        result &= lao.structureEquals(olao);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Lops2019LaajaAlainenOsaaminenKokonaisuus copy(boolean deep) {
        Lops2019LaajaAlainenOsaaminenKokonaisuus result = new Lops2019LaajaAlainenOsaaminenKokonaisuus();
        result.setLaajaAlaisetOsaamiset(this.laajaAlaisetOsaamiset.stream()
            .map(Lops2019LaajaAlainenOsaaminen::copy)
            .collect(Collectors.toList()));
        return result;
    }

}
