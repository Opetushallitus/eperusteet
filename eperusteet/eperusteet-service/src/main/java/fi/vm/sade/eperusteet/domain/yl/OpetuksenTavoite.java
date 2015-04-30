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
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "yl_opetuksen_tavoite")
@Audited
public class OpetuksenTavoite extends AbstractReferenceableEntity {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen tavoite;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen arvioinninOtsikko;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen arvioinninKuvaus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen arvioinninOsaamisenKuvaus;

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<KeskeinenSisaltoalue> sisaltoalueet = new HashSet<>();

    @Getter
    @Setter
    @ManyToMany
    private Set<LaajaalainenOsaaminen> laajattavoitteet = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TavoitteenArviointi> arvioinninkohteet = new HashSet<>();

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<OpetuksenKohdealue> kohdealueet = new HashSet<>();

    public Set<TavoitteenArviointi> getArvioinninkohteet() {
        return new HashSet<>(arvioinninkohteet);
    }

    public void setArvioinninkohteet(Set<TavoitteenArviointi> kohteet) {
        this.arvioinninkohteet.clear();
        if (kohteet != null) {
            this.arvioinninkohteet.addAll(kohteet);
        }
    }

    public OpetuksenTavoite kloonaa(
            Map<KeskeinenSisaltoalue, KeskeinenSisaltoalue> keskeinenSisaltoalueMapper,
            Map<LaajaalainenOsaaminen, LaajaalainenOsaaminen> laajainenOsaaminenMapper,
            Map<OpetuksenKohdealue, OpetuksenKohdealue> kohdealueMapper) {
        OpetuksenTavoite klooni = new OpetuksenTavoite();
        klooni.setArvioinninKuvaus(arvioinninKuvaus);
        klooni.setArvioinninOsaamisenKuvaus(arvioinninOsaamisenKuvaus);
        klooni.setArvioinninOtsikko(arvioinninOtsikko);
        klooni.setTavoite(tavoite);

        for (KeskeinenSisaltoalue sisalto : sisaltoalueet) {
            klooni.getSisaltoalueet().add(keskeinenSisaltoalueMapper.get(sisalto));
        }

        for (LaajaalainenOsaaminen laaja : laajattavoitteet) {
            klooni.getLaajattavoitteet().add(laajainenOsaaminenMapper.get(laaja));
        }

        for (OpetuksenKohdealue kohdealue : kohdealueet) {
            klooni.getKohdealueet().add(kohdealueMapper.get(kohdealue));
        }

        Set<TavoitteenArviointi> kohteet = new HashSet<>();
        for (TavoitteenArviointi kohde : arvioinninkohteet) {
            kohteet.add(kohde.kloonaa());
        }
        klooni.setArvioinninkohteet(kohteet);

        return klooni;
    }

}
