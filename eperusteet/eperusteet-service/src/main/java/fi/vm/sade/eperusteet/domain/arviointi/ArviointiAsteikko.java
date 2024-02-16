package fi.vm.sade.eperusteet.domain.arviointi;

import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.Reference;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "ArviointiAsteikko")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "arviointiasteikko")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArviointiAsteikko implements Serializable, ReferenceableEntity {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @OrderColumn(name = "osaamistasot_order")
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(
            name = "arviointiasteikko_osaamistaso",
            joinColumns = @JoinColumn(name = "arviointiasteikko_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamistasot_id")
    )
    private List<Osaamistaso> osaamistasot;

    @Override
    public Reference getReference() {
        return new Reference(id);
    }
}
