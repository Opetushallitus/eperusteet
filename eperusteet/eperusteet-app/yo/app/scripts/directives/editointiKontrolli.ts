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

import * as angular from "angular";
import _ from "lodash";

angular.module("eperusteApp").directive("editointiKontrolli", function($rootScope, Editointikontrollit) {
    return {
        restrict: "A",
        link: function(scope, element: any, attrs: any) {
            Editointikontrollit.getEditModePromise().then(function(editMode) {
                if (!editMode) {
                    hideOrDisableElement();
                }
            });

            scope.$on("enableEditing", function() {
                showOrEnableElement();
            });
            scope.$on("disableEditing", function() {
                hideOrDisableElement();
            });

            function hideOrDisableElement() {
                if (element.is("input, textarea, button")) {
                    element.attr("disabled", "disabled");
                } else {
                    if (attrs.ngShow || attrs.ngHide) {
                        element.addClass("ng-hide");
                    } else {
                        element.hide();
                    }
                }
            }

            function shouldBeShown() {
                return (!attrs.ngShow || scope.$eval(attrs.ngShow)) && (!attrs.ngHide || !scope.$eval(attrs.ngHide));
            }

            function showOrEnableElement() {
                if (element.is("input, textarea, button")) {
                    if (!attrs.ngDisabled || !scope.$eval(attrs.ngDisabled)) {
                        element.removeAttr("disabled");
                    }
                } else {
                    if (shouldBeShown()) {
                        if (attrs.ngShow || attrs.ngHide) {
                            element.removeClass("ng-hide");
                        } else {
                            element.show();
                        }
                    }
                }
            }
        }
    };
});
