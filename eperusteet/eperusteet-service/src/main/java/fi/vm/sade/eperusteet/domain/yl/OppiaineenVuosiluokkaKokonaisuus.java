package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import org.hibernate.envers.RelationTargetAuditMode;

import static fi.vm.sade.eperusteet.service.util.Util.identityEquals;

/**
 * Kuvaa oppimäärän yhteen vuosiluokkakokonaisuuteen osalta.
 */
@Entity
@Audited
@Table(name = "yl_oppiaineen_vlkok")
public class OppiaineenVuosiluokkaKokonaisuus extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(optional = false)
    @NotNull
    private Oppiaine oppiaine;

    @RelatesToPeruste
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private VuosiluokkaKokonaisuus vuosiluokkaKokonaisuus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa tyotavat;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa ohjaus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa arviointi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private TekstiOsa sisaltoalueinfo;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen opetuksenTavoitteetOtsikko;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "yl_oppiaineen_vlkok_yl_opetuksen_tavoite",
            joinColumns = @JoinColumn(name = "yl_oppiaineen_vlkok_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoitteet_id"))
    @OrderColumn
    private List<OpetuksenTavoite> tavoitteet = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "yl_oppiaineen_vlkok_yl_keskeinen_sisaltoalue",
            joinColumns = @JoinColumn(name = "yl_oppiaineen_vlkok_id"),
            inverseJoinColumns = @JoinColumn(name = "sisaltoalueet_id"))
    @OrderColumn
    private List<KeskeinenSisaltoalue> sisaltoalueet = new ArrayList<>();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen vapaaTeksti;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_oppiaineen_vlkok_vapaateksti",
            joinColumns = @JoinColumn(name = "oppiaine_vlk_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    public List<OpetuksenTavoite> getTavoitteet() {
        return new ArrayList<>(tavoitteet);
    }

    public void addOpetuksenTavoite(OpetuksenTavoite tavoite) {
        tavoitteet.add(tavoite);
    }

    public void setTavoitteet(List<OpetuksenTavoite> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            this.tavoitteet.addAll(tavoitteet);
        }
    }

    public void addSisaltoalue(KeskeinenSisaltoalue sisalto) {
        sisaltoalueet.add(sisalto);
    }

    public List<KeskeinenSisaltoalue> getSisaltoalueet() {
        return new ArrayList<>(sisaltoalueet);
    }

    public void setSisaltoalueet(List<KeskeinenSisaltoalue> sisaltoalueet) {
        this.sisaltoalueet.clear();
        if (sisaltoalueet != null) {
            this.sisaltoalueet.addAll(sisaltoalueet);
        }
    }

    public boolean structureEquals(OppiaineenVuosiluokkaKokonaisuus other) {
        boolean result = identityEquals(this.getOppiaine(), other.getOppiaine());
        result &= identityEquals(this.getVuosiluokkaKokonaisuus(), other.getVuosiluokkaKokonaisuus());
        //result &= refXnor(this.getArviointi(), other.getArviointi());
        //result &= refXnor(this.getOhjaus(), other.getOhjaus());
        //result &= refXnor(this.getSisaltoalueinfo(), other.getSisaltoalueinfo());
        //result &= refXnor(this.getTehtava(), other.getTehtava());
        //result &= refXnor(this.getTyotavat(), other.getTyotavat());
        result &= this.getSisaltoalueet().size() == other.getSisaltoalueet().size();
        result &= this.getTavoitteet().size() == other.getTavoitteet().size();

        if (result) {
            Iterator<KeskeinenSisaltoalue> i = this.getSisaltoalueet().iterator();
            Iterator<KeskeinenSisaltoalue> j = other.getSisaltoalueet().iterator();
            while (result && i.hasNext() && j.hasNext()) {
                result &= identityEquals(i.next(), j.next());
            }
        }

        if (result) {
            Iterator<OpetuksenTavoite> i = this.getTavoitteet().iterator();
            Iterator<OpetuksenTavoite> j = other.getTavoitteet().iterator();
            while (result && i.hasNext() && j.hasNext()) {
                result &= identityEquals(i.next(), j.next());
            }
        }

        return result;
    }

    public OppiaineenVuosiluokkaKokonaisuus kloonaa(
            Map<VuosiluokkaKokonaisuus, VuosiluokkaKokonaisuus> vuosiluokkaKokonaisuusMapper,
            Map<LaajaalainenOsaaminen, LaajaalainenOsaaminen> laajainenOsaaminenMapper,
            Map<OpetuksenKohdealue, OpetuksenKohdealue> kohdealueMapper) {
        OppiaineenVuosiluokkaKokonaisuus ovlk = new OppiaineenVuosiluokkaKokonaisuus();
        ovlk.setArviointi(arviointi);
        ovlk.setOhjaus(ohjaus);
        ovlk.setTehtava(tehtava);
        ovlk.setTyotavat(tyotavat);
        ovlk.setSisaltoalueinfo(sisaltoalueinfo);

        ovlk.setVuosiluokkaKokonaisuus(vuosiluokkaKokonaisuusMapper.get(vuosiluokkaKokonaisuus));

        Map<KeskeinenSisaltoalue, KeskeinenSisaltoalue> keskeinenSisaltoalueMapper = new HashMap<>();
        for (KeskeinenSisaltoalue sisalto : sisaltoalueet) {
            KeskeinenSisaltoalue klooni = sisalto.kloonaa();
            keskeinenSisaltoalueMapper.put(sisalto, klooni);
            ovlk.addSisaltoalue(klooni);
        }

        for (OpetuksenTavoite tavoite : tavoitteet) {
            OpetuksenTavoite klooni = tavoite.kloonaa(keskeinenSisaltoalueMapper, laajainenOsaaminenMapper, kohdealueMapper);
            ovlk.addOpetuksenTavoite(klooni);
        }

        ovlk.setVapaaTeksti(vapaaTeksti);

        return ovlk;
    }

    public void setVapaatTekstit(List<KevytTekstiKappale> vapaatTekstit) {
        if (this.vapaatTekstit == null) {
            this.vapaatTekstit = new ArrayList<>();
        }
        this.vapaatTekstit.clear();
        this.vapaatTekstit.addAll(vapaatTekstit);
    }

}
