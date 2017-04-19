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

angular.module("eperusteApp")
.component("epPagination", {
    template: '<ul uib-pagination ng-model="vm.ngModel" boundary-links="vm.boundaryLinks" ng-change="vm.valitseSivu(vm.valittuSivu)" total-items="vm.totalItems" items-per-page="vm.itemsPerPage" previous-text="«" next-text="»"></ul>',
    bindings: {
        totalItems: "<",
        itemsPerPage: "<",
        boundaryLinks: "<",
        ngModel: "=",
        ngChange: "<",
    },
    controllerAs: "vm",
    controller() {
        const vm = this;

        vm.valitseSivu = (sivu) => {
            if (_.isFunction(vm.ngChange)) {
                vm.ngChange(sivu);
            }
        };

        vm.$onInit = function() {
            vm.valittuSivu = vm.nykyinen;
            console.log(vm);
        }
    }
});
