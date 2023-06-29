/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.Lops2019Oppiaine;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukioOpetussuunnitelmaRakenne;
import fi.vm.sade.eperusteet.domain.yl.lukio.OppiaineLukiokurssi;
import static fi.vm.sade.eperusteet.service.util.Util.identityEquals;
import static fi.vm.sade.eperusteet.service.util.Util.refXnor;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.yl.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_oppiaine")
public class Oppiaine extends AbstractAuditedReferenceableEntity implements NimettyKoodillinen, HistoriaTapahtuma {



    public enum OsaTyyppi {
        tehtava(Oppiaine::getTehtava),
        tavoitteet(Oppiaine::getTavoitteet),
        arviointi(Oppiaine::getArviointi);

        private Function<Oppiaine, TekstiOsa> getter;
        private OsaTyyppi(Function<Oppiaine, TekstiOsa> getter) {
            this.getter = getter;
        }

        public Function<Oppiaine, TekstiOsa> getter() {
            return getter;
        }
    }

    public static Predicate<Oppiaine> inLukioPeruste(long perusteId) {
        return inLukioPerusteDirect(perusteId).or(oa -> oa.getOppiaine() != null
                && inLukioPeruste(perusteId).test(oa.getOppiaine()));
    }
    private static Predicate<Oppiaine> inLukioPerusteDirect(long perusteId) {
        return oa -> oa.getLukioRakenteet().stream().anyMatch(
                LukioOpetussuunnitelmaRakenne.inPeruste(perusteId));
    }

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @NotNull(groups = Strict.class)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Valid
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "tavoitteet_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tavoitteet;

