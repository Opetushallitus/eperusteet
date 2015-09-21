/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;

import javax.persistence.MappedSuperclass;
import java.util.Set;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 11.37
 */
@MappedSuperclass
public abstract class AbstractOppiaineOpetuksenSisalto extends AbstractAuditedReferenceableEntity {

    public abstract PerusteenOsaViite getSisalto();

    public abstract Set<Oppiaine> getOppiaineet();

    public abstract Peruste getPeruste();

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && getSisalto().getId().equals(viite.getRoot().getId());
    }

    public void addOppiaine(Oppiaine oppiaine) {
        if (oppiaine.getOppiaine() != null) {
            if (containsOppiaine(oppiaine.getOppiaine())) {
                oppiaine.getOppiaine().addOppimaara(oppiaine);
            } else {
                throw new IllegalArgumentException("Ei voida lisätä oppimäärää jonka oppiaine ei kuulu sisältöön");
            }

        } else {
            getOppiaineet().add(oppiaine);
        }
    }

    public boolean containsOppiaine(Oppiaine aine) {
        if (aine == null) {
            return false;
        }
        if (aine.getOppiaine() != null) {
            return containsOppiaine(aine.getOppiaine());
        }

        if (getOppiaineet().contains(aine) ) {
            return true;
        }

        //revisioissa ei voi verrata object-identityn perusteella vaan täytyy käyttää pääavainta
        for (Oppiaine o : getOppiaineet()) {
            if (o.getId() != null && o.getId().equals(aine.getId()) ) {
                return true;
            }
        }

        return false;
    }

    public void removeOppiaine(Oppiaine aine) {
        getOppiaineet().remove(aine);
    }

}
