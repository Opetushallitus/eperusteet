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
package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author isaul
 * Apuluokka sisällysluettelon luomiseen
 */
public class CharapterNumberGenerator {
    private List<Integer> numbers = new ArrayList<>();

    public CharapterNumberGenerator() {
        numbers.add(1);
    }

    public int getDepth() {
        return numbers.size();
    }

    public void increaseDepth() {
        numbers.add(1);
    }

    public void decreaseDepth() {
        if (!numbers.isEmpty())
            numbers.remove(numbers.size() - 1);
    }

    public void increaseNumber() {
        if (!numbers.isEmpty()) {
            int last = numbers.size() - 1;
            numbers.set(last, numbers.get(last) + 1);
        }
    }

    public String generateNumber() {
        String numberString = "";

        for (Integer number : numbers) {
            numberString += String.valueOf(number) + ".";
        }

        return numberString;
    }
}
