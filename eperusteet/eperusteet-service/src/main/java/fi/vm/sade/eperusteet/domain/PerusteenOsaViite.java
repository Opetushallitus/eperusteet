package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.kios.KieliJaKaantajaTutkintoPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.tuva.TutkintoonvalmentavaSisalto;
import fi.vm.sade.eperusteet.domain.vst.VapaasivistystyoSisalto;
import fi.vm.sade.eperusteet.domain.digi.DigitaalisenOsaamisenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.TpoOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Audited
@Table(name = "perusteenosaviite")
public class  PerusteenOsaViite implements
        ReferenceableEntity,
        Serializable,
        Copyable<PerusteenOsaViite>,
        HistoriaTapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @RelatesToPeruste
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vanhempi_id", insertable = false, updatable = false)
    @Getter
    @Setter
    private PerusteenOsaViite vanhempi;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private Suoritustapa suoritustapa;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private LukiokoulutuksenPerusteenSisalto lukiokoulutuksenPerusteenSisalto;


    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private Lops2019Sisalto lops2019Sisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private AIPEOpetuksenSisalto aipeSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private TpoOpetuksenSisalto tpoOpetuksenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private OpasSisalto opasSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private VapaasivistystyoSisalto vstSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private TutkintoonvalmentavaSisalto tuvaSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private DigitaalisenOsaamisenPerusteenSisalto digitaalinenOsaaminenSisalto;

    @RelatesToPeruste
    @NotAudited
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto")
    private KieliJaKaantajaTutkintoPerusteenSisalto kieliJaKaantajaTutkintoPerusteenSisalto;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    private PerusteenOsa perusteenOsa;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "vanhempi_id")
    @OrderColumn(name = "lapset_order")
    @NotAudited
    @Getter
    @Setter
    private List<PerusteenOsaViite> lapset = new ArrayList<>();

    public PerusteenOsaViite() {
    }

    public PerusteenOsaViite(final Suoritustapa suoritustapa) {
        this.suoritustapa= suoritustapa;
    }

    public PerusteenOsaViite(final PerusopetuksenPerusteenSisalto sisalto) {
        this.perusopetuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(final LukiokoulutuksenPerusteenSisalto sisalto) {
        this.lukiokoulutuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(final Lops2019Sisalto sisalto) {
        this.lops2019Sisalto = sisalto;
    }

    public PerusteenOsaViite(final EsiopetuksenPerusteenSisalto sisalto) {
        this.esiopetuksenPerusteenSisalto = sisalto;
    }

    public PerusteenOsaViite(final OpasSisalto sisalto) {
        this.opasSisalto = sisalto;
    }

    public PerusteenOsaViite(final AIPEOpetuksenSisalto sisalto) {
        this.aipeSisalto = sisalto;
    }

    public PerusteenOsaViite(final TpoOpetuksenSisalto sisalto) {
        this.tpoOpetuksenSisalto = sisalto;
    }

    public PerusteenOsaViite(final VapaasivistystyoSisalto sisalto) {
        this.vstSisalto = sisalto;
    }

    public PerusteenOsaViite(final TutkintoonvalmentavaSisalto sisalto) {
        this.tuvaSisalto = sisalto;
    }

    public PerusteenOsaViite(final DigitaalisenOsaamisenPerusteenSisalto sisalto) {
        this.digitaalinenOsaaminenSisalto = sisalto;
    }

    public PerusteenOsaViite(final KieliJaKaantajaTutkintoPerusteenSisalto sisalto) {
        this.kieliJaKaantajaTutkintoPerusteenSisalto = sisalto;
    }

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    public PerusteenOsaViite getRoot() {
        PerusteenOsaViite root = this;
        while (root.getVanhempi() != null) {
            root = root.getVanhempi();
        }
        return root;
    }

    @Override
    public PerusteenOsaViite copy(final boolean deep) {
        final PerusteenOsaViite pov = new PerusteenOsaViite();
        if (this.getPerusteenOsa() != null) {
            pov.setPerusteenOsa(this.getPerusteenOsa().copy());
        }

        final List<PerusteenOsaViite> uudetLapset = new ArrayList<>();
        for (final PerusteenOsaViite lapsi : lapset) {
            final PerusteenOsaViite kloonattu = lapsi.copy();
            kloonattu.setVanhempi(pov);
            uudetLapset.add(kloonattu);
        }
        pov.setLapset(uudetLapset);
        return pov;
    }

    @Override
    public Date getLuotu() {
        return this.perusteenOsa.getLuotu();
    }

    @Override
    public Date getMuokattu() {
        return this.perusteenOsa.getMuokattu();
    }

    @Override
    public String getLuoja() {
        return this.perusteenOsa.getLuoja();
    }

    @Override
    public String getMuokkaaja() {
        return this.perusteenOsa.getMuokkaaja();
    }

    @Override
    public TekstiPalanen getNimi() {
        return this.perusteenOsa.getNimi();
    }

    @Override
    public NavigationType getNavigationType() {
        if (this.perusteenOsa != null) {
            return this.perusteenOsa.getNavigationType();
        }
        return NavigationType.viite;
    }

    public Peruste getPeruste() {
        if (this.vanhempi != null) {
            return this.vanhempi.getPeruste();
        }

        if (suoritustapa != null) {
            return suoritustapa.getPerusteet().stream().findFirst().get();
        }

        if (perusopetuksenPerusteenSisalto != null) {
            return perusopetuksenPerusteenSisalto.getPeruste();
        }

        if (lukiokoulutuksenPerusteenSisalto != null) {
            return lukiokoulutuksenPerusteenSisalto.getPeruste();
        }

        if (lops2019Sisalto != null) {
            return lops2019Sisalto.getPeruste();
        }

        if (esiopetuksenPerusteenSisalto != null) {
            return esiopetuksenPerusteenSisalto.getPeruste();
        }

        if (aipeSisalto != null) {
            return aipeSisalto.getPeruste();
        }

        if (tpoOpetuksenSisalto != null) {
            return tpoOpetuksenSisalto.getPeruste();
        }

        if (opasSisalto != null) {
            return opasSisalto.getPeruste();
        }

        if (vstSisalto != null) {
            return vstSisalto.getPeruste();
        }
        if (tuvaSisalto != null) {
            return tuvaSisalto.getPeruste();
        }

        if (digitaalinenOsaaminenSisalto != null) {
            return digitaalinenOsaaminenSisalto.getPeruste();
        }

        if (kieliJaKaantajaTutkintoPerusteenSisalto != null) {
            return kieliJaKaantajaTutkintoPerusteenSisalto.getPeruste();
        }

        return null;
    }
}
