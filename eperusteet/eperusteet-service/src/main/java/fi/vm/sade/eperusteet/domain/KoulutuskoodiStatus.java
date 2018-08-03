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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "koulutuskoodi_status")
public class KoulutuskoodiStatus {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peruste_id", nullable = false, unique = true)
    private Peruste peruste;

    @Getter
    @Setter
    @Column(name = "aikaleima", nullable = false)
    private Date lastCheck;

    @Getter
    @Setter
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<KoulutuskoodiStatusInfo> infot = new ArrayList<>();

    @Getter
    @Setter
    private boolean kooditOk = false;

    public void setInfot(List<KoulutuskoodiStatusInfo> infot) {
        if (infot != null) {
            this.infot.clear();
            this.infot.addAll(infot);
        }
    }
}
