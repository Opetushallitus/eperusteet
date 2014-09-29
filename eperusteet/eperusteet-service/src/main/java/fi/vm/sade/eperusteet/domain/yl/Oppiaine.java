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
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_oppiaine")
public class Oppiaine extends AbstractAuditedReferenceableEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @NotNull(groups = Strict.class)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @NotNull(groups = Strict.class)
    @Size(min = 1, groups = Strict.class)
    @Valid
    private Set<OppiaineenVuosiluokkaKokonaisuus> vuosiluokkakokonaisuudet = new HashSet<>();

    @Getter
    @Setter
    @ManyToOne(optional = true)
    private Oppiaine oppiaine;

    @OneToMany(mappedBy = "oppiaine", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<Oppiaine> oppimaarat;

    public void addOppimaara(Oppiaine oppimaara) {
        if ( oppimaarat == null ) {
            oppimaarat = new HashSet<>();
        }
        oppimaara.setOppiaine(this);
        oppimaarat.add(oppimaara);
    }

    public interface Strict {};
}
