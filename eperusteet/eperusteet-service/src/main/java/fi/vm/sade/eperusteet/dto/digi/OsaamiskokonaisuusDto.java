package fi.vm.sade.eperusteet.dto.digi;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("osaamiskokonaisuus")
public class OsaamiskokonaisuusDto extends PerusteenOsaDto.Laaja {
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto keskeinenKasitteisto;
    private List<OsaamiskokonaisuusKasitteistoDto> kasitteistot;

    @Override
    public String getOsanTyyppi() {
        return "osaamiskokonaisuus";
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.osaamiskokonaisuus;
    }
}
