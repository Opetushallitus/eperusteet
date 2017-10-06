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

import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "yl_kohdealue")
public class OpetuksenKohdealue extends AbstractReferenceableEntity {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @NotNull(message = "Tavoitealueella täytyy olla nimi")
    private TekstiPalanen nimi;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen kuvaus;

    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_oppiaine_yl_kohdealue",
            joinColumns = @JoinColumn(name = "kohdealueet_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_oppiaine_id", nullable = false, updatable = false))
    private Set<Oppiaine> oppiaineet;

    /*@Getter
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "aipevaihe_kohdealue",
            joinColumns = @JoinColumn(name = "kohdealue_id", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "vaihe_id", nullable = false, updatable = false))
    @OrderColumn(name = "kohdealue_order")
    private Set<AIPEVaihe> vaiheet = new HashSet<>();*/


    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_opetuksen_tavoite_yl_kohdealue",
            joinColumns = @JoinColumn(name = "kohdealueet_id", updatable = false, nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id", updatable = false, nullable = false))
    private Set<OpetuksenTavoite> opetuksenTavoitteet;

    public OpetuksenKohdealue kloonaa() {
        OpetuksenKohdealue klooni = new OpetuksenKohdealue();
        klooni.setKuvaus(kuvaus);
        klooni.setNimi(nimi);
        return klooni;
    }
}
