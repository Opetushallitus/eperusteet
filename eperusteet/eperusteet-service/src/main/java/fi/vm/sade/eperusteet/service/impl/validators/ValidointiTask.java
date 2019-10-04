/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.impl.AbstractScheduledTask;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ValidointiTask extends AbstractScheduledTask {
    private static AtomicBoolean isUpdating = new AtomicBoolean(false);

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Override
    public int getPriority() {
        return 3000;
    }

    @Override
    public void executeTask(Date viimeisinAjoaika) {
        if (!isUpdating.get()) {
            if (isUpdating.compareAndSet(false, true)) {
                try {
                    perusteprojektiService.validoiPerusteetTask(50);
                }
                finally {
                    isUpdating.set(false);
                }
            }
        }
    }
}
