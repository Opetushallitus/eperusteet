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

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "kevyttekstikappale")
@Audited
public class KevytTekstiKappale extends AbstractAuditedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen nimi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private TekstiPalanen teksti;

    public KevytTekstiKappale() {
    }

    public KevytTekstiKappale(KevytTekstiKappale other) {
        copyState(other);
    }

    public KevytTekstiKappale copy() { return new KevytTekstiKappale(this); }

    private void copyState(KevytTekstiKappale other) {
        this.setNimi(other.getNimi());
        this.setTeksti(other.getTeksti());
    }

}
