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
package fi.vm.sade.eperusteet.domain.liite;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "liite")
public class Liite implements Serializable {

    @Id
    @Getter
    @Setter
    @Column(updatable = false)
    private UUID id;

    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    private LiiteTyyppi tyyppi = LiiteTyyppi.TUNTEMATON;

    @Getter
    @NotNull
    @Basic(optional = false)
    private String mime;
    
    @Getter
    //@NotNull
    @Size(max = 1024)
    private String nimi;

    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    @Getter
    @Basic(fetch = FetchType.LAZY, optional = false)
    @Lob
    @NotNull
    private Blob data;

    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "peruste_liite",
            joinColumns = @JoinColumn(name="liite_id"),
            inverseJoinColumns = @JoinColumn(name="peruste_id"))
    @Getter
    @Setter
    private Set<Peruste> perusteet;

    protected Liite() {
        //JPA
    }

    public Liite(UUID uuid, LiiteTyyppi tyyppi, String mime, String nimi, Blob data) {
        this.id = uuid;
        this.luotu = new Date();
        this.nimi = nimi;
        this.tyyppi = tyyppi;
        this.mime = mime;
        this.data = data;
    }

    public Liite(LiiteTyyppi tyyppi, String mime, String nimi, Blob data) {
        this.id = UUID.randomUUID();
        this.luotu = new Date();
        this.nimi = nimi;
        this.tyyppi = tyyppi;
        this.mime = mime;
        this.data = data;
    }

    public Date getLuotu() {
        return luotu == null ? null : new Date(luotu.getTime());
    }

}
