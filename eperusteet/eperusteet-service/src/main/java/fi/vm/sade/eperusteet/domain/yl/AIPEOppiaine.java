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
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;

import java.util.*;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * @author nkala
 */
@Entity
@Audited
@Table(name = "yl_aipe_oppiaine")
public class AIPEOppiaine extends AbstractAuditedReferenceableEntity implements Kloonattava<AIPEOppiaine>, AIPEJarjestettava {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @NotNull(groups = Oppiaine.Strict.class)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi koodi;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @Setter
    @Valid
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tyotavat;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa ohjaus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa arviointi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa sisaltoalueinfo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    @OrderColumn
    private List<OpetuksenTavoite> tavoitteet = new ArrayList<>();

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable
    @OrderColumn
    private List<KeskeinenSisaltoalue> sisaltoalueet = new ArrayList<>();

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private TekstiPalanen pakollinenKurssiKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "syventava_kurssi_kuvaus")
    private TekstiPalanen syventavaKurssiKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "soveltava_kurssi_kuvaus")
    private TekstiPalanen soveltavaKurssiKuvaus;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipeoppiaine_aipekurssi",
            joinColumns = {@JoinColumn(name = "oppiaine_id")},
            inverseJoinColumns = {@JoinColumn(name = "kurssi_id")})
    @OrderBy("jarjestys, id")
    private List<AIPEKurssi> kurssit = new ArrayList<>();

    @Getter
    @Setter
    private boolean koosteinen = false;

    @Getter
    @Setter
    private boolean abstrakti = false;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable(name = "aipeoppiaine_aipeoppiaine",
            joinColumns = {@JoinColumn(name = "oppiaine_id")},
            inverseJoinColumns = {@JoinColumn(name = "oppimaara_id")})
    @OrderBy("jarjestys, id")
    private List<AIPEOppiaine> oppimaarat = new ArrayList<>();

    @ValidHtml(whitelist = WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kielikasvatus;

    @Getter
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "aipeoppiaine_aipeoppiaine",
            joinColumns = {@JoinColumn(name = "oppimaara_id", insertable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "oppiaine_id", insertable = false, updatable = false)})
    private AIPEOppiaine oppiaine;

    @Override
    public AIPEOppiaine kloonaa() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<OpetuksenTavoite> getTavoitteet() {
        return new ArrayList<>(tavoitteet);
    }

    public void addOpetuksenTavoite(OpetuksenTavoite tavoite) {
        tavoitteet.add(tavoite);
    }

    public void setTavoitteet(List<OpetuksenTavoite> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            this.tavoitteet.addAll(tavoitteet);
        }
    }

    public Optional<AIPEKurssi> getKurssi(Long kurssiId) {
        return kurssit.stream()
                .filter(kurssi -> Objects.equals(kurssi.getId(), kurssiId))
                .findFirst();
    }

}
