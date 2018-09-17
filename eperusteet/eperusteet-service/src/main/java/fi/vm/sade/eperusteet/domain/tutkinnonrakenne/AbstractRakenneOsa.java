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

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "tutkinnon_rakenne")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tyyppi")
@Audited
public abstract class AbstractRakenneOsa implements Serializable, ReferenceableEntity {

    @Getter
    @NotNull
    @Column(updatable = false, unique = true)
    private UUID tunniste;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi vieras;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    private Boolean pakollinen = false;

    public AbstractRakenneOsa() {
    }

    @PrePersist
    public void prePersist() {
        if (this.tunniste == null) {
            this.tunniste = UUID.randomUUID();
        }
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    public void setTunniste(UUID tunniste) {
        if (tunniste != null) {
            this.tunniste = tunniste;
        }
    }

    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa other, boolean includeText) {
        return this.isSame(other, 0, includeText);
    }

    public Optional<RakenneOsaVirhe> isSame(AbstractRakenneOsa other, int depth, boolean includeText) {
        if (other == null) {
            return fail();
        }

        if (this == other) {
            return success();
        }

        if (pakollinen != other.getPakollinen()) {
            return fail("ryhman-pakollisuutta-ei-voi-muuttaa");
        }
        else if (!Objects.equals(tunniste, other.getTunniste())) {
            return fail("ryhman-tunnistetta-ei-voi-muuttaa");
        }
        else if (includeText && !Objects.equals(kuvaus, other.getKuvaus())) {
            return fail("ryhman-kuvausta-ei-voi-muuttaa");
        }

        return success();
    }

    static public class RakenneOsaVirhe {
        @Getter
        String message = "";

        protected RakenneOsaVirhe() {
        }

        protected RakenneOsaVirhe(String message) {
            this.message = message;
        }
    }

    static protected Optional<RakenneOsaVirhe> fail() {
        return Optional.of(new RakenneOsaVirhe("vain-tekstimuutokset-sallittu"));
    }

    static protected Optional<RakenneOsaVirhe> fail(String reason) {
        return Optional.of(new RakenneOsaVirhe(reason));
    }

    static protected Optional<RakenneOsaVirhe> success() {
        return Optional.empty();
    }

}
