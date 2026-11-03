package fi.vm.sade.eperusteet.domain.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.domain.vst.TavoiteAlue;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "taiteenalavaihe")
@JsonTypeName("taiteenalavaihe")
@Audited
@Getter
@Setter
public class TaiteenalaVaihe extends PerusteenOsa implements Serializable {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidKoodisto(koodisto = KoodistoUriArvo.TPO_VAIHE)
    private Koodi koodi;

    private Integer laajuus;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "taiteenalavaihe_tavoitealueet",
            joinColumns = @JoinColumn(name = "taiteenalavaihe_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoitealue_id"))
    private List<TavoiteAlue> tavoitealueet = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "taiteenalavaihe_vapaateksti",
            joinColumns = @JoinColumn(name = "taiteenalavaihe_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit = new ArrayList<>();

    public TaiteenalaVaihe() {

    }

    public TaiteenalaVaihe(TaiteenalaVaihe other) {
        super(other);
        copyState(other);
    }

    @Override
    public TaiteenalaVaihe copy() {
        return new TaiteenalaVaihe(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TaiteenalaVaihe other) {
            setKoodi(other.getKoodi());
            setLaajuus(other.getLaajuus());
            setKuvaus(other.getKuvaus());
            this.tavoitealueet = other.getTavoitealueet() != null ? new ArrayList<>(other.getTavoitealueet()) : new ArrayList<>();
            this.vapaatTekstit = other.getVapaatTekstit() != null ? new ArrayList<>(other.getVapaatTekstit()) : new ArrayList<>();
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof TaiteenalaVaihe that) {
            result = super.structureEquals(that);
            result &= Objects.equals(getKoodi(), that.getKoodi());
            result &= Objects.equals(getLaajuus(), that.getLaajuus());
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());

            if (result && getTavoitealueet() != null && that.getTavoitealueet() != null) {
                Iterator<TavoiteAlue> i = getTavoitealueet().iterator();
                Iterator<TavoiteAlue> j = that.getTavoitealueet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(TaiteenalaVaihe other) {
        if (other == null) {
            return;
        }

        this.koodi = other.getKoodi();
        this.laajuus = other.getLaajuus();
        this.kuvaus = other.getKuvaus();

        if (other.getTavoitealueet() != null) {
            this.tavoitealueet = other.getTavoitealueet().stream().map(TavoiteAlue::new).collect(Collectors.toList());
        }

        if (other.getVapaatTekstit() != null) {
            this.vapaatTekstit = other.getVapaatTekstit().stream().map(KevytTekstiKappale::new).collect(Collectors.toList());
        }
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.taiteenalavaihe;
    }
}
