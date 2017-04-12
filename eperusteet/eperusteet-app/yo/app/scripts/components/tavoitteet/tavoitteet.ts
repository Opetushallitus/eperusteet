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

interface LokalisoituTeksti {
    kieli: string;
    teksti: string;
}

interface TekstiPalanen {
    id: number;
    teksti: LokalisoituTeksti;
    tunniste: any;
}

interface Kohdealue {
    nimi: TekstiPalanen;
    kuvaus: TekstiPalanen;
    oppiaineet: Array<any>;
    opetuksenTavoitteet: Array<any>;
}

angular.module('eperusteApp')
.component('tavoitteet', {
    templateUrl: 'scripts/components/tavoitteet/tavoitteet.html',
    bindings: {
        model: '<',
        laajaalaiset: '<',
        vaihe: '<',
        editing: '<'
    },
    controller: function (Kaanna) {

        function generateArraySetter(findFrom, manipulator = _.noop) {
            return item => {
                const found = _.find(findFrom, findItem => parseInt(findItem, 10) === item.id);
                item = _.clone(item);
                item.$hidden = !found;
                item.teksti = item.kuvaus;
                return manipulator(item) || item;
            };
        }

        this.$onInit = () => {
            _.each(this.model, tavoite => {
                tavoite.$accordionOpen = true;

                // Alustetaan valitut kohdealueet
                if (tavoite.kohdealueet && tavoite.kohdealueet.length > 0) {
                    tavoite.$valittuKohdealue = _.find(this.vaihe.opetuksenKohdealueet,
                        ka => ka.id.toString() === tavoite.kohdealueet[0]);
                }

                // Alustetaan laaja-alaiset
                tavoite.$osaaminen = _.map(this.laajaalaiset, generateArraySetter(tavoite.laajattavoitteet));
            });
        };

        this.toggleAll = () => {
            _.each(this.model, (tavoite) => {
                tavoite.$accordionOpen = !tavoite.$accordionOpen;
            })
        };

        this.treeOptions = {};

        this.lisaaTavoite = () => {
            this.model.push({
                $accordionOpen: true,
                $editing: false
            });
        };

        this.poistaTavoite = (tavoite) => {
            _.remove(this.model, tavoite);
        };

        this.asetaKohdealue = (tavoite) => {
            const valittu = tavoite.$valittuKohdealue ? [tavoite.$valittuKohdealue] : [];
            tavoite.kohdealueet = _(valittu)
                .map(item => item.id)
                .value();
        };

        this.kaannaKohdealue = (ka) => {
            return Kaanna.kaanna(ka.nimi);
        };

        this.poistaValittuKohdealue = (tavoite) => {
            tavoite.$valittuKohdealue = undefined;
        };

        this.addArvioinninKohde = (tavoite) => {
            tavoite.arvioinninkohteet.push({});
        };

        this.removeArvioinninKohde = (tavoite, index) => {
            tavoite.arvioinninkohteet.splice(index, 1);
        };
    }
});
