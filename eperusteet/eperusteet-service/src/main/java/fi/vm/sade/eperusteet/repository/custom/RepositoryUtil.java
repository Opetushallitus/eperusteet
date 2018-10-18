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
package fi.vm.sade.eperusteet.repository.custom;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 *
 * @author jhyoty
 */
public final class RepositoryUtil {

    private RepositoryUtil() {
        //apuluokka
    }

    public static final char ESCAPE_CHAR = '\\';

    public static String kuten(String teksti) {
        if (teksti == null) {
            teksti = "";
        }
        return kutenCaseSensitive(teksti.toLowerCase());
    }

    public static String kutenCaseSensitive(String teksti) {
        if (teksti == null) {
            teksti = "";
        }
        StringBuilder b = new StringBuilder("%");
        b.append(teksti.replace("" + ESCAPE_CHAR, "" + ESCAPE_CHAR + ESCAPE_CHAR).replace("_", ESCAPE_CHAR
                + "_").replace("%", ESCAPE_CHAR + "%"));
        b.append("%");
        return b.toString();
    }

    public static Predicate and(CriteriaBuilder cb, List<Predicate> preds) {
        if (preds.size() == 1) {
            return preds.get(0);
        }
        else {
            Predicate result = preds.get(0);
            for (Predicate next : preds.subList(1, preds.size())) {
                result = cb.and(result, next);
            }
            return result;
        }
    }

}
