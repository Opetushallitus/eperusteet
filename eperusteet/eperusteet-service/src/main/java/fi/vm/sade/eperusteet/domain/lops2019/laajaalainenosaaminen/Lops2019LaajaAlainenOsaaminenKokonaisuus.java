package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_laaja_alainen_osaaminen_kokonaisuus")
public class Lops2019LaajaAlainenOsaaminenKokonaisuus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OrderBy("jarjestys, id")
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable(name = "yl_lops2019_laaja_alainen_osaaminen_kokonaisuus_laaja_alainen_osaaminen",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_kokonaisuus_id"),
            inverseJoinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"))
    private List<Lops2019LaajaAlainenOsaaminen> laajaAlaisetOsaamiset = new ArrayList<>();
}
