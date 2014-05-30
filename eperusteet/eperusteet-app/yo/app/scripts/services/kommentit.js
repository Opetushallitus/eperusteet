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
  .factory('KommentitByParent', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/kommentit/parent/:id', { id: '@id' }, {
      get: { method: 'GET', isArray: true }
    });
  })
  .factory('KommentitByYlin', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/kommentit/ylin/:id', { id: '@id' }, {
      get: { method: 'GET', isArray: true }
    });
  })
  .factory('KommentitByPerusteprojekti', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/kommentit/perusteprojekti/:id', { id: '@id' }, {
      get: { method: 'GET', isArray: true }
    });
  })
  .factory('KommentitCRUD', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/kommentit/:id', { id: '@id' }, {
      get: { method: 'GET', isArray: true },
      update: { method: 'PUT' }
    });
  })
  .service('KommenttiSivuCache', function() {
    this.perusteProjektiId = null;
  })
  .service('Kommentit', function(Notifikaatiot, KommenttiSivuCache, KommentitCRUD, KommentitByParent, KommentitByYlin, KommentitByPerusteprojekti) {
    function rakennaKommenttiPuu(viestit) {
      viestit = _(viestit)
        .map(function(viesti) {
          viesti.muokattu = viesti.luotu === viesti.muokattu ? null : viesti.muokattu;
          viesti.viestit = [];
          viesti.$sortDate = viesti.muokattu ? viesti.muokattu : viesti.luotu;
          return viesti;
        })
        .sort('$sortDate')
        .value();

      var viestiMap = _.zipObject(_.map(viestit, 'id'), _.map(viestit, _.identity));

      _.forEach(viestit, function(viesti) {
        if (viesti.parentId && viestiMap[viesti.parentId]) {
          viestiMap[viesti.parentId].viestit.unshift(viesti);
        }
      });

      var baseKommentit = _(viestiMap)
        .values()
        .reject(function(viesti) { return viesti.parentId !== null; })
        .sort('$sortDate')
        .reverse()
        .value();

      var sisaltoObject = {
        $resolved: true,
        yhteensa: _.size(viestit),
        seuraajat: [],
        viestit: baseKommentit
      };
      return sisaltoObject;
    }

    function haeKommentitByPerusteprojekti(id, cb) {
      cb = cb || angular.noop;
      KommentitByPerusteprojekti.get({ id: id }, function(res) {
        cb(rakennaKommenttiPuu(res));
      },
      Notifikaatiot.serverCb);
    }

    // TODO: ota käyttöön tarvittaessa
    // function haeKommentitByParent(id, cb) {
    // }
    // function haeKommentitByYlin(id, cb) {
    // }
    // function haeAliKommentit(parentId) {
    // }

    function lisaaKommentti(parent, viesti) {
      KommentitCRUD.save({
        parentId: parent ? parent.id : null,
        sisalto: viesti,
        perusteprojektiId: KommenttiSivuCache.perusteProjektiId
      }, function(res) {
        res.muokattu = null;
        parent.viestit.unshift(res);
      });
    }

    function poistaKommentti(viesti) {
      KommentitCRUD.remove({ id: viesti.id }, function() {
        viesti.sisalto = '';
        viesti.muokattu = new Date();
        viesti.poistettu = true;
        viesti.muokkaaja = null;
        viesti.lahettaja = null;
        Notifikaatiot.onnistui('kommentti-poistettu');
      },
      Notifikaatiot.serverCb);
    }

    function muokkaaKommenttia(viesti, uusiviesti) {
      KommentitCRUD.update({
        id: viesti.id
      }, { sisalto: uusiviesti },
      function(res) {
        viesti.sisalto = res.sisalto;
        viesti.muokattu = res.muokattu;
      },
      Notifikaatiot.serverCb);
    }

    return {
      haeKommentitByPerusteprojekti: haeKommentitByPerusteprojekti,
      lisaaKommentti: lisaaKommentti,
      poistaKommentti: poistaKommentti,
      muokkaaKommenttia: muokkaaKommenttia
    };
  });
