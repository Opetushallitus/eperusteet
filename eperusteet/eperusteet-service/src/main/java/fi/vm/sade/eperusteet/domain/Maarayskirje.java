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

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 *
 * @author ohel
 */
@Entity
@Table(name = "maarayskirje")
@Audited
public class Maarayskirje implements Serializable {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @CollectionTable(name = "maarayskirje_url")
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Kieli, String> url;

    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "maarayskirje_liite",
            joinColumns = {
                    @JoinColumn(name = "maarayskirje_id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "liite_id")})
    private Map<Kieli, Liite> liitteet = new HashMap<>();

    public Maarayskirje copy() {
        Maarayskirje copy = new Maarayskirje();
        Map<Kieli, Liite> liitteet = new HashMap<>();
        this.liitteet.keySet().forEach(kieli -> liitteet.put(kieli, new Liite(
                this.liitteet.get(kieli).getTyyppi(),
                this.liitteet.get(kieli).getMime(),
                this.liitteet.get(kieli).getNimi(),
                this.liitteet.get(kieli).getData())));
        copy.setLiitteet(liitteet);

        return copy;
    }
}
