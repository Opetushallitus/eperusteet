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

angular.module('eperusteApp')
.directive('hallintalinkki', (Profiili, $window, $stateParams) => {
    return {
        template: '<a ui-sref="root.admin.perusteprojektit(stateParams)" icon-role="settings" kaanna="\'hallinta\'"></a>',
        restrict: 'E',
        link: (scope: any, element: any) => {
            scope.stateParams = $stateParams;
            $window.location.host.indexOf('localhost') === 0 ? element.show() : element.hide();
            scope.$on('fetched:casTiedot', () => {
                if (_.contains(Profiili.groups(), 'APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001')) {
                    element.show();
                }
            });
        }
    };
});
