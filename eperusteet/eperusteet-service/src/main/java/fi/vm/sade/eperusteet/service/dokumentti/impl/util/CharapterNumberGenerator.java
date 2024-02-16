package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import java.util.ArrayList;
import java.util.List;

/**
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
