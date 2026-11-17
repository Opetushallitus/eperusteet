package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Kooditettu;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.vst.TavoiteAlue;
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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "tpo_perusteen_sisalto")
public class TpoOpetuksenSisalto extends AbstractAuditedReferenceableEntity implements PerusteenSisalto, Kooditettu {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(nullable = false, updatable = false)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    public TpoOpetuksenSisalto kloonaa(Peruste peruste) {
        TpoOpetuksenSisalto tpoSisalto = new TpoOpetuksenSisalto();
        tpoSisalto.setPeruste(peruste);
        tpoSisalto.setSisalto(sisalto.copy());
        return tpoSisalto;
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
            if (po instanceof Taiteenala taiteenala && taiteenala.getKoodi() != null) {
                koodit.add(taiteenala.getKoodi());
            }

            if (po instanceof TaiteenalaVaihe taiteenalaVaihe) {
                if (taiteenalaVaihe.getKoodi() != null) {
                    koodit.add(taiteenalaVaihe.getKoodi());
                }
                if (taiteenalaVaihe.getTavoitealueet() != null) {
                    koodit.addAll(taiteenalaVaihe.getTavoitealueet().stream()
                            .map(TavoiteAlue::getOtsikko)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                    koodit.addAll(taiteenalaVaihe.getTavoitealueet().stream()
                            .map(TavoiteAlue::getTavoitteet)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                }
            }

            koodit.addAll(sisallonKoodit(lapsi));
        }

        return koodit;
    }

}
