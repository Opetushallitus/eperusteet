package fi.vm.sade.eperusteet.dto.tutkinnonrakenne;

import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.VersionedDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import org.springframework.util.CollectionUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RakenneModuuliDto extends AbstractRakenneOsaDto implements VersionedDto {

    private LokalisoituTekstiDto nimi;
    private RakenneModuuliRooli rooli;
    private MuodostumisSaantoDto muodostumisSaanto;
    private OsaamisalaDto osaamisala;
    private KoodiDto tutkintonimike;
    private List<AbstractRakenneOsaDto> osat;
    private Integer versioId;

    @Override
    public Integer getVersioId() {
        return versioId;
    }

    @Override
    public void setVersionId(Integer id) {
        versioId = id;
    }

    @Override
    public String validationIdentifier() {
        if (rooli == RakenneModuuliRooli.TUTKINTONIMIKE && tutkintonimike != null) {
            return tutkintonimike.getUri();
        }
        else if (rooli == RakenneModuuliRooli.OSAAMISALA && osaamisala != null) {
            return osaamisala.getOsaamisalakoodiUri();
        }
        else {
            return "";
        }
    }

    @Override
    protected void foreach(final Visitor visitor, final int depth) {
        visitor.visit(this, depth);
        if (osat != null) {
            for (AbstractRakenneOsaDto dto : osat) {
                dto.foreach(visitor, depth + 1);
            }
        }
    }

    public LokalisoituTekstiDto getNimi() {
        if (osaamisala != null && osaamisala.getNimi() != null && !CollectionUtils.isEmpty(osaamisala.getNimi().getTekstit())) {
            return osaamisala.getNimi();
        }

        if (tutkintonimike != null && tutkintonimike.getNimi() != null && !CollectionUtils.isEmpty(tutkintonimike.getNimi().getTekstit())) {
            return tutkintonimike.getNimi();
        }

        return nimi;
    }

}
