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
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 *
 * @author nkala
 */
@Entity
@Audited
@Table(name = "yl_aipe_vaihe")
public class AIPEVaihe extends AbstractAuditedReferenceableEntity implements Kloonattava<AIPEVaihe> {
    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    private TekstiOsa siirtymaEdellisesta;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    private TekstiOsa siirtymaSeuraavaan;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa paikallisestiPaatettavatAsiat;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipevaihe_kohdealue",
            joinColumns = {
                    @JoinColumn(name = "vaihe_id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "kohdealue_id")})
    @OrderColumn(name = "kohdealue_order")
    private List<OpetuksenKohdealue> opetuksenKohdealueet = new ArrayList<>(0);

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipevaihe_aipeoppiaine",
               joinColumns = {
                   @JoinColumn(name = "vaihe_id")},
               inverseJoinColumns = {
                   @JoinColumn(name = "oppiaine_id")})
    @OrderColumn(name = "oppiaine_order")
    private List<AIPEOppiaine> oppiaineet = new ArrayList<>(0);

    public AIPEOppiaine getOppiaine(Long oppiaineId) {
        Optional<AIPEOppiaine> result = oppiaineet.stream()
                .filter(oppiaine -> Objects.equals(oppiaine.getId(), oppiaineId))
                .findFirst();

        if (!result.isPresent()) {
            result = oppiaineet.stream()
                .map(AIPEOppiaine::getOppimaarat)
                .flatMap(Collection::stream)
                .filter(oppiaine -> Objects.equals(oppiaine.getId(), oppiaineId))
                .findFirst();
        }

        return result.orElse(null);
    }

    @Override
    public AIPEVaihe kloonaa() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
