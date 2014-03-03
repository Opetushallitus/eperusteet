'use strict';
/* global _ */

angular.module('eperusteApp')
  .factory('PerusteenOsat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId',
      {
        osanId: '@id'
      },
      {
        byKoodi: { method: 'GET', params: { koodi: true } },
        saveTekstikappale: {method:'POST', params:{tyyppi:'perusteen-osat-tekstikappale'}},
        saveTutkinnonOsa: {method:'POST', params:{tyyppi:'perusteen-osat-tutkinnon-osa'}}
      });
  })
  .service('TutkinnonOsanValidointi', function($q, PerusteenOsat) {
    function validoi(tutkinnonOsa) {
      var virheet = {};
      return virheet;
    }

    return {
      validoi: function(tutkinnonOsa) {
        var deferred = $q.defer();

        PerusteenOsat.byKoodi({ osanId: tutkinnonOsa.koodi }, function(re) {
          console.log('Jo oleva:', re);
          deferred.reject();
          return deferred.promise;
        }, function(error) {
          var virheet = validoi(tutkinnonOsa);
          if (virheet !== {}) {
            deferred.reject(virheet);
          } else {
            deferred.resolve();
          }
        });
      }
    };
  });
