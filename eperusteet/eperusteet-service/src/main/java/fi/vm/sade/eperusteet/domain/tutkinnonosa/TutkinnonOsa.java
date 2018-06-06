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
package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa_2018.Ammattitaitovaatimus;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import static fi.vm.sade.eperusteet.service.util.Util.refXnor;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "tutkinnonosa")
@JsonTypeName("tutkinnonosa")
@Audited
public class TutkinnonOsa extends PerusteenOsa implements Serializable {

    @Getter
    @Enumerated(EnumType.STRING)
    @NotNull
    private TutkinnonOsaTyyppi tyyppi = TutkinnonOsaTyyppi.NORMAALI;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen ammattitaidonOsoittamistavat;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tavoitteet;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private Koodi koodi;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(schema = "tutkinnonosa_2018")
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    private List<Ammattitaitovaatimus> ammattitaitovaatimukset2018 = new ArrayList<>();

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen ammattitaitovaatimukset;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "ammattitaitovaatimuksenkohdealue_tutkinnonosa",
            joinColumns = @JoinColumn(name = "tutkinnonosa_id"),
            inverseJoinColumns = @JoinColumn(name = "ammattitaitovaatimuksenkohdealue_id"))
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    private List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimuksetLista = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Arviointi arviointi;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_tutkinnonosa_osaalue",
               joinColumns = @JoinColumn(name = "tutkinnonosa_id"),
               inverseJoinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"))
    @OrderColumn
    // TUTKE2:n mukainen osa-alue
    private List<OsaAlue> osaAlueet;

    /**
     * @deprecated Muutettu käyttämään koodia ja säilytetty, jotta rajapinta ei muutu
     */
    @Deprecated
    @Column(name = "koodi_uri")
    private String koodiUri;

    /**
     * @deprecated Muutettu käyttämään koodia ja säilytetty, jotta rajapinta ei muutu
     */
    @Deprecated
    @Column(name = "koodi_arvo")
    private String koodiArvo;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    private ValmaTelmaSisalto valmaTelmaSisalto;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_tutkinnonosa_kevyttekstikappale",
            joinColumns = @JoinColumn(name = "tutkinnonosa_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    public TutkinnonOsa() {
    }

    public TutkinnonOsa(TutkinnonOsa other) {
        super(other);
        copyState(other);
    }

    @PrePersist
    void prePersist() {
        if (this.getTyyppi() == TutkinnonOsaTyyppi.TUTKINNONOSA2018) {
            this.arviointi = null;
            this.ammattitaitovaatimukset = null;
            this.arviointi = null;
            this.osaAlueet = null;
            this.valmaTelmaSisalto = null;
        }
    }

    public TekstiPalanen getKuvaus() {
        return kuvaus;
    }

    public void setKuvaus(TekstiPalanen kuvaus) {
        this.kuvaus = kuvaus;
    }

    public TekstiPalanen getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(TekstiPalanen tavoitteet) {
        this.tavoitteet = tavoitteet;
    }

    public TekstiPalanen getAmmattitaidonOsoittamistavat() {
        return ammattitaidonOsoittamistavat;
    }

    public void setAmmattitaidonOsoittamistavat(TekstiPalanen ammattitaidonOsoittamistavat) {
        this.ammattitaidonOsoittamistavat = ammattitaidonOsoittamistavat;
    }

    public void setTyyppi(TutkinnonOsaTyyppi tyyppi) {
        this.tyyppi = tyyppi == null ? TutkinnonOsaTyyppi.NORMAALI : tyyppi;
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(getId());
    }

    @Override
    public TutkinnonOsa copy() {
        return new TutkinnonOsa(this);
    }

    public TekstiPalanen getAmmattitaitovaatimukset() {
        return ammattitaitovaatimukset;
    }

    public void setAmmattitaitovaatimukset(TekstiPalanen ammattitaitovaatimukset) {
        this.ammattitaitovaatimukset = ammattitaitovaatimukset;
    }

    public Arviointi getArviointi() {
        return arviointi;
    }

    public void setArviointi(Arviointi arviointi) {
        if ( Objects.equals(this.arviointi, arviointi) ) {
            return;
        }
        if ( arviointi == null || this.arviointi == null ) {
            this.arviointi = arviointi;
        } else {
            this.arviointi.mergeState(arviointi);
            this.muokattu();
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa other) {
        boolean result = false;
        if (other instanceof TutkinnonOsa) {
            TutkinnonOsa that = (TutkinnonOsa) other;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());
            result &= Objects.equals(getTyyppi(), that.getTyyppi());
            result &= Objects.equals(getKoodi(), that.getKoodi());
            // Todo: ei toimi vanhojen kanssa
            //result &= Objects.equals(getKoodiArvo(), that.getKoodiArvo());
            //result &= Objects.equals(getKoodiUri(), that.getKoodiUri());
            result &= refXnor(getTavoitteet(), that.getTavoitteet());
            result &= refXnor(getAmmattitaidonOsoittamistavat(), that.getAmmattitaidonOsoittamistavat());
            result &= refXnor(getAmmattitaitovaatimukset(), that.getAmmattitaitovaatimukset());
            result &= refXnor(getArviointi(), that.getArviointi());
            if (result && getArviointi() != null) {
                result &= getArviointi().structureEquals(that.getArviointi());
            }
            result &= refXnor(getOsaAlueet(), that.getOsaAlueet());
            if (result && getOsaAlueet() != null) {
                Iterator<OsaAlue> i = getOsaAlueet().iterator();
                Iterator<OsaAlue> j = that.getOsaAlueet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= i.next().structureEquals(j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TutkinnonOsa) {
            TutkinnonOsa other = (TutkinnonOsa) perusteenOsa;
            this.setArviointi(other.getArviointi());
            this.setAmmattitaitovaatimukset(other.getAmmattitaitovaatimukset());
            this.setAmmattitaitovaatimuksetLista(connectAmmattitaitovaatimusListToTutkinnonOsa(other));
            this.setAmmattitaidonOsoittamistavat(other.getAmmattitaidonOsoittamistavat());
            this.setTavoitteet(other.getTavoitteet());
            this.setKoodi(other.getKoodi());
            this.setTyyppi(other.getTyyppi());
            this.setKuvaus(other.getKuvaus());
            this.setValmaTelmaSisalto(other.getValmaTelmaSisalto());
            this.setVapaatTekstit(other.getVapaatTekstit());
            if (other.getOsaAlueet() != null) {
                this.setOsaAlueet(mergeOsaAlueet(this.getOsaAlueet(), other.getOsaAlueet()));
            }
        }
    }

    private List<AmmattitaitovaatimuksenKohdealue> connectAmmattitaitovaatimusListToTutkinnonOsa(TutkinnonOsa other) {
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : other.getAmmattitaitovaatimuksetLista()) {
            ammattitaitovaatimuksenKohdealue.connectAmmattitaitovaatimuksetToKohdealue(ammattitaitovaatimuksenKohdealue);
        }
        return other.getAmmattitaitovaatimuksetLista();
    }

    private void copyState(TutkinnonOsa other) {
        this.koodi = other.getKoodi();
        this.kuvaus = other.getKuvaus();
        this.tyyppi = other.getTyyppi();
        this.ammattitaidonOsoittamistavat = other.getAmmattitaidonOsoittamistavat();
        this.tavoitteet = other.getTavoitteet();
        this.arviointi = other.getArviointi() == null ? null : new Arviointi(other.getArviointi());
        this.ammattitaitovaatimukset = other.getAmmattitaitovaatimukset();
        this.koodiUri = other.getKoodiUri();
        this.koodiArvo = other.getKoodiArvo();
        this.ammattitaitovaatimuksetLista = other.getAmmattitaitovaatimuksetLista().stream()
                .map(AmmattitaitovaatimuksenKohdealue::new)
                .collect(Collectors.toList());
        if (other.getValmaTelmaSisalto() != null) {
            this.valmaTelmaSisalto = new ValmaTelmaSisalto(other.getValmaTelmaSisalto());
        }
        if (TutkinnonOsaTyyppi.isTutke(this.getTyyppi()) && other.getOsaAlueet() != null) {
            this.osaAlueet = new ArrayList<>();
            for (OsaAlue o : other.getOsaAlueet()) {
                this.osaAlueet.add(new OsaAlue(o));
            }
        }
    }

    private List<OsaAlue> mergeOsaAlueet(List<OsaAlue> current, List<OsaAlue> other) {
        List<OsaAlue> tempList = new ArrayList<>();
        boolean loyty = false;
        if (other != null) {
            for (OsaAlue osaAlueOther : other) {
                for (OsaAlue osaAlueCurrent : current) {
                    if (osaAlueCurrent.getId().equals(osaAlueOther.getId())) {
                        // Jos tutkinnon osalla osa-aluelista mergessä, niin kyseessä on kevyempi
                        // osa-alue objekteja. Joten käytetään partialMergeStatea.
                        osaAlueCurrent.partialMergeState(osaAlueOther);
                        tempList.add(osaAlueCurrent);
                        loyty = true;
                    }
                }
                if (!loyty) {
                    tempList.add(osaAlueOther);
                }
                loyty = false;
            }
        }

        return tempList;
    }

    @Deprecated
    public String getKoodiUri() {
        if (this.getKoodi() != null) {
            return this.getKoodi().getUri();
        } else {
            return koodiUri;
        }
    }

    @Deprecated
    public void setKoodiUri(String koodiUri) {
        // NOP
    }

    @Deprecated
    public String getKoodiArvo() {
        return koodiArvo;
    }

    @Deprecated
    public void setKoodiArvo(String koodiArvo) {
        // NOP
    }

}
