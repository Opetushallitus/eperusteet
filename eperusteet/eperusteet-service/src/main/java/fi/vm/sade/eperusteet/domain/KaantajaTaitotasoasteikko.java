package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
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
@Table(name = "kaantaja_taitotasoasteikko")
@Audited
@Getter
@Setter
public class KaantajaTaitotasoasteikko extends PerusteenOsa {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaTaitotasoasteikko", orphanRemoval = true)
    private List<TaitotasoasteikkoKategoria> taitotasoasteikkoKategoriat = new ArrayList<>();

    public KaantajaTaitotasoasteikko() {
    }

    public KaantajaTaitotasoasteikko(KaantajaTaitotasoasteikko other) {
        super(other);
        copyState(other);
    }

    @Override
    public KaantajaTaitotasoasteikko copy() {
        return new KaantajaTaitotasoasteikko(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajataitotasoasteikko;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KaantajaTaitotasoasteikko) {
            KaantajaTaitotasoasteikko other = (KaantajaTaitotasoasteikko) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());

            this.taitotasoasteikkoKategoriat.clear();
            for (TaitotasoasteikkoKategoria kategoria : other.getTaitotasoasteikkoKategoriat()) {
                TaitotasoasteikkoKategoria newKategoria = new TaitotasoasteikkoKategoria(kategoria);
                newKategoria.setKaantajaTaitotasoasteikko(this);
                this.taitotasoasteikkoKategoriat.add(newKategoria);
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KaantajaTaitotasoasteikko) {
            KaantajaTaitotasoasteikko that = (KaantajaTaitotasoasteikko) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());

            if (result && getTaitotasoasteikkoKategoriat() != null) {
                Iterator<TaitotasoasteikkoKategoria> i = getTaitotasoasteikkoKategoriat().iterator();
                Iterator<TaitotasoasteikkoKategoria> j = that.getTaitotasoasteikkoKategoriat().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(KaantajaTaitotasoasteikko other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        this.taitotasoasteikkoKategoriat = other.getTaitotasoasteikkoKategoriat().stream()
                .map(TaitotasoasteikkoKategoria::new)
                .peek(kategoria -> kategoria.setKaantajaTaitotasoasteikko(this))
                .collect(Collectors.toList());
    }
}

