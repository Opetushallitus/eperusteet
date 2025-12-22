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
@Table(name = "kaantaja_kielitaito")
@Audited
@Getter
@Setter
public class KaantajaKielitaito extends PerusteenOsa implements Liitteellinen {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaKielitaito", orphanRemoval = true)
    private List<KaantajaKielitaitoTaitotaso> taitotasot = new ArrayList<>();

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public KaantajaKielitaito() {
    }

    public KaantajaKielitaito(KaantajaKielitaito other) {
        super(other);
        copyState(other);
    }

    @Override
    public KaantajaKielitaito copy() {
        return new KaantajaKielitaito(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajakielitaito;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KaantajaKielitaito) {
            KaantajaKielitaito other = (KaantajaKielitaito) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setLiite(other.isLiite());

            this.taitotasot.clear();
            for (KaantajaKielitaitoTaitotaso taitotaso : other.getTaitotasot()) {
                KaantajaKielitaitoTaitotaso newTaitotaso = new KaantajaKielitaitoTaitotaso(taitotaso);
                newTaitotaso.setKaantajaKielitaito(this);
                this.taitotasot.add(newTaitotaso);
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KaantajaKielitaito) {
            KaantajaKielitaito that = (KaantajaKielitaito) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());

            if (result && getTaitotasot() != null) {
                Iterator<KaantajaKielitaitoTaitotaso> i = getTaitotasot().iterator();
                Iterator<KaantajaKielitaitoTaitotaso> j = that.getTaitotasot().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(KaantajaKielitaito other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        this.taitotasot = other.getTaitotasot().stream()
                .map(KaantajaKielitaitoTaitotaso::new)
                .peek(taitotaso -> taitotaso.setKaantajaKielitaito(this))
                .collect(Collectors.toList());
    }
}

