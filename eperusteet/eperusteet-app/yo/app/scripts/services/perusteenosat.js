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
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteenOsat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId', {
      osanId: '@id'
    }, {
      byKoodiUri: {method: 'GET', isArray: true, params: {koodi: true}},
      saveTekstikappale: {method: 'POST', params: {tyyppi: 'perusteen-osat-tekstikappale'}},
      saveTutkinnonOsa: {method: 'POST', params: {tyyppi: 'perusteen-osat-tutkinnon-osa'}},
      versiot: {method: 'GET', isArray: true, url: SERVICE_LOC + '/perusteenosat/:osanId/versiot'},
      getVersio: {method: 'GET', url: SERVICE_LOC + '/perusteenosat/:osanId/versio/:versioId'},
      palauta: {method: 'POST', url: SERVICE_LOC + '/perusteenosat/:osanId/palauta/:versioId'},
      kloonaa: {method: 'POST', url: SERVICE_LOC + '/perusteenosat/:osanId/kloonaa'}
    });
  })
  .factory('PerusteenOsaViitteet', function($resource, SERVICE_LOC, $stateParams, PerusteprojektiTiedotService) {
    var baseUrl = SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto/:viiteId';
    //FIXME
    var pleaseFixMe;
    PerusteprojektiTiedotService.then(function(value) {
      pleaseFixMe = value;
    });
    return $resource(baseUrl, {
      viiteId: '@viiteId',
      perusteId: function() {
        //FIXME (parempi tapa perusteen id:n hakemiseen)
        return pleaseFixMe.getPeruste().id;
      },
      suoritustapa: function() {
        return $stateParams.suoritustapa;
      }
    }, {
      kloonaaTekstikappale: {
        method: 'POST',
        url: baseUrl + '/muokattavakopio',
        params: {tyyppi: 'perusteen-osat-tekstikappale'}
      },
      //FIXME
      kloonaaTutkinnonOsa: {method: 'POST', url: SERVICE_LOC + '/perusteenosaviitteet/kloonaa/:viiteId', params: {tyyppi: 'perusteen-osat-tutkinnon-osa'}},
      update: {method: 'POST'}
    });
  })
  .factory('TutkinnonOsanOsaAlue', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId/osaalue/:osaalueenId', {
      osaalueenId: '@id'
    }, {
      list: {method: 'GET', isArray: true, url: SERVICE_LOC + '/perusteenosat/:osanId/osaalueet'}
    });
  })
  .factory('Osaamistavoite', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId/osaalue/:osaalueenId/osaamistavoite/:osaamistavoiteId', {
      osaamistavoiteId: '@id'
    }, {
      list: {method: 'GET', isArray: true, url: SERVICE_LOC + '/perusteenosat/:osanId/osaalue/:osaalueenId/osaamistavoitteet'}
    });
  })
  .service('TutkinnonOsanValidointi', function($q, PerusteenOsat) {
    function validoi(tutkinnonOsa) {
      var virheet = [];
      var kentat = ['nimi'];
      _.forEach(kentat, function(f) {
        if (!tutkinnonOsa[f] || tutkinnonOsa[f] === '') {
          virheet.push(f);
        }
      });
      if (!_.isEmpty(virheet)) {
        virheet.unshift('koodi-virhe-3');
      }
      return virheet;
    }

    return {
      validoi: function(tutkinnonOsa) {
        var deferred = $q.defer();

        PerusteenOsat.byKoodiUri({
          osanId: tutkinnonOsa.koodiUri
        }, function(re) {
          if (re.length === 0) {
            deferred.resolve();
          } else {
            deferred.reject(['koodi-virhe-2']);
          }
        }, function() {
          var virheet = validoi(tutkinnonOsa);
          if (_.isEmpty(virheet)) {
            deferred.resolve();
          } else {
            deferred.reject(virheet);
          }
        });
        return deferred.promise;
      }
    };
  });
