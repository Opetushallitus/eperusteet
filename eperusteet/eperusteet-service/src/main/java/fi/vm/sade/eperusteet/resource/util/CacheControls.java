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
package fi.vm.sade.eperusteet.resource.util;

import fi.vm.sade.eperusteet.resource.util.CacheControl;

/**
 *
 * @author jhyoty
 */
final class CacheControls {

    private CacheControls() {
    }


    public static final String PRIVATE_NOT_CACHEABLE = "private,no-cache";
    public static final String PUBLIC_NOT_CACHEABLE = "no-cache";

    private static final String PRIVATE_CACHEABLE = "private,max-age=";
    private static final String PUBLIC_CACHEABLE = "max-age=";

    public static String buildCacheControl(CacheControl cc) {
        if (cc.nocache()) {
            if (cc.nonpublic()) {
                return PRIVATE_NOT_CACHEABLE;
            } else {
                return PUBLIC_NOT_CACHEABLE;
            }
        } else {
            if (cc.nonpublic()) {
                return PRIVATE_CACHEABLE + cc.age();
            } else {
                return PUBLIC_CACHEABLE + cc.age();
            }
        }

    }
}
