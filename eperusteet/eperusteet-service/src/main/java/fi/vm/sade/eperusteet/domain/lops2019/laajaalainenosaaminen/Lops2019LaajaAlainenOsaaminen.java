package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidKoodisto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_laaja_alainen_osaaminen")
public class Lops2019LaajaAlainenOsaaminen extends AbstractAuditedReferenceableEntity implements Copyable<Lops2019LaajaAlainenOsaaminen> {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ValidKoodisto(koodisto = KoodistoUriArvo.LAAJAALAINENOSAAMINENLOPS2021)
    private Koodi koodi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    private Integer jarjestys;

    public boolean structureEquals(Lops2019LaajaAlainenOsaaminen other) {
        boolean result = Objects.equals(this.getId(), other.getId());

        result &= refXnor(this.getNimi(), other.getNimi());
        result &= Objects.equals(this.getKoodi(), other.getKoodi());
        result &= refXnor(this.getKuvaus(), other.getKuvaus());

        return result;
    }

    @Override
    public Lops2019LaajaAlainenOsaaminen copy(boolean deep) {
        Lops2019LaajaAlainenOsaaminen result = new Lops2019LaajaAlainenOsaaminen();
        result.setKuvaus(this.getKuvaus());
        result.setNimi(this.getNimi());
        result.setKoodi(this.getKoodi());
        result.setJarjestys(this.getJarjestys());
        return result;
    }
}
