package fi.vm.sade.eperusteet.domain.validation;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "validointi_status_info")
public class ValidointiStatusInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Setter
    @Getter
    private String viesti;

    @Setter
    @Getter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Validointi validointi;

    @Getter
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<TekstiPalanen> nimet = new ArrayList<>();

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Suoritustapakoodi suoritustapa;

    public void setNimet(List<TekstiPalanen> tekstipalaset) {
        if (tekstipalaset != null) {
            this.nimet.clear();
            this.nimet.addAll(tekstipalaset);
        }
    }
}
