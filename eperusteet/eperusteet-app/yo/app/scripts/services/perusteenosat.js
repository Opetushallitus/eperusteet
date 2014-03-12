'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteenOsat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId',
      {
        osanId: '@id'
      },
      {
        byKoodiUri: { method: 'GET', params: { koodi: true } },
        saveTekstikappale: {method:'POST', params:{tyyppi:'perusteen-osat-tekstikappale'}},
        saveTutkinnonOsa: {method:'POST', params:{tyyppi:'perusteen-osat-tutkinnon-osa'}}
      });
  })
  .service('TutkinnonOsanValidointi', function($q, PerusteenOsat) {
    function validoi(tutkinnonOsa) {
      var virheet = [];
      _.forEach(['nimi'], function(f) {
        if (!tutkinnonOsa[f] || tutkinnonOsa[f] === '') {
          virheet.push({ 'koodi-virhe-4': f});
        }
      });
      return virheet;
    }

    return {
      validoi: function(tutkinnonOsa) {
        var deferred = $q.defer();

        if (!tutkinnonOsa.koodiUri || tutkinnonOsa.koodiUri === '') {
          deferred.reject('koodi-virhe-2');
        } else if (_.isNaN(tutkinnonOsa.koodiUri)) {
          deferred.reject('koodi-virhe-3');
        } else {
          PerusteenOsat.byKoodiUri({ osanId: tutkinnonOsa.koodiUri }, function() {
            deferred.reject('koodi-virhe-1');
          }, function() {
            var virheet = validoi(tutkinnonOsa);
            if (_.isEmpty(virheet)) {
              deferred.resolve();
            } else {
              deferred.reject(virheet);
            }
          });
        }
        return deferred.promise;
      }
    };
  });
