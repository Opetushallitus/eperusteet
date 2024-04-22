package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.NimettyKoodillinen;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@Entity
@Audited
@Table(name = "yl_lukio_opetussuunnitelma_rakenne", schema = "public")
public class LukioOpetussuunnitelmaRakenne extends PerusteenOsa {
    public static Predicate<LukioOpetussuunnitelmaRakenne> inPeruste(long perusteId) {
        return rakenne -> rakenne.getSisalto().getPeruste().getId().equals(perusteId);
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name="viite_id", nullable = false)
    private PerusteenOsaViite viite = new PerusteenOsaViite();

    @RelatesToPeruste
    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "sisalto_id", nullable = false)
    private LukiokoulutuksenPerusteenSisalto sisalto;

    @Getter
    @Audited
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine",
            joinColumns = @JoinColumn(name = "rakenne_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "oppiaine_id", nullable = false, updatable = false))
    private Set<Oppiaine> oppiaineet = new HashSet<>(0);

    @Getter
    @Audited
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "opetussuunnitelma")
    private Set<Lukiokurssi> kurssit = new HashSet<>(0);

    public LukioOpetussuunnitelmaRakenne kloonaa(LukiokoulutuksenPerusteenSisalto sisalto) {
        LukioOpetussuunnitelmaRakenne kopio = new LukioOpetussuunnitelmaRakenne();
        kopio.sisalto = sisalto;
        kopio.oppiaineet.addAll(this.oppiaineet.stream().map(Oppiaine::kloonaa).collect(toList()));
        kopio.kurssit.addAll(this.kurssit.stream().map(k -> k.kloonaa(kopio)).collect(toList()));
        return kopio;
    }

    @Override
    public PerusteenOsa copy() {
        return kloonaa(null);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    public Stream<Lukiokurssi> kurssit() {
        return getKurssit().stream();
    }
    public Stream<Oppiaine> oppiaineet() {
        return getOppiaineet().stream();
    }
    public Stream<Oppiaine> oppiaineetMaarineen() {
        return oppiaineet().flatMap(Oppiaine::maarineen);
    }
    public Stream<NimettyKoodillinen> koodilliset() {
        return concat(oppiaineetMaarineen(), kurssit());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.lukiorakenne;
    }
}
