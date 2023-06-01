package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "tutkinnonosa")
@JsonTypeName("tutkinnonosa")
@Audited
@Slf4j
public class TutkinnonOsa extends PerusteenOsa implements Serializable {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen tavoitteet;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Deprecated
    @Getter
    private TekstiPalanen ammattitaitovaatimukset;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private Ammattitaitovaatimukset2019 ammattitaitovaatimukset2019;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Deprecated
    private TekstiPalanen ammattitaidonOsoittamistavat;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private GeneerinenArviointiasteikko geneerinenArviointiasteikko;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private Koodi koodi;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "ammattitaitovaatimuksenkohdealue_tutkinnonosa",
            joinColumns = @JoinColumn(name = "tutkinnonosa_id"),
            inverseJoinColumns = @JoinColumn(name = "ammattitaitovaatimuksenkohdealue_id"))
    @Getter
    @Setter
    @OrderColumn(name = "jarjestys")
    @Deprecated
    private List<AmmattitaitovaatimuksenKohdealue> ammattitaitovaatimuksetLista = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Arviointi arviointi;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_tutkinnonosa_kevyttekstikappale",
               joinColumns = @JoinColumn(name = "tutkinnonosa_id"),
               inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_tutkinnonosa_osaalue",
               joinColumns = @JoinColumn(name = "tutkinnonosa_id"),
               inverseJoinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"))
    @OrderColumn
    // TUTKE2:n mukainen osa-alue
    private List<OsaAlue> osaAlueet;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    private ValmaTelmaSisalto valmaTelmaSisalto;

    @Getter
    @Enumerated(EnumType.STRING)
    @NotNull
    private TutkinnonOsaTyyppi tyyppi = TutkinnonOsaTyyppi.NORMAALI;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "alkuperainen_peruste_id")
    private Peruste alkuperainenPeruste;

    public TutkinnonOsa() {
    }

    public TutkinnonOsa(TutkinnonOsa other) {
        super(other);
        copyState(other);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public TutkinnonOsa copy() {
        return new TutkinnonOsa(this);
    }

    public TekstiPalanen getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(TekstiPalanen tavoitteet) {
        this.tavoitteet = tavoitteet;
    }


    public void setAmmattitaitovaatimukset(TekstiPalanen ammattitaitovaatimukset) {
        this.ammattitaitovaatimukset = ammattitaitovaatimukset;
    }

    public TekstiPalanen getAmmattitaidonOsoittamistavat() {
        return ammattitaidonOsoittamistavat;
    }

    public void setAmmattitaidonOsoittamistavat(TekstiPalanen ammattitaidonOsoittamistavat) {
        this.ammattitaidonOsoittamistavat = ammattitaidonOsoittamistavat;
    }

    public TekstiPalanen getKuvaus() {
        return kuvaus;
    }

    public void setKuvaus(TekstiPalanen kuvaus) {
        this.kuvaus = kuvaus;
    }

    public Arviointi getArviointi() {
        return arviointi;
    }

    public void setArviointi(Arviointi arviointi) {
        if ( Objects.equals(this.arviointi, arviointi) ) {
            return;
        }
        if ( arviointi == null || this.arviointi == null ) {
            this.arviointi = arviointi;
        } else {
            this.arviointi.mergeState(arviointi);
            this.muokattu();
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof TutkinnonOsa) {
            TutkinnonOsa that = (TutkinnonOsa) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());
            result &= Objects.equals(getTyyppi(), that.getTyyppi());
            result &= Objects.equals(getKoodi(), that.getKoodi());
            result &= refXnor(getTavoitteet(), that.getTavoitteet());
            result &= refXnor(getAmmattitaidonOsoittamistavat(), that.getAmmattitaidonOsoittamistavat());
            result &= refXnor(getAmmattitaitovaatimukset(), that.getAmmattitaitovaatimukset());
            result &= refXnor(getArviointi(), that.getArviointi());

            if (result && that.getAmmattitaitovaatimukset2019() != null && getAmmattitaitovaatimukset2019() != null) {
                result &= that.getAmmattitaitovaatimukset2019().structureEquals(that.getAmmattitaitovaatimukset2019());
            }

            if (result && getArviointi() != null) {
                result &= getArviointi().structureEquals(that.getArviointi());
            }

            log.debug("Tutkinnon osa update status: ", result);

            result &= refXnor(getOsaAlueet(), that.getOsaAlueet());
            if (result && getOsaAlueet() != null) {
                Iterator<OsaAlue> i = getOsaAlueet().iterator();
                Iterator<OsaAlue> j = that.getOsaAlueet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= i.next().structureEquals(j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }


            log.debug("Tutkinnon osa osa-alue update status: ", result);
        }
        return result;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof TutkinnonOsa) {
            TutkinnonOsa other = (TutkinnonOsa) perusteenOsa;
            this.setArviointi(other.getArviointi());
            this.setAmmattitaitovaatimukset(other.getAmmattitaitovaatimukset());
            this.setAmmattitaitovaatimukset2019(other.getAmmattitaitovaatimukset2019());
            this.setAmmattitaitovaatimuksetLista(connectAmmattitaitovaatimusListToTutkinnonOsa(other));
            this.setAmmattitaidonOsoittamistavat(other.getAmmattitaidonOsoittamistavat());
            this.setTavoitteet(other.getTavoitteet());
            this.setKoodi(other.getKoodi());
            this.setTyyppi(other.getTyyppi());
            this.setGeneerinenArviointiasteikko(other.getGeneerinenArviointiasteikko());
            this.setKuvaus(other.getKuvaus());
            this.setValmaTelmaSisalto(other.getValmaTelmaSisalto());
            this.setVapaatTekstit(other.getVapaatTekstit());
            if (other.getOsaAlueet() != null) {
                this.setOsaAlueet(mergeOsaAlueet(this.getOsaAlueet(), other.getOsaAlueet()));
            }
        }
    }

    private List<AmmattitaitovaatimuksenKohdealue> connectAmmattitaitovaatimusListToTutkinnonOsa(TutkinnonOsa other) {
        for (AmmattitaitovaatimuksenKohdealue ammattitaitovaatimuksenKohdealue : other.getAmmattitaitovaatimuksetLista()) {
            ammattitaitovaatimuksenKohdealue.connectAmmattitaitovaatimuksetToKohdealue(ammattitaitovaatimuksenKohdealue);
        }
        return other.getAmmattitaitovaatimuksetLista();
    }

    private void copyState(TutkinnonOsa other) {
        this.arviointi = other.getArviointi() == null ? null : new Arviointi(other.getArviointi());
        this.ammattitaitovaatimukset = other.getAmmattitaitovaatimukset();
        this.ammattitaitovaatimuksetLista = other.getAmmattitaitovaatimuksetLista().stream()
                .map(AmmattitaitovaatimuksenKohdealue::new)
                .collect(Collectors.toList());
        this.ammattitaidonOsoittamistavat = other.getAmmattitaidonOsoittamistavat();
        this.tavoitteet = other.getTavoitteet();
        this.koodi = other.getKoodi();
        this.tyyppi = other.getTyyppi();
        this.kuvaus = other.getKuvaus();
        if (other.getValmaTelmaSisalto() != null) {
            this.valmaTelmaSisalto = new ValmaTelmaSisalto(other.getValmaTelmaSisalto());
        }
        if (TutkinnonOsaTyyppi.isTutke(this.tyyppi) && other.getOsaAlueet() != null) {
            this.osaAlueet = new ArrayList<>();
            for (OsaAlue o : other.getOsaAlueet()) {
                this.osaAlueet.add(new OsaAlue(o));
            }
        }

        if (other.getAmmattitaitovaatimukset2019() != null) {
            this.ammattitaitovaatimukset2019 = new Ammattitaitovaatimukset2019(other.getAmmattitaitovaatimukset2019());
        }

        this.geneerinenArviointiasteikko = other.getGeneerinenArviointiasteikko();

        if (other.getVapaatTekstit() != null) {
            this.vapaatTekstit = new ArrayList<>();
            for (KevytTekstiKappale vapaaTeksti : other.getVapaatTekstit()) {
                this.vapaatTekstit.add(new KevytTekstiKappale(vapaaTeksti));
            }
        }
    }

    private List<OsaAlue> mergeOsaAlueet(List<OsaAlue> current, List<OsaAlue> other) {
        List<OsaAlue> tempList = new ArrayList<>();
        boolean loyty = false;
        if (other != null) {
            for (OsaAlue osaAlueOther : other) {
                for (OsaAlue osaAlueCurrent : current) {
                    if (osaAlueCurrent.getId().equals(osaAlueOther.getId())) {
                        // Jos tutkinnon osalla osa-aluelista mergessä, niin kyseessä on kevyempi
                        // osa-alue objekteja. Joten käytetään partialMergeStatea.
                        osaAlueCurrent.partialMergeState(osaAlueOther);
                        tempList.add(osaAlueCurrent);
                        loyty = true;
                    }
                }
                if (!loyty) {
                    tempList.add(osaAlueOther);
                }
                loyty = false;
            }
        }

        return tempList;
    }

    public void setTyyppi(TutkinnonOsaTyyppi tyyppi) {
        this.tyyppi = tyyppi == null ? TutkinnonOsaTyyppi.NORMAALI : tyyppi;
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.tutkinnonosa;
    }

    public void asetaAlkuperainenPeruste(Peruste peruste) {
        this.alkuperainenPeruste = peruste;
    }

    public List<Koodi> getKaikkiKoodit() {
        List<Koodi> koodit = new ArrayList<>();
        koodit.addAll(getAmmattitaitovaatimuksienKoodit());
        koodit.addAll(getOsaAlueidenKoodit());
        koodit.addAll(getOsaAlueidenOsaamistavoiteKoodit());

        return koodit;
    }

    private List<Koodi> getAmmattitaitovaatimuksienKoodit() {
        return Optional.ofNullable(Arrays.asList(ammattitaitovaatimukset2019)).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .flatMap(av -> {
                    List<Ammattitaitovaatimus2019> v = new ArrayList<>(av.getVaatimukset());
                    v.addAll(av.getKohdealueet().stream()
                            .map(Ammattitaitovaatimus2019Kohdealue::getVaatimukset)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                    return v.stream();
                })
                .map(Ammattitaitovaatimus2019::getKoodi)
                .collect(Collectors.toList());
    }

    private List<Koodi> getOsaAlueidenKoodit() {
        return Optional.ofNullable(getOsaAlueet()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(OsaAlue::getKoodi)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Koodi> getOsaAlueidenOsaamistavoiteKoodit() {
        return Optional.ofNullable(getOsaAlueet()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(OsaAlue::getAllOsaamistavoitteet)
                .flatMap(Collection::stream)
                .map(Osaamistavoite::getTavoitteet2020)
                .filter(Objects::nonNull)
                .flatMap(av -> {
                    List<Ammattitaitovaatimus2019> v = new ArrayList<>(av.getVaatimukset());
                    v.addAll(av.getKohdealueet().stream()
                            .map(Ammattitaitovaatimus2019Kohdealue::getVaatimukset)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                    return v.stream();
                })
                .map(Ammattitaitovaatimus2019::getKoodi)
                .collect(Collectors.toList());
    }

    public static boolean hasRequiredKielet(TekstiPalanen tutkinnonOsaNimi) {
        if (tutkinnonOsaNimi.getTeksti() == null) {
            return false;
        }

        Set<Kieli> pakollisetKielet = new HashSet<>(Arrays.asList(Kieli.FI, Kieli.SV));
        for (Kieli kieli : pakollisetKielet) {
            String osa = tutkinnonOsaNimi.getTeksti().get(kieli);
            if (osa == null || osa.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
