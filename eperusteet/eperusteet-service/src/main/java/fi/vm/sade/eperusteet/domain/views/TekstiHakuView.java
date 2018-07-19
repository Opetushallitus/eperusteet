package fi.vm.sade.eperusteet.domain.views;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import lombok.Getter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Table(name = "tekstihaku_view")
@IdClass(TekstiHakuId.class)
public class TekstiHakuView implements Serializable {

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    private Perusteprojekti perusteprojekti;

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    private Peruste peruste;

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    private TekstiPalanen tekstipalanen;

    @Id
    @Getter
    @Enumerated(EnumType.STRING)
    private Kieli kieli;

    @Getter
    private String teksti;

}
