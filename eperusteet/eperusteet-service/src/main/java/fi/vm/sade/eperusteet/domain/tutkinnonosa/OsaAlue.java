package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "tutkinnonosa_osaalue")
@Audited
public class OsaAlue implements Serializable, PartialMergeable<OsaAlue>, HistoriaTapahtuma {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private OsaAlueTyyppi tyyppi;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private GeneerinenArviointiasteikko geneerinenArviointiasteikko;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Osaamistavoite pakollisetOsaamistavoitteet;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Osaamistavoite valinnaisetOsaamistavoitteet;

    // Ei käytössä Valma/Telma perusteissa
    @Deprecated
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "tutkinnonosa_osaalue_osaamistavoite",
            joinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"),
            inverseJoinColumns = @JoinColumn(name = "osaamistavoite_id"))
    @OrderColumn
    private List<Osaamistavoite> osaamistavoitteet;

    @Getter
    @Setter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tutkinnonosa_tutkinnonosa_osaalue",
            inverseJoinColumns = @JoinColumn(name = "tutkinnonosa_id"),
            joinColumns = @JoinColumn(name = "tutkinnonosa_osaalue_id"))
    private Set<TutkinnonOsa> tutkinnonOsat;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    private ValmaTelmaSisalto valmaTelmaSisalto;

    /**
     * Jos osa-alueesta on vain yksi kieliversio, määritellään se tässä.
     */
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ValidKoodisto(koodisto = KoodistoUriArvo.KIELIVALIKOIMA)
    private Koodi kielikoodi;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ValidKoodisto(koodisto = KoodistoUriArvo.AMMATILLISENOPPIAINEET)
    private Koodi koodi;

    public OsaAlue() {
    }

    public OsaAlue(OsaAlue o) {
        if (o == null) {
            return;
        }

        this.nimi = o.nimi;
        this.kuvaus = o.kuvaus;
        this.osaamistavoitteet = new ArrayList<>();
        this.valmaTelmaSisalto = null;
        this.koodi = o.koodi;
        this.kielikoodi = o.kielikoodi;
        this.tyyppi = o.tyyppi;
        this.pakollisetOsaamistavoitteet = new Osaamistavoite(o.getPakollisetOsaamistavoitteet());
        this.valinnaisetOsaamistavoitteet = new Osaamistavoite(o.getValinnaisetOsaamistavoitteet());
        this.geneerinenArviointiasteikko = o.getGeneerinenArviointiasteikko();

        IdentityHashMap<Osaamistavoite, Osaamistavoite> identityMap = new IdentityHashMap<>();
        if (CollectionUtils.isNotEmpty(o.getOsaamistavoitteet())) {
            for (Osaamistavoite ot : o.getOsaamistavoitteet()) {
                if (identityMap.containsKey(ot)) {
                    this.osaamistavoitteet.add(identityMap.get(ot));
                } else {
                    Osaamistavoite t = new Osaamistavoite(ot, identityMap);
                    identityMap.put(ot, t);
                    this.osaamistavoitteet.add(t);
                }
            }
        }
    }

    public Osaamistavoite getValinnaisetOsaamistavoitteet() {
        if (OsaAlueTyyppi.OSAALUE2020.equals(this.tyyppi)) {
            return valinnaisetOsaamistavoitteet;
        }
        return null;
    }

    public Osaamistavoite getPakollisetOsaamistavoitteet() {
        if (OsaAlueTyyppi.OSAALUE2020.equals(this.tyyppi)) {
            return pakollisetOsaamistavoitteet;
        }
        return null;
    }

    public List<Osaamistavoite> getOsaamistavoitteet() {
        if (this.tyyppi == null || OsaAlueTyyppi.OSAALUE2014.equals(this.tyyppi)) {
            return osaamistavoitteet;
        }
        return null;
    }

    public List<Osaamistavoite> getAllOsaamistavoitteet() {
        List<Osaamistavoite> tavoitteet = new ArrayList<>();
        tavoitteet.addAll(Optional.ofNullable(getOsaamistavoitteet()).orElse(Collections.emptyList()));
        tavoitteet.add(getPakollisetOsaamistavoitteet());
        tavoitteet.add(getValinnaisetOsaamistavoitteet());

        return tavoitteet.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void setOsaamistavoitteet(List<Osaamistavoite> osaamistavoitteet) {
        if (this.osaamistavoitteet == null) {
            this.osaamistavoitteet = new ArrayList<>();
        }
        this.osaamistavoitteet.clear();
        if (osaamistavoitteet != null) {
            this.osaamistavoitteet.addAll(osaamistavoitteet);
        }
    }

    @Override
    public void mergeState(OsaAlue updated) {
        if (updated != null) {
            this.setNimi(updated.getNimi());
            this.setKuvaus(updated.getKuvaus());
            this.setTyyppi(updated.getTyyppi());
            this.koodi = updated.getKoodi();
            this.setGeneerinenArviointiasteikko(updated.getGeneerinenArviointiasteikko());
            this.setPakollisetOsaamistavoitteet(updated.getPakollisetOsaamistavoitteet());
            this.setValinnaisetOsaamistavoitteet(updated.getValinnaisetOsaamistavoitteet());
            this.setKielikoodi(updated.kielikoodi);

            if (updated.getOsaamistavoitteet() != null) {
                this.setOsaamistavoitteet(mergeOsaamistavoitteet(this.getOsaamistavoitteet(), updated.getOsaamistavoitteet()));
            }
        }
    }

    @Override
    public void partialMergeState(OsaAlue updated) {
        if (updated != null) {
            this.setNimi(updated.getNimi());
            this.setKuvaus(updated.getKuvaus());
        }
    }

    public boolean structureEquals(OsaAlue other) {
        boolean result = refXnor(getNimi(), other.getNimi());
        result &= refXnor(getKuvaus(), other.getKuvaus());
        if ( result && getOsaamistavoitteet() != null && other.getOsaamistavoitteet() != null ) {
            Iterator<Osaamistavoite> i = getOsaamistavoitteet().iterator();
            Iterator<Osaamistavoite> j = other.getOsaamistavoitteet().iterator();
            while (result && i.hasNext() && j.hasNext()) {
                result &= i.next().structureEquals(j.next());
            }
            result &= !i.hasNext();
            result &= !j.hasNext();
        }
        return result;
    }

    private List<Osaamistavoite> mergeOsaamistavoitteet(List<Osaamistavoite> current, List<Osaamistavoite> updated) {
        List<Osaamistavoite> tempList = new ArrayList<>();
        boolean loyty = false;
        if (updated != null) {
            for (Osaamistavoite osaamistavoiteUpdate : updated) {
                for (Osaamistavoite osaamistavoiteCurrent : current) {
                    if (osaamistavoiteCurrent.getId().equals(osaamistavoiteUpdate.getId())) {
                        // Jos osa-alueella osaamistavoitelista mergessä, niin kyseessä on kevyempi
                        // osaamistavoite objekteja. Joten käytetään partialMergeStatea.
                        //osaamistavoiteCurrent.partialMergeState(osaamistavoiteUpdate);
                        osaamistavoiteCurrent.mergeState(osaamistavoiteUpdate);
                        tempList.add(osaamistavoiteCurrent);
                        loyty = true;
                    }
                }
                if (!loyty) {
                    tempList.add(osaamistavoiteUpdate);
                }
                loyty = false;
            }
        }
        return tempList;
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.osaalue;
    }

    public List<Koodi> getKaikkiKoodit() {
        return getAllOsaamistavoitteet().stream()
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
}
