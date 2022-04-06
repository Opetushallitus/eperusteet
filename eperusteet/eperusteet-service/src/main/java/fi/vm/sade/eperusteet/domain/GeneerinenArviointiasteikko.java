package fi.vm.sade.eperusteet.domain;


import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Audited
@Table(name = "geneerinenarviointiasteikko")
@Entity
public class GeneerinenArviointiasteikko extends AbstractAuditedReferenceableEntity implements Copyable<GeneerinenArviointiasteikko> {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = { Peruste.Valmis.class, Peruste.ValmisPohja.class, Peruste.ValmisPohja.class })
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(groups = { Peruste.Valmis.class, Peruste.ValmisPohja.class, Peruste.ValmisPohja.class })
    private TekstiPalanen kohde;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ArviointiAsteikko arviointiAsteikko;

    @OneToMany(cascade = CascadeType.ALL)
    @Getter
    @Setter
    private Set<GeneerisenOsaamistasonKriteeri> osaamistasonKriteerit = new HashSet<>();

    @Getter
    @Setter
    private boolean julkaistu;

    @Getter
    @Setter
    private boolean valittavissa = true;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    @CollectionTable(name = "geneerinenarviointiasteikko_koulutustyyppi")
    @Column(name = "koulutustyyppi")
    private Set<KoulutusTyyppi> koulutustyypit;

    @Override
    public GeneerinenArviointiasteikko copy(boolean deep) {
        GeneerinenArviointiasteikko uusi = new GeneerinenArviointiasteikko();
        uusi.setJulkaistu(false);
        uusi.setValittavissa(true);
        uusi.setArviointiAsteikko(this.arviointiAsteikko);
        uusi.setNimi(this.getNimi());
        uusi.setKohde(this.getKohde());
        uusi.setOsaamistasonKriteerit(this.getOsaamistasonKriteerit().stream()
                .map(GeneerisenOsaamistasonKriteeri::copy)
                .collect(Collectors.toSet()));
        return uusi;
    }

    public boolean structureEquals(GeneerinenArviointiasteikko updated) {
        boolean result = Objects.equals(getArviointiAsteikko().getId(), updated.getArviointiAsteikko().getId());

        if (result && getOsaamistasonKriteerit() != null) {
            Iterator<GeneerisenOsaamistasonKriteeri> alkup_osKriteerit = getOsaamistasonKriteerit()
                    .stream().sorted(Comparator.comparingLong(gok -> gok.getOsaamistaso().getId()))
                    .iterator();
            Iterator<GeneerisenOsaamistasonKriteeri> up_osKriteerit = updated.getOsaamistasonKriteerit()
                    .stream().sorted(Comparator.comparingLong(gok -> gok.getOsaamistaso().getId()))
                    .iterator();
            while (result && alkup_osKriteerit.hasNext() && up_osKriteerit.hasNext()) {

                GeneerisenOsaamistasonKriteeri alkup_osKriteeri = alkup_osKriteerit.next();
                GeneerisenOsaamistasonKriteeri up_osKriteeri = up_osKriteerit.next();

                result &= Objects.equals(alkup_osKriteeri.getOsaamistaso().getId(), up_osKriteeri.getOsaamistaso().getId());

                Iterator<TekstiPalanen> alkup_kriteerit = alkup_osKriteeri.getKriteerit().iterator();
                Iterator<TekstiPalanen> up_kriteerit = up_osKriteeri.getKriteerit().iterator();
                while (result && alkup_kriteerit.hasNext() && up_kriteerit.hasNext()) {
                    result &= Objects.equals(alkup_kriteerit.next().getTunniste(), up_kriteerit.next().getTunniste());
                }

                result &= !alkup_kriteerit.hasNext();
                result &= !up_kriteerit.hasNext();
            }
            result &= !alkup_osKriteerit.hasNext();
            result &= !up_osKriteerit.hasNext();
        }

        return result;
    }
}
