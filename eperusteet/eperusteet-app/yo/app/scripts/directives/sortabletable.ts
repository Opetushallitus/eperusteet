/*
 * Copyright (c) 2017 The Finnish Board of Education - Opetushallitus
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
.component("sortableTable", {
    templateUrl: "scripts/directives/sortabletable.html",
    bindings: {
        ngModel: "=",
        showIdx: "<",
        ngChange: "<", // Sallii muokkauksen jos määritelty
        isSorting: "=",
        nimeton: "<"
    },
    controllerAs: "vm",
    controller($state, Editointikontrollit, Notifikaatiot, Api) {
        const vm = this;

        vm.$onInit = () => {
            vm.ngModel = vm.ngModel || [];
            vm.showIdx = vm.showIdx || true;
            vm.isSorting = false;
            vm.allowSorting = _.isFunction(vm.ngChange);
            vm.hasMuokattu = !_.isEmpty(vm.ngModel) && !!_.first(vm.ngModel).muokattu;
        }

        vm.sortableOptions = {
            cursor: "move",
            cursorAt: { top : 2, left: 2 },
            handle: ".handle",
            delay: 100,
            tolerance: "pointer",
        };

        // let backup = Api.copy(vm.ngModel);

        vm.sort = () => {
            if (!vm.allowSorting) {
                return;
            }

            Editointikontrollit.registerCallback({
                async edit() {
                    // let backup = Api.copy(vm.ngModel);
                    vm.isSorting = true;
                },
                async save() {
                    vm.isSorting = false;
                    await vm.ngChange(vm.ngModel);
                    Notifikaatiot.onnistui("tallennus-onnistui");
                },
                cancel() {
                    vm.isSorting = false;
                    // vm.ngModel = Api.copy(backup);
                }
            });
            Editointikontrollit.startEditing();
        };

    }
});

