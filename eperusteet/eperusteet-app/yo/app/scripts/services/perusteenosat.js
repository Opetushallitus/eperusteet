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
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId',
      {
        osanId: '@id'
      }, {
        byKoodiUri: { method: 'GET', isArray: true, params: { koodi: true } },
        saveTekstikappale: {method:'POST', params:{tyyppi:'perusteen-osat-tekstikappale'}},
        saveTutkinnonOsa: {method:'POST', params:{tyyppi:'perusteen-osat-tutkinnon-osa'}},
        revisions: {method: 'GET', isArray: true, url: SERVICE_LOC + '/perusteenosat/:osanId/revisions'},
        getRevision: {method: 'GET', url: SERVICE_LOC + '/perusteenosat/:osanId/revisions/:revisionId'}
      });
  })
  .factory('PerusteenOsaViitteet', function($resource, SERVICE_LOC) {
      return $resource(SERVICE_LOC + '/perusteenosaviitteet/sisalto/:viiteId');
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
