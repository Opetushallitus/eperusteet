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
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Tunnistettava;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;

import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
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
@Table(name = "yl_aipe_kurssi", schema = "public")
@Audited
public class AIPEKurssi extends AbstractAuditedReferenceableEntity implements AIPEJarjestettava, Tunnistettava {

    @Getter
    @Column(nullable = false, unique = true, updatable = false)
    private UUID tunniste = UUID.randomUUID();

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "kuvaus_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi koodi;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @ManyToOne
    @NotAudited
    @JoinTable(name = "aipeoppiaine_aipekurssi",
            joinColumns = { @JoinColumn(name = "kurssi_id", insertable = false, updatable = false)},
            inverseJoinColumns = { @JoinColumn(name = "oppiaine_id", insertable = false, updatable = false)})
    private AIPEOppiaine oppiaine;

    @ManyToMany
    @JoinTable
    private Set<OpetuksenTavoite> tavoitteet = new HashSet<>();

    public Set<OpetuksenTavoite> getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(Set<OpetuksenTavoite> tavoitteet) {
        this.tavoitteet = tavoitteet;
    }

    public static void validateChange(AIPEKurssi a, AIPEKurssi b) {
        Koodi.validateChange(a.koodi, b.koodi);

        if (!Objects.equals(a.getTunniste(), b.getTunniste())) {
            throw new BusinessRuleViolationException("tunnistetta-ei-voi-muuttaa");
        }

        if (a.nimi != null && b.nimi == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.kuvaus != null && b.kuvaus == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

    }

}
