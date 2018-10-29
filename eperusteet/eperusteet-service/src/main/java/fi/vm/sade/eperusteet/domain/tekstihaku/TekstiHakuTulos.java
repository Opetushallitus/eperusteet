package fi.vm.sade.eperusteet.domain.tekstihaku;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tekstihaku")
@Immutable
public class TekstiHakuTulos implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Column
    @Getter
    @Setter
    private String koulutustyyppi;

    @Column
    @Getter
    @Setter
    private Boolean esikatseltavissa;

    @Column
    @Getter
    @Setter
    private ProjektiTila tila;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private Perusteprojekti perusteprojekti;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private Peruste peruste;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private Suoritustapa suoritustapa;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private PerusteenOsaViite pov;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private TutkinnonOsaViite tov;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private TekstiKappale tekstiKappale;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    private TutkinnonOsa tutkinnonOsa;

    @Column
    @Getter
    @Setter
    private String kuvaus;

    @Enumerated(EnumType.STRING)
    @Column
    @Getter
    @Setter
    private Kieli kieli;

    @Column
    @Getter
    @Setter
    private String teksti;

    public TekstiHakuTulos() {
    }

    public TekstiHakuTulos(TekstiHaku haku) {
        TekstihakuCtx ctx = haku.getCtx();

        setKoulutustyyppi(ctx.getKoulutustyyppi());
        setEsikatseltavissa(ctx.getEsikatseltavissa());
        setTila(ctx.getTila());

        setPerusteprojekti(ctx.getPerusteprojekti());
        setPeruste(ctx.getPeruste());
        setSuoritustapa(ctx.getSuoritustapa());
        setPov(ctx.getPov());
        setTov(ctx.getTov());
        setTekstiKappale(ctx.getTekstiKappale());
        setTutkinnonOsa(ctx.getTutkinnonOsa());

        setKuvaus(haku.getKuvaus());
        setKieli(haku.getKieli());
        setTeksti(haku.getTeksti());
    }
}
