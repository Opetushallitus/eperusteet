package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.Reference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "suoritustapa")
@Audited
public class Suoritustapa implements Serializable, ReferenceableEntity, PerusteenSisalto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private Suoritustapakoodi suoritustapakoodi;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "sisalto_perusteenosaviite_id")
    private PerusteenOsaViite sisalto;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @JoinColumn(name = "tutkinnon_rakenne_id")
    private RakenneModuuli rakenne;

    @Getter
    @Setter
    @Column(name = "yksikko")
    private LaajuusYksikko laajuusYksikko;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "suoritustapa")
    @Getter
    @OrderBy("jarjestys, id")
    @BatchSize(size = 10)
    private Set<TutkinnonOsaViite> tutkinnonOsat = new HashSet<>();

    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "suoritustavat")
    @Getter
    private Set<Peruste> perusteet = new HashSet<>();

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    public void setTutkinnonOsat(Set<TutkinnonOsaViite> osat) {

        for (TutkinnonOsaViite v : osat) {
            v.setSuoritustapa(this);
        }

        tutkinnonOsat.retainAll(osat);
        tutkinnonOsat.addAll(osat);
    }

    public Suoritustapa() {
    }

    public Suoritustapa(Suoritustapakoodi suoritustapakoodi, PerusteenOsaViite sisalto, RakenneModuuli rakenne, LaajuusYksikko laajuusYksikko) {
        this.suoritustapakoodi = suoritustapakoodi;
        this.sisalto = sisalto;
        this.rakenne = rakenne;
        this.laajuusYksikko = laajuusYksikko;
    }

    boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && sisalto == viite.getRoot();
    }

}
