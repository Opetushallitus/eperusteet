'use strict';

angular.module('eperusteApp')
  .service('PerusteenRakenne', function() {
    // function haeRakenne() {
    //   PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
    //     PerusteProjektiService.save(vastaus);
    //     PerusteRakenteet.get({
    //       perusteenId: vastaus.peruste.id,
    //       suoritustapa: vastaus.peruste.suoritustavat[0].suoritustapakoodi // FIXME
    //     }, function(rakenne) {
    //       PerusteTutkinnonosat.query({
    //         perusteenId: vastaus.peruste.id,
    //         suoritustapa: vastaus.peruste.suoritustavat[0].suoritustapakoodi // FIXME
    //       }, function(tosat) {
    //         $scope.rakenne = rakenne;
    //         $scope.rakenne.tutkinnonOsat = tosat;
    //         $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck($scope.rakenne.tutkinnonOsat, '_tutkinnonOsa'), $scope.rakenne.tutkinnonOsat);
    //       });
    //     }, function() {
    //       $scope.rakenne.$resolved = true;
    //     });
    //   });
    // }
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
