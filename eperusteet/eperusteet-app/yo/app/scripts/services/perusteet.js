'use strict';
/* global _ */

angular.module('eperusteApp')
  .factory('PerusteTutkinnonosat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteenId/suoritustavat/:suoritustapa/tutkinnonosat',
      {
        perusteenId: '@id',
        suoritustapa: '@suoritustapa'
      }, {
        update: { method: 'PUT' }
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
  })
  .service('PerusteenRakenne', function(PerusteProjektiService, PerusteprojektiResource, PerusteRakenteet, TreeCache, PerusteTutkinnonosat, Perusteet) {
    function haeRakenne(perusteProjektiId, success) {
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
              response.$peruste = peruste;
              response.tutkinnonOsat = _(tosat)
                                        .pluck('_tutkinnonOsa')
                                        .zipObject(tosat)
                                        .value();
              // console.log(response);
              success(response);
            });
          });
        });
      });
    }

    function tallennaRakenne(rakenne, id, suoritustapa, success) {
      var after = _.after(_.size(rakenne.tutkinnonOsat) + 1, success);

      PerusteRakenteet.save({
        perusteenId: id,
        suoritustapa: suoritustapa
      }, rakenne.rakenne, function() {
        // TODO: notifikaatio
        after();
      }, function() {
        // TODO: notifikaatio
      });

      _.forEach(_.values(rakenne.tutkinnonOsat), function(osa) {
        console.log(osa);
        PerusteTutkinnonosat.update({
          perusteenId: id,
          suoritustapa: suoritustapa
        }, osa, function() {
          after();
        }, function(virhe) {
          console.log(virhe);
          // TODO: notifikaatio
        });
      });
    }

    return {
      hae: haeRakenne,
      tallenna: tallennaRakenne
    };
  });
