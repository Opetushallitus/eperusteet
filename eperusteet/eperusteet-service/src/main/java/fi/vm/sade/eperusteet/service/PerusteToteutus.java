package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;

import java.util.Set;

public interface PerusteToteutus {
    Set<KoulutustyyppiToteutus> getTyypit();
    Class getImpl();
}
