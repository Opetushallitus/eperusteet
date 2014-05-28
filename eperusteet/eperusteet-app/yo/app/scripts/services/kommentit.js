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

'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Kommentit', function(YleinenData) {
    function haeKommentit() {
      function randomDate() {
        var start = new Date(2010, 0, 1);
        var end = new Date();
        return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
      }
      return {
        yhteensa: 5,
        seuraajat: [1,2,3],
        viestit: [{
          lahettaja: 2,
          nimi: 'Mikko Mallikas',
          lahetetty: randomDate(),
          muokattu: null,
          sisalto: 'Terve kaikki',
          viestit: [{
            lahettaja: 3,
            nimi: 'Joku nimi',
            lahetetty: randomDate(),
            muokattu: null,
            sisalto: 'Kommentti',
            viestit: [{
              lahettaja: 3,
              nimi: 'Joku nimi',
              lahetetty: randomDate(),
              muokattu: new Date(),
              sisalto: YleinenData.loremIpsum + YleinenData.loremIpsum,
              viestit: []
            }]
          }]
        }, {
          lahettaja: 2,
          nimi: 'Mikko Mallikas',
          lahetetty: new Date(),
          muokattu: null,
          sisalto: 'Terve taas!',
          viestit: [{
            lahettaja: 1,
            nimi: 'Teemu Teekkari',
            lahetetty: randomDate(),
            muokattu: null,
            sisalto: 'No moi moi',
            viestit: []
          }]
        }]
      };
    }

    function haeAliKommentit(parentId) {
    }

    function lisaaKommentti(parent, viesti) {
      parent.viestit.unshift({
        lahettaja: 1,
        nimi: 'Teemu Teekkari',
        lahetetty: new Date(),
        muokattu: null,
        sisalto: viesti,
        viestit: []
      });
    }

    function poistaKommentti(viesti) {
      viesti.sisalto = '';
      viesti.muokattu = new Date();
      viesti.poistettu = true;
      viesti.nimi = null;
      viesti.lahettaja = null;
    }

    function muokkaaKommenttia(viesti, uusiviesti) {
      viesti.sisalto = uusiviesti;
    }

    return {
      haeKommentit: haeKommentit,
      haeAliKommentit: haeAliKommentit,
      lisaaKommentti: lisaaKommentti,
      poistaKommentti: poistaKommentti,
      muokkaaKommenttia: muokkaaKommenttia
    };
  });
