package fi.vm.sade.eperusteet.domain.views;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Table(name = "tekstihaku_tulos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "id" })
public class TekstiHakuTulos implements Serializable {

    @Id
    @Getter
    @Setter
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Perusteprojekti perusteprojekti;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Suoritustapa suoritustapa;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private PerusteenOsaViite pov;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private TutkinnonOsaViite tov;

    @Getter
    @Enumerated(EnumType.STRING)
    @Setter
    private Kieli kieli;

    @Getter
    @Setter
    private String teksti;

}
