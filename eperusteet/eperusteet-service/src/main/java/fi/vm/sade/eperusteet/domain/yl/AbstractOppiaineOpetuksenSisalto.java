package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;

import jakarta.persistence.MappedSuperclass;
import java.util.HashSet;
import java.util.Set;

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

    public Set<Oppiaine> getOppiaineetCopy() {
        return new HashSet<>(getOppiaineet());
    }

    public void removeOppiaine(Oppiaine aine) {
        getOppiaineet().remove(aine);
    }

}
