package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ValidatorUtil {

    public ValidatorUtil() {
    }

    public static boolean hasValidTutkintonimikkeet(Peruste peruste, List<TutkintonimikeKoodiDto> tutkintonimikeKoodiDtos) {
        for (TutkintonimikeKoodiDto tutkintonimike : tutkintonimikeKoodiDtos) {
            boolean hasTutkintonimikkeetPerusteenKielilla = peruste.getKielet().stream()
                    .allMatch(kieli -> tutkintonimike.getNimi() != null && StringUtils.isNotEmpty(tutkintonimike.getNimi().get(kieli)));
            if (!hasTutkintonimikkeetPerusteenKielilla) {
                return false;
            }
        }
        return true;
    }
}
