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
package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.eperusteet.domain.annotation.Identifiable;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.lops2019.Lops2019Sisalto;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import fi.vm.sade.eperusteet.domain.yl.EsiopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.TpoOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.PerusteIdentifiable;
import fi.vm.sade.eperusteet.service.util.PerusteUtils;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus.LOPS2019;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "peruste")
@Audited
public class Peruste extends AbstractAuditedEntity
        implements Serializable, ReferenceableEntity, WithPerusteTila, PerusteIdentifiable, Identifiable, HistoriaTapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "peruste")
    private PerusteVersion globalVersion = new PerusteVersion(this);

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = { Valmis.class, ValmisPohja.class, ValmisPohja.class })
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Deprecated
    private TekstiPalanen kuvaus;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    private Maarayskirje maarayskirje;

    @Getter
    @Setter
    @NotNull
    private boolean koulutusvienti = false;

    @Getter
    @OrderColumn
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Muutosmaarays> muutosmaaraykset = new ArrayList<>();

    @Getter
    @Setter
    @NotNull(groups = Valmis.class)
    private Diaarinumero diaarinumero;

    @Getter
    @Setter
    @NotNull(groups = { Valmis.class, ValmisPohja.class })
    @Column(name = "koulutustyyppi")
    private String koulutustyyppi;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "peruste_koulutus",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "koulutus_id"))
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Koulutus> koulutukset;

    @ElementCollection
    @Getter
    @Setter
    @CollectionTable(name = "korvattavat_diaarinumerot")
    private Set<Diaarinumero> korvattavatDiaarinumerot;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(name = "peruste_osaamisala",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "osaamisala_id"))
    @Column(name = "osaamisala_id")
    private Set<Koodi> osaamisalat = new HashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "voimassaolo_alkaa")
    @NotNull(groups = { Valmis.class })
    private Date voimassaoloAlkaa;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "voimassaolo_loppuu")
    private Date voimassaoloLoppuu;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    @Column(name = "siirtyma_paattyy")
    private Date siirtymaPaattyy;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date paatospvm;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapKey(name = "suoritustapakoodi")
    @JoinTable(name = "peruste_suoritustapa",
               joinColumns = @JoinColumn(name = "peruste_id"),
               inverseJoinColumns = @JoinColumn(name = "suoritustapa_id"))
    @Getter
    @Setter
    private Set<Suoritustapa> suoritustavat = new HashSet<>();

    @Getter
    @OneToOne(mappedBy = "peruste", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private LukiokoulutuksenPerusteenSisalto lukiokoulutuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Lops2019Sisalto lops2019Sisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private AIPEOpetuksenSisalto aipeOpetuksenPerusteenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private TpoOpetuksenSisalto tpoOpetuksenSisalto;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private OpasSisalto oppaanSisalto;

    @Getter
    @Enumerated(EnumType.STRING)
    @NotNull
    private PerusteTila tila = PerusteTila.LUONNOS;

    @Setter
    @Enumerated(EnumType.STRING)
    private KoulutustyyppiToteutus toteutus;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private PerusteTyyppi tyyppi = PerusteTyyppi.NORMAALI;

    @Getter
    @Setter
    @Transient
    private Set<Koodi> tutkintonimikeKoodit;

    @Getter
    @OneToOne(mappedBy = "peruste", fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    private KVLiite kvliite;

    public void setKvliite(KVLiite liite) {
        if (liite != null) {
            this.kvliite = liite;
            liite.setPeruste(this);
        }
    }

    /**
     * Kielet jolla peruste tarjotaan. Oletuksena suomi ja ruotsi.
     */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "peruste_kieli")
    @Column(name = "kieli")
    @Size(min = 1, groups = { Valmis.class, ValmisPohja.class })
    private Set<Kieli> kielet = EnumSet.of(Kieli.FI, Kieli.SV);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "peruste_liite", inverseJoinColumns = {@JoinColumn(name="liite_id")}, joinColumns = {@JoinColumn(name="peruste_id")})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Liite> liitteet = new HashSet<>();

    @OneToOne(mappedBy = "peruste")
    @Getter
    @Setter
    private Perusteprojekti perusteprojekti;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToMany
    @JoinTable(name = "opas_peruste",
            joinColumns = @JoinColumn(name = "opas_id"),
            inverseJoinColumns = @JoinColumn(name = "peruste_id"))
    private Set<Peruste> oppaanPerusteet = new HashSet<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "opas_koulutustyyppi",
            joinColumns = @JoinColumn(name = "opas_id"))
    @Column(name = "koulutustyyppi")
    private Set<KoulutusTyyppi> oppaanKoulutustyypit = new HashSet<>();

    @Getter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "peruste", orphanRemoval = true)
    private Set<PerusteAikataulu> perusteenAikataulut = new HashSet<>();

    public Set<PerusteenSisalto> getSisallot() {
        if (PerusteTyyppi.OPAS.equals(this.getTyyppi())) {
            return Collections.singleton(this.getOppaanSisalto());
        }
        else {
            if (KoulutustyyppiToteutus.AMMATILLINEN.equals(this.getToteutus())) {
                return new HashSet<>(this.getSuoritustavat());
            }
            else if (this.getPerusopetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getPerusopetuksenPerusteenSisalto());
            }
            else if (this.getLops2019Sisalto() != null) {
                return Collections.singleton(this.getLops2019Sisalto());
            }
            else if (this.getEsiopetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getEsiopetuksenPerusteenSisalto());
            }
            else if (this.getLukiokoulutuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getLukiokoulutuksenPerusteenSisalto());
            }
            else if (this.getAipeOpetuksenPerusteenSisalto() != null) {
                return Collections.singleton(this.getAipeOpetuksenPerusteenSisalto());
            }
            else if (this.getTpoOpetuksenSisalto() != null) {
                return Collections.singleton(this.getTpoOpetuksenSisalto());
            }
        }
        return new HashSet<>();
    }

    public void attachLiite(Liite liite) {
        liitteet.add(liite);
    }

    public void removeLiite(Liite liite) {
        liitteet.remove(liite);
    }

    public Suoritustapa getSuoritustapa(Suoritustapakoodi koodi) {
        for (Suoritustapa s : suoritustavat) {
            if (s.getSuoritustapakoodi() == koodi) {
                return s;
            }
        }

        if (koodi.equals(Suoritustapakoodi.REFORMI)) {
            Optional<Suoritustapa> naytto = suoritustavat.stream()
                    .filter(suoritustapa -> suoritustapa.getSuoritustapakoodi().equals(Suoritustapakoodi.NAYTTO))
                    .findFirst();
            if (naytto.isPresent()) {
                return naytto.get();
            }
        }

        throw new IllegalArgumentException("Perusteella ei ole pyydettyä suoritustapaa");
    }

    @Override
    public void asetaTila(PerusteTila tila) {
        this.tila = tila;
    }

    @Override
    public Reference getReference() {
        return new Reference(id);
    }

    public void setMuutosmaaraykset(List<Muutosmaarays> muutosmaaraykset) {
        this.muutosmaaraykset.clear();
        if (muutosmaaraykset != null) {
            this.muutosmaaraykset.addAll(muutosmaaraykset);
        }
    }

    public void setPerusteenAikataulut(List<PerusteAikataulu> perusteenAikataulut) {
        this.perusteenAikataulut.clear();
        if (perusteenAikataulut != null) {
            this.perusteenAikataulut.addAll(perusteenAikataulut);
        }
    }

    @Override
    public KoulutustyyppiToteutus getToteutus() {
        return PerusteUtils.getToteutus(this.toteutus, getKoulutustyyppi(), getTyyppi());
    }

    /*
    Palauttaa suoritustavan mukaisen sisällön.
    */
    public PerusteenOsaViite getSisallot(Suoritustapakoodi suoritustapakoodi) {
        if (this.tyyppi == PerusteTyyppi.OPAS) {
            return this.getOppaanSisalto().getSisalto();
        }

        KoulutusTyyppi kt = KoulutusTyyppi.of(this.koulutustyyppi);
        switch (kt) {
            // Yksinkertaiset perusteet
            case LISAOPETUS:
            case ESIOPETUS:
            case VARHAISKASVATUS:
            case PERUSOPETUSVALMISTAVA:
                EsiopetuksenPerusteenSisalto esiopetusSisalto = this.getEsiopetuksenPerusteenSisalto();
                if (esiopetusSisalto != null) {
                    return esiopetusSisalto.getSisalto();
                }
                break;

            // Perusopetuksen rakenteella
            case PERUSOPETUS:
                PerusopetuksenPerusteenSisalto poSisalto = this.getPerusopetuksenPerusteenSisalto();
                if (poSisalto != null) {
                    return poSisalto.getSisalto();
                }
                break;

            // AIPE-rakenteella
            case AIKUISTENPERUSOPETUS:
                AIPEOpetuksenSisalto aipeSisalto = this.getAipeOpetuksenPerusteenSisalto();
                if (aipeSisalto != null) {
                    return aipeSisalto.getSisalto();
                }
                break;

            // Lukiorakenteella
            case LUKIOKOULUTUS:
            case LUKIOVALMISTAVAKOULUTUS:
            case AIKUISTENLUKIOKOULUTUS:
                // LOPS 2019 toteutus
                if (LOPS2019.equals(toteutus)) {
                    Lops2019Sisalto lops2019Sisalto = this.getLops2019Sisalto();
                    if (lops2019Sisalto != null) {
                        return lops2019Sisalto.getSisalto();
                    }
                    break;
                } else {
                    LukiokoulutuksenPerusteenSisalto lukioSisalto = this.getLukiokoulutuksenPerusteenSisalto();
                    if (lukioSisalto != null) {
                        return lukioSisalto.getSisalto();
                    }
                    break;
                }

            case TPO:
                TpoOpetuksenSisalto sisalto = this.getTpoOpetuksenSisalto();
                if (sisalto != null) {
                    return sisalto.getSisalto();
                }
                break;

            // Ammatillisella rakenteella
            case TELMA:
            case VALMA:
            case PERUSTUTKINTO:
            case AMMATTITUTKINTO:
            case ERIKOISAMMATTITUTKINTO:
                // Ammatilliset
                for (Suoritustapa suoritustapa : this.getSuoritustavat()) {
                    if (suoritustapa.getSuoritustapakoodi().equals(suoritustapakoodi)) {
                        return suoritustapa.getSisalto();
                    }
                }

                if (!this.getSuoritustavat().isEmpty()) {
                    return this.getSuoritustavat().iterator().next().getSisalto();
                }
                break;
        }

        return null;
    }

    @JsonIgnore
    public void setSisalto(PerusopetuksenPerusteenSisalto perusopetuksenPerusteenSisalto) {
        this.perusopetuksenPerusteenSisalto = perusopetuksenPerusteenSisalto;
        this.perusopetuksenPerusteenSisalto.setPeruste(this);
    }

    @JsonIgnore
    public void setSisalto(AIPEOpetuksenSisalto sisalto) {
        this.aipeOpetuksenPerusteenSisalto = sisalto;
        this.aipeOpetuksenPerusteenSisalto.setPeruste(this);
    }

    @JsonIgnore
    public void setSisalto(OpasSisalto sisalto) {
        this.oppaanSisalto = sisalto;
        this.oppaanSisalto.setPeruste(this);
    }

    @JsonIgnore
    public void setSisalto(EsiopetuksenPerusteenSisalto esiopetuksenPerusteenSisalto) {
        this.esiopetuksenPerusteenSisalto = esiopetuksenPerusteenSisalto;
        this.esiopetuksenPerusteenSisalto.setPeruste(this);
    }

    @JsonIgnore
    public void setSisalto(TpoOpetuksenSisalto sisalto) {
        this.tpoOpetuksenSisalto = sisalto;
        this.tpoOpetuksenSisalto.setPeruste(this);
    }

    @JsonIgnore
    public void setSisalto(LukiokoulutuksenPerusteenSisalto lukiokoulutuksenPerusteenSisalto) {
        this.lukiokoulutuksenPerusteenSisalto = lukiokoulutuksenPerusteenSisalto;
        if (lukiokoulutuksenPerusteenSisalto != null) {
            lukiokoulutuksenPerusteenSisalto.setPeruste(this);
        }
    }

    @JsonIgnore
    public void setSisalto(Lops2019Sisalto lops2019Sisalto) {
        this.lops2019Sisalto = lops2019Sisalto;
        this.lops2019Sisalto.setPeruste(this);
    }

    // Käytetään ainoastaan testaamiseen
    @Deprecated
    public void setLops2019Sisalto(Lops2019Sisalto lops2019Sisalto) {
        this.lops2019Sisalto = lops2019Sisalto;
        this.lops2019Sisalto.setPeruste(this);
    }

    public boolean containsViite(PerusteenOsaViite viite) {
        if (suoritustavat != null) {
            for (Suoritustapa s : suoritustavat) {
                if (s.containsViite(viite)) {
                    return true;
                }
            }
        }

        if (perusopetuksenPerusteenSisalto != null) {
            return perusopetuksenPerusteenSisalto.containsViite(viite);
        }

        if (aipeOpetuksenPerusteenSisalto != null) {
            return aipeOpetuksenPerusteenSisalto.containsViite(viite);
        }

        if (tpoOpetuksenSisalto != null) {
            return tpoOpetuksenSisalto.containsViite(viite);
        }

        if (esiopetuksenPerusteenSisalto != null) {
            return esiopetuksenPerusteenSisalto.containsViite(viite);
        }

        if (lukiokoulutuksenPerusteenSisalto != null) {
            return lukiokoulutuksenPerusteenSisalto.containsViite(viite);
        }

        if (lops2019Sisalto != null) {
            return lops2019Sisalto.containsViite(viite);
        }

        if (this.oppaanSisalto != null) {
            return this.oppaanSisalto.containsViite(viite);
        }


        throw new BusinessRuleViolationException("Ei toteutusta koulutustyypillä");
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.peruste;
    }

    public interface Valmis {}
    public interface ValmisPohja {}
    public interface ValmisOpas {}
}
