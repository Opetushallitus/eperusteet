package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.Sortable;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericAlgorithms {

    static public <T extends Sortable, R extends Sortable> List<R> sort(List<T> order, List<R> originalList) {
        Map<Long, R> data = originalList.stream()
                .collect(Collectors.toMap(Sortable::getId, Function.identity()));
        List<R> result = order.stream()
                .sorted(Comparator.comparing(Sortable::getJarjestys))
                .map(Sortable::getId)
                .map(data::get)
                .collect(Collectors.toList());
        Set<Long> uniikit = result.stream()
                .map(Sortable::getId)
                .collect(Collectors.toSet());
        if (result.size() != data.size() || !uniikit.containsAll(data.keySet())) {
            throw new BusinessRuleViolationException("jarjestettava-ei-vastaa-alkuperaista");
        }
        for (int idx = 0; idx < result.size(); ++idx) {
            result.get(idx).setJarjestys(idx);
        }
        return result;
    }
}
