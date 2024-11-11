package fi.vm.sade.eperusteet.service.tasks;

import fi.vm.sade.eperusteet.domain.KoodiRelaatioTyyppi;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.repository.SkeduloituajoRepository;
import fi.vm.sade.eperusteet.service.AmmattitaitovaatimusService;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.SkeduloituajoService;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TutkinnonosienAmmattiaitovaatimusKooditTask extends AbstractScheduledTask {

    @Autowired
    private SkeduloituajoRepository skeduloituajoRepository;

    @Autowired
    private SkeduloituajoService skeduloituajoService;

    @Autowired
    private KoodistoClient koodistoClient;

    @Autowired
    private AmmattitaitovaatimusService ammattitaitovaatimusService;

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public void executeTask(Date viimeisinajoaika) {
        koodistoClient.addKoodistoRelaatio(KoodistoUriArvo.TUTKINTONIMIKKEET, KoodistoUriArvo.TUTKINNONOSAT, KoodiRelaatioTyyppi.SISALTYY);
        koodistoClient.addKoodistoRelaatio(KoodistoUriArvo.KOULUTUS, KoodistoUriArvo.TUTKINNONOSAT, KoodiRelaatioTyyppi.SISALTYY);
        koodistoClient.addKoodistoRelaatio(KoodistoUriArvo.TUTKINNONOSAT, KoodistoUriArvo.AMMATTITAITOVAATIMUKSET, KoodiRelaatioTyyppi.SISALTYY);
        ammattitaitovaatimusService.lisaaAmmattitaitovaatimusTutkinnonosaKoodistoon(viimeisinajoaika);
    }
}
