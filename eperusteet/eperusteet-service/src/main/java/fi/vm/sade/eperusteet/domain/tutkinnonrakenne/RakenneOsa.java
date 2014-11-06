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
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("RO")
@Audited
@EqualsAndHashCode(callSuper = true)
public class RakenneOsa extends AbstractRakenneOsa {
    @JoinColumn(name = "rakenneosa_tutkinnonosaviite")
    @ManyToOne
    @NotNull
    private TutkinnonOsaViite tutkinnonOsaViite;

    private boolean pakollinen;

    @Override
    public boolean isSame(AbstractRakenneOsa other) {

        if ( !super.isSame(other) ) {
            return false;
        }

        if ( other instanceof RakenneOsa ) {
            final RakenneOsa ro = (RakenneOsa)other;
            return Objects.equal(this.tutkinnonOsaViite, ro.getTutkinnonOsaViite());
        }
        return false;
    }
}
