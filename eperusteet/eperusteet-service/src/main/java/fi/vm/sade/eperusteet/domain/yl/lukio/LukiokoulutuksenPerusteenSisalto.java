package fi.vm.sade.eperusteet.domain.yl.lukio;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.PerusteenSisalto;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Audited
@Table(name = "yl_lukiokoulutuksen_perusteen_sisalto", schema = "public")
public class LukiokoulutuksenPerusteenSisalto extends AbstractOppiaineOpetuksenSisalto implements PerusteenSisalto {

    @RelatesToPeruste
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @NotNull
    @JoinColumn(name = "peruste_id", nullable = false, updatable = false, unique = true)
    private Peruste peruste;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @Getter
    @Setter
    @JoinColumn(name="sisalto_id")
    private PerusteenOsaViite sisalto = new PerusteenOsaViite(this);

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "sisalto", cascade = CascadeType.PERSIST)
    private LukioOpetussuunnitelmaRakenne opetussuunnitelma = new LukioOpetussuunnitelmaRakenne();

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="aihekokonaisuudet_id")
    private Aihekokonaisuudet aihekokonaisuudet;


    @Getter
    @Setter
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="opetuksen_yleiset_tavoitteet_id")
    private OpetuksenYleisetTavoitteet opetuksenYleisetTavoitteet;

    public LukiokoulutuksenPerusteenSisalto kloonaa(Peruste peruste) {
        LukiokoulutuksenPerusteenSisalto kopio = new LukiokoulutuksenPerusteenSisalto();
        kopio.peruste = peruste;
        kopio.sisalto = this.sisalto.copy();
        kopio.opetussuunnitelma = this.opetussuunnitelma.kloonaa(kopio);
        kopio.aihekokonaisuudet =  this.aihekokonaisuudet.kloonaa();
        kopio.aihekokonaisuudet.setSisalto(kopio);
        kopio.opetuksenYleisetTavoitteet = this.opetuksenYleisetTavoitteet.kloonaa();
        kopio.opetuksenYleisetTavoitteet.setSisalto(kopio);
        return kopio;
    }

    @Override
    public Set<Oppiaine> getOppiaineet() {
        return opetussuunnitelma.getOppiaineet();
    }
}
