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
package fi.vm.sade.eperusteet.domain.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.Mergeable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@DiscriminatorValue("RM")
@Audited
public class RakenneModuuli extends AbstractRakenneOsa implements Mergeable<RakenneModuuli> {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    private MuodostumisSaanto muodostumisSaanto;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private RakenneModuuliRooli rooli;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Osaamisala osaamisala;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "rakennemoduuli_rakenneosa",
               joinColumns = {
                   @JoinColumn(name = "rakennemoduuli_id")},
               inverseJoinColumns = {
                   @JoinColumn(name = "rakenneosa_id")})
    @OrderColumn(name = "osat_order")
    @Getter
    private List<AbstractRakenneOsa> osat = new ArrayList<>();

    public void setOsat(List<AbstractRakenneOsa> osat) {
        this.osat.clear();
        if (osat != null) {
            this.osat.addAll(osat);
        }
    }

    @Override
    public void mergeState(RakenneModuuli moduuli) {
        if (moduuli != null) {
            // FIXME: Tämä räjähtää jos olioon tulee lisää kenttiä
            this.setOsat(moduuli.osat);
            this.nimi = moduuli.nimi;
            this.rooli = moduuli.rooli;
            this.setKuvaus(moduuli.getKuvaus());
            if (this.muodostumisSaanto != null) {
                this.muodostumisSaanto.mergeState(moduuli.getMuodostumisSaanto());
            } else {
                this.muodostumisSaanto = moduuli.getMuodostumisSaanto();
            }
            this.osaamisala = moduuli.osaamisala;

            assert (isSame(moduuli));
        }

    }

    public boolean isSame(RakenneModuuli moduuli) {

        if (moduuli == null) {
            return false;
        }

        TekstiPalanen moduuliNimi = moduuli.getNimi();
        if (!Objects.equals(this.nimi,moduuliNimi)) {
            return false;
        }

        if ((this.osat == null && moduuli.getOsat() != null) || (this.osat != null && moduuli.getOsat() == null)) {
            return false;
        }
        if ((this.muodostumisSaanto == null && moduuli.getMuodostumisSaanto() != null) || (this.muodostumisSaanto != null && moduuli.getMuodostumisSaanto()
            == null)) {
            return false;
        }

        if (moduuli.osat != null) {
            if (this.osat.size() != moduuli.getOsat().size()) {
                return false;
            } else {
                for (int i = 0; i < this.osat.size(); i++) {
                    if (this.osat.get(i) instanceof RakenneModuuli && moduuli.getOsat().get(i) instanceof RakenneModuuli) {
                        if (((RakenneModuuli) this.osat.get(i)).isSame((RakenneModuuli) moduuli.getOsat().get(i)) == false) {
                            return false;
                        }
                    } else if (this.osat.get(i) instanceof RakenneOsa && moduuli.getOsat().get(i) instanceof RakenneOsa) {
                        if (!((RakenneOsa) this.osat.get(i)).getTutkinnonOsaViite().equals(((RakenneOsa) moduuli.getOsat().get(i)).getTutkinnonOsaViite())) {
                            return false;
                        }
                    }
                }
            }
        }

        if (this.muodostumisSaanto != null) {
            if (!this.muodostumisSaanto.equals(moduuli.muodostumisSaanto)) {
                System.out.println("Muodostumissääntö false");
                return false;
            }
        }

        return true;
    }

    public boolean isInRakenne(TutkinnonOsaViite viite, boolean ylinTaso) {

        for (AbstractRakenneOsa rakenneosa : osat) {
            if (rakenneosa instanceof RakenneModuuli) {
                if (((RakenneModuuli) rakenneosa).isInRakenne(viite, false)) {
                    return true;
                }
            } else if (rakenneosa instanceof RakenneOsa) {
                if (((RakenneOsa) rakenneosa).getTutkinnonOsaViite().equals(viite)) {
                    return true;
                }
            }
        }

        return false;
    }

}
