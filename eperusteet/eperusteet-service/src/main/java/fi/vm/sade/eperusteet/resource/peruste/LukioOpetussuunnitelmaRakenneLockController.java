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
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LukioOpetussuunnitelmaRakenneLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: tommiratamaa
 * Date: 12.10.15
 * Time: 15.42
 */
@RestController
@InternalApi
@RequestMapping(value = {
        LukioOpetussuunnitelmaRakenneLockController.BASE + "/lukko",
})
public class LukioOpetussuunnitelmaRakenneLockController extends AbstractLockController<LukioOpetussuunnitelmaRakenneLockContext> {
    public static final String BASE = "/api/perusteet/{perusteId}/lukiokoulutus/rakenne";

    @Autowired
    @LockCtx(LukioOpetussuunnitelmaRakenneLockContext.class)
    private LockService<LukioOpetussuunnitelmaRakenneLockContext> service;

    @Override
    protected LockService<LukioOpetussuunnitelmaRakenneLockContext> service() {
        return service;
    }
}
