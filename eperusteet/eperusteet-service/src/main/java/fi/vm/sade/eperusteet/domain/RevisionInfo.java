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

import fi.vm.sade.eperusteet.service.impl.AuditRevisionListener;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
@AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "rev")),
    @AttributeOverride(name = "timestamp", column = @Column(name = "revtstmp"))
})
@Getter
@Setter
public class RevisionInfo extends DefaultRevisionEntity {
    private static final int MAX_LEN = 1000;

    @Column
    private String muokkaajaOid;

    @Column(length = MAX_LEN)
    private String kommentti;

    public void addKommentti(String kommentti) {
        if (this.kommentti == null) {
            this.kommentti = kommentti;
        } else if (this.kommentti.length() < (MAX_LEN-2)) {
            this.kommentti = this.kommentti + ("; " + kommentti).substring(0, Math.min(MAX_LEN - this.kommentti.length() - 2, kommentti.length()));
        }
    }
}
