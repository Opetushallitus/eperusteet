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
package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import com.google.common.base.Objects;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.PartialMergeable;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohde;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.Ammattitaitovaatimus;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidOsaamistavoiteEsitieto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "osaamistavoite")
@Audited
@ValidOsaamistavoiteEsitieto
public class Osaamistavoite implements Serializable, PartialMergeable<Osaamistavoite>, ReferenceableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    private boolean pakollinen;

    @Getter
    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal laajuus;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tavoitteet;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tunnustaminen;

    @Getter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Arviointi arviointi;


    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "ammattitaitovaatimuksenkohdealue_osaamistavoite",
            joinColumns = @JoinColumn(name = "osaamistavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "ammattitaitovaatimuksenkohdealue_id"))
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    private List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimuksetLista = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    private Osaamistavoite esitieto;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    /**
     * Jos osaamistavoiteesta on vain yksi kieliversio, määritellään se tässä.
     */
    private Kieli kieli;

    public Osaamistavoite() {
    }

    Osaamistavoite(Osaamistavoite ot, Map<Osaamistavoite, Osaamistavoite> esitiedot) {
        this.nimi = ot.getNimi();
        this.pakollinen = ot.isPakollinen();
        this.laajuus = ot.getLaajuus();
        this.tunnustaminen = ot.getTunnustaminen();
        this.kieli = ot.kieli;
        this.arviointi = ot.getArviointi() == null ? null : new Arviointi(ot.getArviointi());

        if (ot.getEsitieto() != null) {
            this.esitieto = esitiedot.get(ot.getEsitieto());
            if (this.esitieto == null) {
                this.esitieto = new Osaamistavoite(ot.getEsitieto(), esitiedot);
                esitiedot.put(ot.getEsitieto(), this.esitieto);
            }
        }
    }

    public void setArviointi(Arviointi arviointi) {
        if (this.arviointi == null || arviointi == null || this.arviointi == arviointi) {
            this.arviointi = arviointi;
        } else {
            this.arviointi.mergeState(arviointi);
        }
    }

    @Override
    public void mergeState(Osaamistavoite updated) {

        if (updated != null) {
            this.setNimi(updated.getNimi());
            this.setPakollinen(updated.isPakollinen());
            this.setKieli(updated.getKieli());
            this.setLaajuus(updated.getLaajuus());
            this.setTavoitteet(updated.getTavoitteet());
            this.setTunnustaminen(updated.getTunnustaminen());
            this.setArviointi(updated.getArviointi());
            connectAmmattitaitovaatimusListToTutkinnonOsa(updated);
            this.setAmmattitaitovaatimuksetLista(updated.getAmmattitaitovaatimuksetLista());
            this.setEsitieto(updated.getEsitieto());
        }
    }

    private List<AmmattitaitovaatimuksenKohdealue> connectAmmattitaitovaatimusListToTutkinnonOsa(Osaamistavoite other) {
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : other.getAmmattitaitovaatimuksetLista()) {
            ammattitaitovaatimuksenKohdealue.connectAmmattitaitovaatimuksetToKohdealue(ammattitaitovaatimuksenKohdealue );
        }
        return other.getAmmattitaitovaatimuksetLista();
    }

    @Override
    public void partialMergeState(Osaamistavoite updated) {
        if (updated != null) {
            this.setNimi(updated.getNimi());
            this.setPakollinen(updated.isPakollinen());
            this.setLaajuus(updated.getLaajuus());
            this.setKieli(updated.getKieli());
        }
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(id);
    }

    public void setEsitieto(Osaamistavoite esitieto) {
        if (this == esitieto) {
            throw new IllegalArgumentException("Osaamistavoite ei voi olla oma esitietonsa");
        }
        this.esitieto = esitieto;
    }

    public boolean structureEquals(Osaamistavoite other) {
        boolean result = refXnor(getNimi(), other.getNimi());
        result &= isPakollinen() == other.isPakollinen();
        result &= Objects.equal(getLaajuus(), other.getLaajuus());
        result &= refXnor(getTavoitteet(), other.getTavoitteet());
        result &= refXnor(getTunnustaminen(), other.getTunnustaminen());
        result &= refXnor(getEsitieto(), other.getEsitieto());
        result &= refXnor(getArviointi(), other.getArviointi());
        if (getArviointi() != null) {
            result &= getArviointi().structureEquals(other.getArviointi());
        }
        return result;
    }

}
