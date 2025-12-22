package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "kaantaja_taitotasokuvaus")
@Audited
@Getter
@Setter
public class KaantajaTaitotasokuvaus extends PerusteenOsa implements Liitteellinen {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaTaitotasokuvaus", orphanRemoval = true)
    private List<KaantajaTaitotasoTutkintotaso> tutkintotasot = new ArrayList<>();

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public KaantajaTaitotasokuvaus() {
    }

    public KaantajaTaitotasokuvaus(KaantajaTaitotasokuvaus other) {
        super(other);
        copyState(other);
    }

    @Override
    public KaantajaTaitotasokuvaus copy() {
        return new KaantajaTaitotasokuvaus(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajataitotasokuvaus;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KaantajaTaitotasokuvaus) {
            KaantajaTaitotasokuvaus other = (KaantajaTaitotasokuvaus) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setLiite(other.isLiite());

            this.tutkintotasot.clear();
            for (KaantajaTaitotasoTutkintotaso tutkintotaso : other.getTutkintotasot()) {
                KaantajaTaitotasoTutkintotaso newTutkintotaso = new KaantajaTaitotasoTutkintotaso(tutkintotaso);
                newTutkintotaso.setKaantajaTaitotasokuvaus(this);
                this.tutkintotasot.add(newTutkintotaso);
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KaantajaTaitotasokuvaus) {
            KaantajaTaitotasokuvaus that = (KaantajaTaitotasokuvaus) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());

            if (result && getTutkintotasot() != null) {
                Iterator<KaantajaTaitotasoTutkintotaso> i = getTutkintotasot().iterator();
                Iterator<KaantajaTaitotasoTutkintotaso> j = that.getTutkintotasot().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(KaantajaTaitotasokuvaus other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        this.tutkintotasot = other.getTutkintotasot().stream()
                .map(KaantajaTaitotasoTutkintotaso::new)
                .peek(tutkintotaso -> tutkintotaso.setKaantajaTaitotasokuvaus(this))
                .collect(Collectors.toList());
    }
}

