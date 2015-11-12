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
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.*;

import static fi.vm.sade.eperusteet.service.util.Util.identityEquals;

/**
 * Kuvaa oppimäärän yhteen vuosiluokkakokonaisuuteen osalta.
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_oppiaineen_vlkok")
public class OppiaineenVuosiluokkaKokonaisuus extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(optional = false)
    @NotNull
    private Oppiaine oppiaine;

    @RelatesToPeruste
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private VuosiluokkaKokonaisuus vuosiluokkaKokonaisuus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa tyotavat;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa ohjaus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa arviointi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa sisaltoalueinfo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    @OrderColumn
    private List<OpetuksenTavoite> tavoitteet = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    @OrderColumn
    private List<KeskeinenSisaltoalue> sisaltoalueet = new ArrayList<>();

    public List<OpetuksenTavoite> getTavoitteet() {
        return new ArrayList<>(tavoitteet);
    }

    public void addOpetuksenTavoite(OpetuksenTavoite tavoite) {
        tavoitteet.add(tavoite);
    }

    public void setTavoitteet(List<OpetuksenTavoite> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            this.tavoitteet.addAll(tavoitteet);
        }
    }

    public void addSisaltoalue(KeskeinenSisaltoalue sisalto) {
        sisaltoalueet.add(sisalto);
    }

    public List<KeskeinenSisaltoalue> getSisaltoalueet() {
        return new ArrayList<>(sisaltoalueet);
    }

    public void setSisaltoalueet(List<KeskeinenSisaltoalue> sisaltoalueet) {
        this.sisaltoalueet.clear();
        if (sisaltoalueet != null) {
            this.sisaltoalueet.addAll(sisaltoalueet);
        }
    }

    public boolean structureEquals(OppiaineenVuosiluokkaKokonaisuus other) {
        boolean result = identityEquals(this.getOppiaine(), other.getOppiaine());
        result &= identityEquals(this.getVuosiluokkaKokonaisuus(), other.getVuosiluokkaKokonaisuus());
        //result &= refXnor(this.getArviointi(), other.getArviointi());
        //result &= refXnor(this.getOhjaus(), other.getOhjaus());
        //result &= refXnor(this.getSisaltoalueinfo(), other.getSisaltoalueinfo());
        //result &= refXnor(this.getTehtava(), other.getTehtava());
        //result &= refXnor(this.getTyotavat(), other.getTyotavat());
        result &= this.getSisaltoalueet().size() == other.getSisaltoalueet().size();
        result &= this.getTavoitteet().size() == other.getTavoitteet().size();

        if ( result ) {
            Iterator<KeskeinenSisaltoalue> i = this.getSisaltoalueet().iterator();
            Iterator<KeskeinenSisaltoalue> j = other.getSisaltoalueet().iterator();
            while ( result && i.hasNext() && j.hasNext() ) {
                result &= identityEquals(i.next(), j.next());
            }
        }

        if ( result ) {
            Iterator<OpetuksenTavoite> i = this.getTavoitteet().iterator();
            Iterator<OpetuksenTavoite> j = other.getTavoitteet().iterator();
            while ( result && i.hasNext() && j.hasNext() ) {
                result &= identityEquals(i.next(), j.next());
            }
        }

        return result;
    }

    public OppiaineenVuosiluokkaKokonaisuus kloonaa(
            Map<VuosiluokkaKokonaisuus, VuosiluokkaKokonaisuus> vuosiluokkaKokonaisuusMapper,
            Map<LaajaalainenOsaaminen, LaajaalainenOsaaminen> laajainenOsaaminenMapper,
            Map<OpetuksenKohdealue, OpetuksenKohdealue> kohdealueMapper) {
        OppiaineenVuosiluokkaKokonaisuus ovlk = new OppiaineenVuosiluokkaKokonaisuus();
        ovlk.setArviointi(arviointi);
        ovlk.setOhjaus(ohjaus);
        ovlk.setTehtava(tehtava);
        ovlk.setTyotavat(tyotavat);
        ovlk.setSisaltoalueinfo(sisaltoalueinfo);

        ovlk.setVuosiluokkaKokonaisuus(vuosiluokkaKokonaisuusMapper.get(vuosiluokkaKokonaisuus));

        Map<KeskeinenSisaltoalue, KeskeinenSisaltoalue> keskeinenSisaltoalueMapper = new HashMap<>();
        for (KeskeinenSisaltoalue sisalto : sisaltoalueet) {
            KeskeinenSisaltoalue klooni = sisalto.kloonaa();
            keskeinenSisaltoalueMapper.put(sisalto, klooni);
            ovlk.addSisaltoalue(klooni);
        }

        for (OpetuksenTavoite tavoite : tavoitteet) {
            OpetuksenTavoite klooni = tavoite.kloonaa(keskeinenSisaltoalueMapper, laajainenOsaaminenMapper, kohdealueMapper);
            ovlk.addOpetuksenTavoite(klooni);
        }

        return ovlk;
    }

}
