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
    @AttributeOverride(name="id", column = @Column(name = "rev")),
    @AttributeOverride(name="timestamp", column=@Column(name="revtstmp"))
})
public class RevisionInfo extends DefaultRevisionEntity {

    @Column
    private String muokkaajaOid;
    @Column(length = 1000)
    private String kommentti;

    public String getMuokkaajaOid() {
        return muokkaajaOid;
    }

    public void setMuokkaajaOid(String muokkaajaOid) {
        this.muokkaajaOid = muokkaajaOid;
    }

    public String getKommentti() {
        return kommentti;
    }

    public void setKommentti(String kommentti) {
        this.kommentti = kommentti;
    }

}
