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
package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import com.google.common.base.Objects;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.Optional;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("RO")
@Audited
public class RakenneOsa extends AbstractRakenneOsa {

    @JoinColumn(name = "rakenneosa_tutkinnonosaviite")
    @ManyToOne
    private TutkinnonOsaViite tutkinnonOsaViite;

    private String erikoisuus;

    @Override
    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa other, int depth, boolean excludeText) {
        Optional<RakenneOsaVirhe> supervalidation = super.isSame(other, depth, excludeText);
        if (supervalidation.isPresent()) {
            return supervalidation;
        }

        if (other instanceof RakenneOsa) {
            final RakenneOsa ro = (RakenneOsa) other;
            boolean rakenneOsaValid = this.getPakollinen() == ro.getPakollinen()
                    && Objects.equal(this.tutkinnonOsaViite, ro.getTutkinnonOsaViite())
                    && (erikoisuus == null ? ro.getErikoisuus() == null : erikoisuus.equals(ro.getErikoisuus()));
            if (!rakenneOsaValid) {
                return fail("rakenne-osan-validointi-epaonnistui");
            }
        }

        return success();
    }
}
