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

angular.module("eperusteApp").directive("hallintalinkki", [
    "Profiili",
    "$window",
    function(Profiili, $window) {
        return {
            template:
                '<a ng-cloak ui-sref="root.admin.perusteprojektit({lang: \'fi\'})" icon-role="settings" kaanna="hallinta"></a>',
            restrict: "E",
            link: function postLink(scope: any, element: any) {
                if ($window.location.host.indexOf("localhost") === 0) {
                    element.show();
                } else {
                    element.hide();
                }

                scope.$on("fetched:casTiedot", function() {
                    if (_.contains(Profiili.groups(), "APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001")) {
                        element.show();
                    }
                });
            }
        };
    }
]);
