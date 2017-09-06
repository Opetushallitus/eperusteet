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

angular.module("eperusteApp").service("TutkinnonOsaLeikelautaService", (localStorageService, Algoritmit, $timeout) => {
    let leikelauta = [];

    function kopioiLeikelautaan() {
        if (localStorageService.isSupported) {
            $timeout(() => {
                localStorageService.set("leikelauta", leikelauta);
            });
        }
    }

    function addEvents() {
        return {
            start(event, ui) {},
            update(event, ui) {
                const sortable = ui.item.sortable;

                if (event.target != ui.item.sortable.droptarget[0]) {
                    // Perutaan alkuperäisen siirto
                    if (sortable.isCanceled()) {
                        return;
                    }
                    sortable.cancel();

                    // Kopioidaan alkuperäinen ja poistetaan siitä _id ja _tunniste kentät rekursiivisesti
                    const sourceModelClone = _.cloneDeep(sortable.model);
                    Algoritmit.removeFieldsRecursiveFromObject(sourceModelClone, new RegExp(/^_id|id|_tunniste|\$\$/i));

                    // Asetetaan kopiolle tyyppi ja nimi
                    sourceModelClone.$osanTyyppi = event.target.id;
                    switch (event.target.id) {
                        case "vapaatTekstit":
                            sourceModelClone.$osanNimi = sourceModelClone.nimi;
                            break;
                        default:
                            sourceModelClone.$osanNimi = sourceModelClone.otsikko;
                            break;
                    }

                    // Perutaan kopiointi jos tyyppi on väärä
                    if (
                        sortable.model.$osanTyyppi &&
                        ui.item.sortable.droptarget[0].id !== sortable.model.$osanTyyppi
                    ) {
                        if (sortable.isCanceled()) {
                            return;
                        }
                        sortable.cancel();
                    }

                    // Laitetaan kohde kloonattu olio kohdelistaan
                    sortable.droptargetModel.push(sourceModelClone);
                }
            },
            stop(event, ui) {
                kopioiLeikelautaan();
            },
            over(event, ui) {
                const sender = ui.sender,
                    target = event.target,
                    item = ui.item;

                if (sender && target && item) {
                    console.log(sender.context.id, target.id, item.sortable.model.$osanTyyppi);
                    if (
                        sender.context.id === "leikelauta" &&
                        target.id !== item.sortable.model.$osanTyyppi &&
                        target.id !== "leikelauta"
                    ) {
                        ui.placeholder.css({ backgroundColor: "red" });
                        //ui.placeholder.css({display: "none"});
                    } else {
                        ui.placeholder.css({ backgroundColor: "#f8f8f8" });
                        //ui.placeholder.css({display: "block"});
                    }
                }
            }
        };
    }

    return {
        createConnectedSortable(options = {}) {
            const defaults = {
                cursor: "move",
                tolerance: "pointer",
                forceHelperSize: true,
                placeholder: "sortable-placeholder",
                forcePlaceholderSize: true,
                opacity: ".7",
                delay: 100
            };

            return {
                ...defaults,
                ...options,
                ...addEvents()
            };
        },
        createLeikelautaSortable($scope, options = {}) {
            return {
                ...options,
                ...addEvents()
            };
        },
        initLeikelauta() {
            if (localStorageService.isSupported) {
                leikelauta = localStorageService.get("leikelauta");
            }
            return leikelauta;
        },
        poistaLeikelaudasta(id) {
            leikelauta.splice(id, 1);
            if (localStorageService.isSupported) {
                localStorageService.set("leikelauta", leikelauta);
            }
        }
    };
});
