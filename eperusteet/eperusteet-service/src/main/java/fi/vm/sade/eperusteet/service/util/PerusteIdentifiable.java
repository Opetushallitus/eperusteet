package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.annotation.Identifiable;

public interface PerusteIdentifiable extends Identifiable {
    KoulutustyyppiToteutus getToteutus();
}