    @Getter
    @Setter
    @Valid
    @JoinColumn(name = "arviointi_id", nullable = true)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa arviointi;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "pakollinen_kurssi_kuvaus", nullable = true)
    private TekstiPalanen pakollinenKurssiKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "syventava_kurssi_kuvaus", nullable = true)
    private TekstiPalanen syventavaKurssiKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "soveltava_kurssi_kuvaus", nullable = true)
    private TekstiPalanen soveltavaKurssiKuvaus;

    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @NotNull(groups = Strict.class)
    @Size(min = 1, groups = Strict.class)
    @Valid
    @BatchSize(size = 3)
    private Set<OppiaineenVuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet;

    @RelatesToPeruste
    @Getter
    @ManyToOne
    private Oppiaine oppiaine;

    @Getter
    @Setter
    private Long jnro;

    @Getter
    @Setter
    @Column(name = "koodi_uri")
    private String koodiUri;

    @Getter
    @Setter
    @Column(name = "koodi_arvo")
    private String koodiArvo;

    /**
     * kertoo koostuuko oppiaine oppimääristä (esim. äidinkieli ja kirjallisuus) vai onko se "yksinkertainen" kuten matematiikka.
     */
    @Getter
    @Setter
    private boolean koosteinen = false;

    @Getter
    @Setter
    private Boolean abstrakti;

    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 10)
    private Set<Oppiaine> oppimaarat;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    private Set<OpetuksenKohdealue> kohdealueet = new HashSet<>();

    @Getter
    @Audited
    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<OppiaineLukiokurssi> lukiokurssit = new HashSet<>(0);

    @RelatesToPeruste
    @Getter
    @Audited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lukio_opetussuunnitelma_rakenne_yl_oppiaine",
            inverseJoinColumns = @JoinColumn(name = "rakenne_id", nullable = false, updatable = false),
            joinColumns = @JoinColumn(name = "oppiaine_id", nullable = false, updatable = false))
    private Set<LukioOpetussuunnitelmaRakenne> lukioRakenteet = new HashSet<>(0);

    @RelatesToPeruste
    @NotAudited
    @Getter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_perusop_perusteen_sisalto_yl_oppiaine",
            joinColumns = @JoinColumn(name = "oppiaineet_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_perusop_perusteen_sisalto_id", nullable = false, updatable = false))
    private Set<PerusopetuksenPerusteenSisalto> perusopetuksenPerusteenSisaltos;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_oppiaine_vapaateksti",
            joinColumns = @JoinColumn(name = "oppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    /**
     * Palauttaa oppimäärät
     *
     * @see #koosteinen
     * @return oppimäärät (joukkoa ei voi muokata) tai null jos oppiaine ei ole koosteinen
     */
    public Set<Oppiaine> getOppimaarat() {
        if (koosteinen == false) {
            return null;
        }
        return oppimaarat == null ? new HashSet<>() : new HashSet<>(oppimaarat);
    }

    public Set<OppiaineenVuosiluokkaKokonaisuus> getVuosiluokkakokonaisuudet() {
        return vuosiluokkakokonaisuudet == null ? Collections.<OppiaineenVuosiluokkaKokonaisuus>emptySet() : new HashSet<>(vuosiluokkakokonaisuudet);
    }

    public void addVuosiluokkaKokonaisuus(OppiaineenVuosiluokkaKokonaisuus ovk) {
        if (vuosiluokkakokonaisuudet == null) {
            vuosiluokkakokonaisuudet = new HashSet<>();
        }
        ovk.setOppiaine(this);
        if (vuosiluokkakokonaisuudet.add(ovk)) {
            this.muokattu();
        }
    }

    public void removeVuosiluokkaKokonaisuus(OppiaineenVuosiluokkaKokonaisuus ovk) {
        if (!ovk.getOppiaine().equals(this)) {
            throw new IllegalArgumentException("Vuosiluokkakokonaisuus ei kuulu tähän oppiaineeseen");
        }
        vuosiluokkakokonaisuudet.remove(ovk);
        ovk.setOppiaine(null);
    }

    public void addOppimaara(Oppiaine oppimaara) {
        if (!koosteinen) {
            throw new BusinessRuleViolationException("Oppiaine ei ole koosteinen eikä tue oppimääriä");
        }
        if (oppimaarat == null) {
            oppimaarat = new HashSet<>();
        }
        oppimaara.setOppiaine(this);
        if (oppimaarat.add(oppimaara)) {
            this.muokattu();
        }
    }

    public void removeOppimaara(Oppiaine aine) {
        if (!koosteinen) {
            throw new IllegalStateException("Oppiaine ei ole koosteinen eikä tue oppimääriä");
        }
        if (aine.getOppiaine().equals(this) && oppimaarat.remove(aine)) {
            aine.oppiaine = null;
        } else {
            throw new IllegalArgumentException("Oppimäärä ei kuulu tähän oppiaineeseen");
        }
    }

    //paivitetaan vain jarjestys
    public void setOppimaarat(final List<Oppiaine> oppimaarat) {
        if (this.oppimaarat == null) {
            this.oppimaarat = new HashSet<>();
        }

        this.oppimaarat.forEach(nykyinenOppimaara -> {
            Oppiaine uusiOppimaara = oppimaarat.stream().filter(oppimaara -> oppimaara.getId().equals(nykyinenOppimaara.getId())).findFirst().get();
            nykyinenOppimaara.setJnro((long)oppimaarat.indexOf(uusiOppimaara));
        });
    }

    public void setOppiaine(Oppiaine oppiaine) {
        if (this.oppiaine == null || this.oppiaine.equals(oppiaine)) {
            this.oppiaine = oppiaine;
        } else {
            throw new IllegalStateException("Oppiaineviittausta ei voi muuttaa");
        }
    }

    public void setOppiaineForce(Oppiaine oppiaine) {
        this.oppiaine = oppiaine;
    }

    public Set<OpetuksenKohdealue> getKohdealueet() {
        return new HashSet<>(kohdealueet);
    }

    public void setKohdealueet(Set<OpetuksenKohdealue> kohdealueet) {
        if (kohdealueet == null) {
            this.kohdealueet.clear();
        } else {
            Set<OpetuksenKohdealue> added = new HashSet<>(kohdealueet.size());
            //kohdealueita ei ole paljon (<10), joten O(n^2) OK tässä
            for (OpetuksenKohdealue k : kohdealueet) {
                added.add(addKohdealue(k));
            }
            //TODO: tarkista onko jokin poistettava kohdealue käytössä
            this.kohdealueet.retainAll(added);
        }
    }

    /**
     * Lisää uuden kohdealueen. Jos samanniminen kohdealue on jo olemassa, palauttaa tämän.
     */
    public OpetuksenKohdealue addKohdealue(OpetuksenKohdealue kohdealue) {
        if (kohdealue.getNimi() != null) {
            for (OpetuksenKohdealue k : kohdealueet) {
                if (kohdealue.getNimi().equals(k.getNimi())) {
                    return k;
                }
            }
        }
        this.kohdealueet.add(kohdealue);
        return kohdealue;
    }

    public void removeKohdealue(OpetuksenKohdealue kohdealue) {
        this.kohdealueet.remove(kohdealue);
    }

    //hiberate javaassist proxy "workaround"
    //ilman equals-metodia objectX.equals(proxy-objectX) on aina false
    @Override
    public boolean equals(Object other) {
        return this == other || (
                other instanceof Oppiaine
                && this.tunniste != null
                && this.tunniste.equals(((Oppiaine) other).tunniste)
        );
    }

    public boolean structureEquals(Oppiaine other) {
        boolean result = Objects.equals(this.getTunniste(), other.getTunniste());
        result &= refXnor(this.getOppiaine(), other.getOppiaine());
        result &= this.getOppiaine() == null || identityEquals(this.getOppiaine(), other.getOppiaine());
        result &= refXnor(this.getNimi(), other.getNimi());
        //salli tekstiosan poisto
        //result &= refXnor(this.getTehtava(), other.getTehtava());
        result &= refXnor(this.getKohdealueet(), other.getKohdealueet());
        result &= this.isKoosteinen() == other.isKoosteinen();
        if (this.isKoosteinen()) {
            result &= this.getOppimaarat().size() == other.getOppimaarat().size();
            for (Oppiaine m : this.getOppimaarat()) {
                if (!result) {
                    break;
                }
                for (Oppiaine om : other.getOppimaarat()) {
                    if (identityEquals(m, om)) {
                        result &= m.structureEquals(om);
                        break;
                    }
                }
            }
        }

        result &= this.getVuosiluokkakokonaisuudet().size() == other.getVuosiluokkakokonaisuudet().size();
        for (OppiaineenVuosiluokkaKokonaisuus vk : this.getVuosiluokkakokonaisuudet()) {
            if (!result) {
                break;
            }
            for (OppiaineenVuosiluokkaKokonaisuus ovk : other.getVuosiluokkakokonaisuudet()) {
                if (identityEquals(vk, ovk)) {
                    result &= vk.structureEquals(ovk);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public Oppiaine kloonaa() {
        return kloonaa(new HashMap<>(), new HashMap<>());
    }

    public Oppiaine kloonaa(
            Map<LaajaalainenOsaaminen, LaajaalainenOsaaminen> laajainenOsaaminenMapper,
            Map<VuosiluokkaKokonaisuus, VuosiluokkaKokonaisuus> vuosiluokkaKokonaisuusMapper) {
        Oppiaine oa = new Oppiaine();
        oa.setAbstrakti(abstrakti);
        oa.setJnro(jnro);
        oa.setKoosteinen(koosteinen);
        oa.setNimi(nimi);
        oa.setTehtava(tehtava);

        Map<OpetuksenKohdealue, OpetuksenKohdealue> kohdealueMapper = new HashMap<>();
        for (OpetuksenKohdealue kohdealue : kohdealueet) {
            OpetuksenKohdealue klooni = kohdealue.kloonaa();
            oa.addKohdealue(klooni);
            kohdealueMapper.put(kohdealue, klooni);
        }

        for (OppiaineenVuosiluokkaKokonaisuus ovlk : vuosiluokkakokonaisuudet) {
            OppiaineenVuosiluokkaKokonaisuus uovlk = ovlk.kloonaa(vuosiluokkaKokonaisuusMapper, laajainenOsaaminenMapper, kohdealueMapper);
            uovlk.setOppiaine(oa);
            oa.addVuosiluokkaKokonaisuus(uovlk);
        }

        for (Oppiaine om : oppimaarat) {
            oa.addOppimaara(om.kloonaa(laajainenOsaaminenMapper, vuosiluokkaKokonaisuusMapper));
        }
        return oa;
    }

    public void setVapaatTekstit(List<KevytTekstiKappale> vapaatTekstit) {
        this.vapaatTekstit = new ArrayList<>();
        if (vapaatTekstit != null) {
            for (KevytTekstiKappale vapaaTeksti : vapaatTekstit) {
                this.vapaatTekstit.add(new KevytTekstiKappale(vapaaTeksti));
            }
        }
    }

    public Stream<Oppiaine> maarineen() {
        return Stream.concat(Stream.of(this), oppimaarat.stream());
    }

    public interface Strict {
    }

    @Transient
    public boolean isAbstraktiBool() {
        return abstrakti != null && abstrakti;
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.oppiaine;
    }
}
