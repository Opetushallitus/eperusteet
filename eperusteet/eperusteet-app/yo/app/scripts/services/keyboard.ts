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
import * as _ from "lodash";

angular.module("eperusteApp")
    .service("KeyboardListener", function($window, Editointikontrollit) {
    this.init = angular.noop;

    function clickRoleButton(role) {
        // Finds the first visible button with given role and clicks it
        angular
            .element("*[icon-role=" + role + "]:visible")
            .closest("button")
            .eq(0)
            .click();
    }

    function handleSingleKeys(event) {
        switch (event.which) {
            case 46:
                // Delete
                clickRoleButton("remove");
                return;
            default:
                break;
        }

        var char = String.fromCharCode(event.which).toLowerCase();
        switch (char) {
            case "m":
                // Start editing
                clickRoleButton("edit");
                break;
            default:
                break;
        }
    }

    function handleCtrlKeys(event, editing) {
        var char = String.fromCharCode(event.which).toLowerCase();
        switch (char) {
            case "s":
                if (editing) {
                    event.preventDefault();
                    // Save editing
                    angular
                        .element(".edit-controls .btn-primary")
                        .eq(0)
                        .click();
                }
                break;
            default:
                break;
        }
    }

    angular.element($window).bind("keydown", function(event) {
        var editing =
            Editointikontrollit.getEditMode() ||
            angular.element(".edit-controls .btn-primary").filter(":visible").length === 1;
        var isDialogOpen = angular.element(".modal .modal-dialog").filter(":visible").length > 0;
        var commenting = angular.element(".kommentti-textarea").filter(":visible").length > 0;
        if (!editing) {
            if (!event.ctrlKey && !event.metaKey && !isDialogOpen && !commenting) {
                handleSingleKeys(event);
            }
        } else {
            // Escape while editing => cancel (but ignore when modal open)
            if (event.which === 27 && !isDialogOpen) {
                angular
                    .element(".edit-controls .btn-default")
                    .eq(0)
                    .click();
            }
        }
        if (event.ctrlKey || event.metaKey) {
            handleCtrlKeys(event, editing);
        }
    });
});
