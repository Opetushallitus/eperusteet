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

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable(
            name = "yl_lops2019_laaja_alainen_osaaminen_kokonaisuus_laaja_alainen_osaaminen",
            joinColumns = @JoinColumn(
                    name = "laaja_alainen_osaaminen_kokonaisuus_id",
                    insertable = false,
                    updatable = false
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "laaja_alainen_osaaminen_id",
                    insertable = false,
                    updatable = false
            ))
    @OrderBy("jarjestys, id")
    private List<Lops2019LaajaAlainenOsaaminen> laajaAlaisetOsaamiset = new ArrayList<>();
}
