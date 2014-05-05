'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('PerusteenRakenne', function(PerusteProjektiService, PerusteprojektiResource, PerusteRakenteet, TreeCache, PerusteTutkinnonosat, Perusteet) {
    function haeRakenne(perusteProjektiId, success, failure) {
      var response = {};
      PerusteprojektiResource.get({ id: perusteProjektiId }, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        Perusteet.get({
          perusteenId: vastaus._peruste
        }, function(peruste) {
          PerusteRakenteet.get({
            perusteenId: peruste.id,
            suoritustapa: peruste.suoritustavat[0].suoritustapakoodi // FIXME
          }, function(rakenne) {
            PerusteTutkinnonosat.query({
              perusteenId: peruste.id,
              suoritustapa: peruste.suoritustavat[0].suoritustapakoodi // FIXME
            }, function(tosat) {
              response.rakenne = rakenne;
              response.tutkinnonOsat = _.zipObject(_.pluck(tosat, '_tutkinnonOsa'), tosat);
              success(response);
            });
          });
        });
      });
    }

    return {
      hae: haeRakenne
    };
  })
  .factory('PerusteTutkinnonosat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa/tutkinnonosat',
      {
        perusteenId: '@id',
        suoritustapa: '@suoritustapa'
      });
  })
  .factory('PerusteRakenteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa/rakenne',
      {
        perusteenId: '@id',
        suoritustapa: '@suoritustapa'
      });
  })
  .factory('Perusteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId',
      {
        perusteenId: '@id'
      });
  })
  .factory('Suoritustapa', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa');
  })
  .factory('SuoritustapaSisalto', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto',
    {
      perusteId: '@id',
      suoritustapa: '@suoritustapa'
    }, {
        add: {method: 'PUT'}
    });
  });
