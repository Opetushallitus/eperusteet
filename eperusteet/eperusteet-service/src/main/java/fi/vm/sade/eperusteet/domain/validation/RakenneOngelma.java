package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "validointi_rakenneongelma")
public class RakenneOngelma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    public String ongelma;

    @Getter
    @Setter
    @OneToOne
    public TekstiPalanen ryhma;
}
