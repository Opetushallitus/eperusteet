package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.Sortable;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.GenericAlgorithms;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class GenericAlgorithmsTest {

    @Getter
    @Setter
    @AllArgsConstructor
    private static class A implements Sortable {
        Long id;
        Integer jarjestys;
    }

    @Test
    public void testSorting() {
        List<A> data = Arrays.asList(
                new A(0L, 0),
                new A(1L, 1),
                new A(2L, 2));

        assertThat(GenericAlgorithms.sort(Arrays.asList(
                new A(0L, 2),
                new A(1L, 0),
                new A(2L, 1)), data))
                .extracting("id", "jarjestys")
                .containsExactly(
                        tuple(1L, 0),
                        tuple(2L, 1),
                        tuple(0L, 2));

        // Virheelliset indeksit
        assertThat(GenericAlgorithms.sort(Arrays.asList(
                new A(0L, 15),
                new A(1L, 0),
                new A(2L, 7)), data))
                .extracting("id", "jarjestys")
                .containsExactly(
                        tuple(1L, 0),
                        tuple(2L, 1),
                        tuple(0L, 2));
    }

    @Test
    public void testFailedSorting() {
        List<A> data = Arrays.asList(
                new A(0L, 0),
                new A(1L, 1),
                new A(2L, 2));

        { // Puuttuva
            assertThatThrownBy(() -> GenericAlgorithms.sort(Arrays.asList(
                    new A(0L, 2),
                    new A(2L, 1)), data))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }

        { // Duplikaatti
            assertThatThrownBy(() -> GenericAlgorithms.sort(Arrays.asList(
                    new A(0L, 0),
                    new A(1L, 2),
                    new A(1L, 3),
                    new A(2L, 1)), data))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

}
