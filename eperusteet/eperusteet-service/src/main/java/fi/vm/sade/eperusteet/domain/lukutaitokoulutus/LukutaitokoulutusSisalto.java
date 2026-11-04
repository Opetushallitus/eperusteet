package fi.vm.sade.eperusteet.domain.lukutaitokoulutus;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Kooditettu;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.vst.Opintokokonaisuus;
import fi.vm.sade.eperusteet.domain.vst.Tavoitesisaltoalue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@Table(name = "lukutaitokoulutus_perusteen_sisalto")
public class LukutaitokoulutusSisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto, Kooditettu {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    public LukutaitokoulutusSisalto kloonaa(Peruste peruste) {
        LukutaitokoulutusSisalto lukutaitoSisalto = new LukutaitokoulutusSisalto();
        lukutaitoSisalto.setPeruste(peruste);
        lukutaitoSisalto.setSisalto(sisalto.copy());
        return lukutaitoSisalto;
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
            if (po instanceof Opintokokonaisuus opintokokonaisuus) {
                koodit.addAll(opintokokonaisuus.getOpetuksenTavoitteet());
            }

            if (po instanceof Tavoitesisaltoalue tavoitesisaltoalue) {
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
