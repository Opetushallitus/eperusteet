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

package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Kooditettu;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.Taiteenala;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@Table(name = "vapaasivistystyo_perusteen_sisalto")
public class VapaasivistystyoSisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto, Kooditettu {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    private Integer laajuus;

    public VapaasivistystyoSisalto kloonaa(Peruste peruste) {
        VapaasivistystyoSisalto vstSisalto = new VapaasivistystyoSisalto();
        vstSisalto.setPeruste(peruste);
        vstSisalto.setSisalto(sisalto.copy());
        vstSisalto.setLaajuus(laajuus);
        return vstSisalto;
    }

    public boolean containsViite(PerusteenOsaViite viite) {
        return viite != null && sisalto.getId().equals(viite.getRoot().getId());
    }

    @Override
    public List<Koodi> getKoodit() {
        return sisallonKoodit(sisalto);
    }

    private List<Koodi> sisallonKoodit(PerusteenOsaViite perusteenOsaViite) {
        List<Koodi> koodit = new ArrayList<>();

        for (PerusteenOsaViite lapsi : perusteenOsaViite.getLapset()) {
            PerusteenOsa po = lapsi.getPerusteenOsa();
            if (po != null && po instanceof Opintokokonaisuus) {
                Opintokokonaisuus opintokokonaisuus = (Opintokokonaisuus) po;
                koodit.addAll(opintokokonaisuus.getOpetuksenTavoitteet());
            }

            if (po != null && po instanceof Tavoitesisaltoalue) {
                Tavoitesisaltoalue tavoitesisaltoalue = (Tavoitesisaltoalue) po;
                koodit.addAll(tavoitesisaltoalue.getTavoitealueet().stream()
                        .map(tavoitealue -> tavoitealue.getTavoitteet())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            }

            koodit.addAll(sisallonKoodit(lapsi));
        }

        return koodit;
    }
}
