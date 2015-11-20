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

package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LukioAihekokonaisuudetLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: jsikio
 * Date: 14.10.15
 * Time: 09.38
 */
@RestController
@InternalApi
@RequestMapping(value = {
        LukioAihekokonaisuudetLockController.BASE + "/lukko",
})
public class LukioAihekokonaisuudetLockController extends AbstractLockController<LukioAihekokonaisuudetLockContext> {
    public static final String BASE = "/perusteet/{perusteId}/lukiokoulutus/aihekokonaisuudet";

    @Autowired
    @LockCtx(LukioAihekokonaisuudetLockContext.class)
    private LockService<LukioAihekokonaisuudetLockContext> service;

    @Override
    protected LockService<LukioAihekokonaisuudetLockContext> service() {
        return service;
    }
}
