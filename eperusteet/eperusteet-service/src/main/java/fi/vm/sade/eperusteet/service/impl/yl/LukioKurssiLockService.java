package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.yl.Kurssi;
import fi.vm.sade.eperusteet.repository.LukiokurssiRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.Suoritustavalle;
import fi.vm.sade.eperusteet.service.yl.KurssiLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.domain.yl.lukio.Lukiokurssi.inPeruste;
import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

@Service
@Suoritustavalle(Suoritustapakoodi.LUKIOKOULUTUS)
@LockCtx(KurssiLockContext.class)
public class LukioKurssiLockService extends AbstractKurssiLockService {

    @Autowired
    private LukiokurssiRepository lukiokurssiRepository;

    @Override
    protected Kurssi getKurssi(KurssiLockContext ctx) {
        return found(lukiokurssiRepository.getOne(ctx.getKurssiId()), inPeruste(ctx.getPerusteId()));
    }

    @Override
    protected int latestRevision(KurssiLockContext ctx) {
        return lukiokurssiRepository.getLatestRevisionId(ctx.getKurssiId()).getNumero();
    }
}
