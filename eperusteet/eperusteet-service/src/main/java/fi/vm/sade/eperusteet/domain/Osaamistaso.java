package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import java.io.Serializable;
import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "Osaamistaso")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "osaamistaso")
@Builder
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@AllArgsConstructor
public class Osaamistaso implements Serializable, ReferenceableEntity {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private TekstiPalanen otsikko;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @ValidKoodisto(koodisto = KoodistoUriArvo.ARVIOINTIASTEIKKOAMMATILLINEN)
    private Koodi koodi;

    @Override
    public String toString() {
        return "" + id;
    }

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

}
