package fi.vm.sade.eperusteet.domain.arviointi;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "arvioinninkohdealue")
@Audited
public class ArvioinninKohdealue implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen otsikko;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "arvioinninkohdealue_arvioinninkohde",
               joinColumns = @JoinColumn(name = "arvioinninkohdealue_id"),
               inverseJoinColumns = @JoinColumn(name = "arvioinninkohde_id"))
    @OrderColumn
    @BatchSize(size = 10)
    private List<ArvioinninKohde> arvioinninKohteet = new ArrayList<>();

    // Käytetään reformin mukaisten perusteiden vanhojen arvioinnin kohdealueiden yhdistämiseen
    @Getter
    @ValidKoodisto(koodisto = KoodistoUriArvo.AMMATTITAITOVAATIMUKSET)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Koodi koodi;

    public ArvioinninKohdealue() {
    }

    public ArvioinninKohdealue(ArvioinninKohdealue other) {
        this.otsikko = other.getOtsikko();
        for (ArvioinninKohde ak : other.getArvioinninKohteet()) {
            this.arvioinninKohteet.add(new ArvioinninKohde(ak));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TekstiPalanen getOtsikko() {
        return otsikko;
    }

    public void setOtsikko(TekstiPalanen otsikko) {
        this.otsikko = otsikko;
    }

    public List<ArvioinninKohde> getArvioinninKohteet() {
        return arvioinninKohteet;
    }

    public void setArvioinninKohteet(List<ArvioinninKohde> arvioinninKohteet) {
        this.arvioinninKohteet.clear();
        if (arvioinninKohteet != null) {
            this.arvioinninKohteet.addAll(arvioinninKohteet);
        }
    }

    public void setKoodi(Koodi koodi) {
        if (this.koodi == null) {
            this.koodi = koodi;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.otsikko);
        hash = 71 * hash + Objects.hashCode(this.arvioinninKohteet);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ArvioinninKohdealue) {
            final ArvioinninKohdealue other = (ArvioinninKohdealue) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.otsikko, other.otsikko)) {
                return false;
            }
            return Objects.equals(this.arvioinninKohteet, other.arvioinninKohteet);
        }
        return false;
    }

    public boolean structureEquals(ArvioinninKohdealue other) {
        if (this == other) {
            return true;
        }
        boolean result = refXnor(getOtsikko(), other.getOtsikko());
        Iterator<ArvioinninKohde> i = getArvioinninKohteet().iterator();
        Iterator<ArvioinninKohde> j = other.getArvioinninKohteet().iterator();
        while (result && i.hasNext() && j.hasNext()) {
            result &= i.next().structureEquals(j.next());
        }
        result &= !i.hasNext();
        result &= !j.hasNext();
        return result;
    }

}
