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
        saveTutkinnonOsa: {method:'POST', params:{tyyppi:'perusteen-osat-tutkinnon-osa'}},
        revisions: {method: 'GET', isArray: true, url: SERVICE_LOC + '/perusteenosat/:osanId/revisions'},
        getRevision: {method: 'GET', url: SERVICE_LOC + '/perusteenosat/:osanId/revisions/:revisionId'}
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

        if (!tutkinnonOsa.koodiUri || tutkinnonOsa.koodiUri === '') {
          deferred.reject(['koodi-virhe-1']);
        } else {
          PerusteenOsat.byKoodiUri({ osanId: tutkinnonOsa.koodiUri }, function() {
            deferred.reject(['koodi-virhe-2']);
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
