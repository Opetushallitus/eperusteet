/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

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

/**
 * User: tommiratamaa
 * Date: 30.9.15
 * Time: 14.36
 */
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
