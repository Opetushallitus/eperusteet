package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KVLiiteDokumentti extends DokumenttiBase {
    KVLiiteJulkinenDto kvLiiteJulkinenDto;
}
