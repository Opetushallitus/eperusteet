package fi.vm.sade.eperusteet.domain.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taiteenala")
@JsonTypeName("taiteenala")
@Audited
@Getter
@Setter
public class Taiteenala extends PerusteenOsa implements Serializable {
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi koodi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen teksti;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale kasvatus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale yhteisetOpinnot;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale teemaopinnot;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale aikuistenOpetus;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale tyotavatOpetuksessa;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KevytTekstiKappale oppimisenArviointiOpetuksessa;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "taiteenala_vapaateksti",
            joinColumns = @JoinColumn(name = "taiteenala_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    public Taiteenala() {

    }

    public Taiteenala(Taiteenala other) {
        copyState(other);
    }

    @Override
    public Taiteenala copy() {
        return new Taiteenala(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof Taiteenala) {
            copyState((Taiteenala) perusteenOsa);
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa other) {
        return false;
    }

    private void copyState(Taiteenala other) {
        if (other == null) {
            return;
        }

        setKoodi(other.getKoodi());
        setTeksti(other.getTeksti());
        setAikuistenOpetus(KevytTekstiKappale.getCopy(other.getAikuistenOpetus()));
        setYhteisetOpinnot(KevytTekstiKappale.getCopy(other.getYhteisetOpinnot()));
        setTeemaopinnot(KevytTekstiKappale.getCopy(other.getTeemaopinnot()));
        setKasvatus(KevytTekstiKappale.getCopy(other.getKasvatus()));
        setTyotavatOpetuksessa(KevytTekstiKappale.getCopy(other.getTyotavatOpetuksessa()));
        setOppimisenArviointiOpetuksessa(KevytTekstiKappale.getCopy(other.getOppimisenArviointiOpetuksessa()));

        if (other.getVapaatTekstit() != null) {
            this.vapaatTekstit = new ArrayList<>();
            for (KevytTekstiKappale vapaaTeksti : other.getVapaatTekstit()) {
                this.vapaatTekstit.add(new KevytTekstiKappale(vapaaTeksti));
            }
        }
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.taiteenala;
    }
}
