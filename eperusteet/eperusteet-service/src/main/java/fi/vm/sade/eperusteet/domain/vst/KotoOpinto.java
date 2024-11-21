package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "koto_opinto")
@Audited
@Getter
@Setter
public class KotoOpinto extends PerusteenOsa implements Serializable, KotoSisalto {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.TAVOITESISALTOALUEENOTSIKKO)
    private Koodi nimiKoodi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "koto_opinto_taitotasot",
            joinColumns = @JoinColumn(name = "koto_opinto_id"),
            inverseJoinColumns = @JoinColumn(name = "taitotaso_id"))
    private List<KotoTaitotaso> taitotasot = new ArrayList<>();

    public KotoOpinto() {

    }

    public KotoOpinto(KotoOpinto other) {
        super(other);
        copyState(other);
    }

    @Override
    public KotoOpinto copy() {
        return new KotoOpinto(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KotoOpinto) {
            KotoOpinto other = (KotoOpinto) perusteenOsa;
            setNimi(other.getNimi());
            setNimiKoodi(other.getNimiKoodi());
            setKuvaus(other.getKuvaus());

            this.taitotasot = new ArrayList<>();
            for (KotoTaitotaso taitotaso : other.getTaitotasot()) {
                this.taitotasot.add(taitotaso);
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KotoOpinto) {
            KotoOpinto that = (KotoOpinto) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());
            result &= Objects.equals(getNimiKoodi(), that.getNimiKoodi());

            if (result && getTaitotasot() != null) {
                Iterator<KotoTaitotaso> i = getTaitotasot().iterator();
                Iterator<KotoTaitotaso> j = that.getTaitotasot().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }

        }
        return result;
    }

    private void copyState(KotoOpinto other) {
        if (other == null) {
            return;
        }

        this.nimiKoodi = other.getNimiKoodi();
        this.kuvaus = other.getKuvaus();
        this.taitotasot = other.getTaitotasot().stream().map(KotoTaitotaso::new).collect(Collectors.toList());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.koto_opinto;
    }
}
