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
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.identityEquals;
import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_oppiaine")
public class Oppiaine extends AbstractAuditedReferenceableEntity {

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
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tehtava;

    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @NotNull(groups = Strict.class)
    @Size(min = 1, groups = Strict.class)
    @Valid
    @BatchSize(size = 3)
    private Set<OppiaineenVuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet;

    @Getter
    @ManyToOne(optional = true)
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

    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private Set<Oppiaine> oppimaarat;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    private Set<OpetuksenKohdealue> kohdealueet = new HashSet<>();

    /**
     * Palauttaa oppimäärät
     *
     * @see #isKoosteinen()
     * @return oppimäärät (joukkoa ei voi muokata) tai null jos oppiaine ei ole koosteinen
     */
    public Set<Oppiaine> getOppimaarat() {
        if (koosteinen == false) {
            return null;
        }
        return oppimaarat == null ? new HashSet<Oppiaine>() : new HashSet<>(oppimaarat);
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
        if (koosteinen == false) {
            throw new IllegalStateException("Oppiaine ei ole koosteinen eikä tue oppimääriä");
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
        if (koosteinen == false) {
            throw new IllegalStateException("Oppiaine ei ole koosteinen eikä tue oppimääriä");
        }
        if (aine.getOppiaine().equals(this) && oppimaarat.remove(aine)) {
            aine.oppiaine = null;
        } else {
            throw new IllegalArgumentException("Oppimäärä ei kuulu tähän oppiaineeseen");
        }
    }

    public void setOppiaine(Oppiaine oppiaine) {
        if (this.oppiaine == null || this.oppiaine.equals(oppiaine)) {
            this.oppiaine = oppiaine;
        } else {
            throw new IllegalStateException("Oppiaineviittausta ei voi muuttaa");
        }
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
     *
     * @param kohdealue
     * @return Lisätty kohdealue tai samanniminen olemassa oleva.
     */
    public OpetuksenKohdealue addKohdealue(OpetuksenKohdealue kohdealue) {
        if (kohdealue.getNimi() != null) {
            for (OpetuksenKohdealue k : kohdealueet) {
                if (k.getNimi().equals(kohdealue.getNimi())) {
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
        return this == other;
    }

    public boolean structureEquals(Oppiaine other) {
        boolean result = Objects.equals(this.getTunniste(), other.getTunniste());
        result &= refXnor(this.getOppiaine(), other.getOppiaine());
        result &= this.getOppiaine() == null || identityEquals(this.getOppiaine(), other.getOppiaine());
        result &= refXnor(this.getNimi(), other.getNimi());
        result &= refXnor(this.getTehtava(), other.getTehtava());
        result &= refXnor(this.getKohdealueet(), other.getKohdealueet());
        result &= this.isKoosteinen() == other.isKoosteinen();
        if ( this.isKoosteinen() ) {
            result &= this.getOppimaarat().size() == other.getOppimaarat().size();
        }
        result &= this.getVuosiluokkakokonaisuudet().size() == other.getVuosiluokkakokonaisuudet().size();

        //TODO tarkista vuosiluokkakokonaisuudet

        return result;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public interface Strict {
    };
}
