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

import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author nkala
 */
@Entity
@Audited
@Table(name = "opas_sisalto")
public class OpasSisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    @Getter
    @NotAudited
    @OneToMany(mappedBy = "opasSisalto", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OppaanKiinnitettyKoodi> oppaanKiinnitetytKoodit = new ArrayList<>();

    public void setOppaanKiinnitetytKoodit(List<OppaanKiinnitettyKoodi> oppaanKiinnitetytKoodit) {
        this.oppaanKiinnitetytKoodit.clear();

        if (oppaanKiinnitetytKoodit != null) {
            this.oppaanKiinnitetytKoodit.addAll(oppaanKiinnitetytKoodit);
        }
    }

    public OpasSisalto kloonaa(Peruste peruste) {
        OpasSisalto eps = new OpasSisalto();
        eps.setPeruste(peruste);
        eps.setSisalto(sisalto.copy());

        eps.setOppaanKiinnitetytKoodit(oppaanKiinnitetytKoodit.stream()
                .map(oppaanKiinnitettyKoodi -> oppaanKiinnitettyKoodi.copy(eps))
                .collect(Collectors.toList()));

        return eps;
    }

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && sisalto.getId().equals(viite.getRoot().getId());
    }
}
