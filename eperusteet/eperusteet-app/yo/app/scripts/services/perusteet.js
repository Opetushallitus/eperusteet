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
  .factory('PerusteTutkinnonosa', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/tutkinnonosat/:osanId',
      {
        perusteId: '@id',
        suoritustapa: '@suoritustapa',
        osanId: '@id'
      });
  })
  .factory('PerusteTutkinnonosat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/tutkinnonosat',
      {
        perusteId: '@id',
        suoritustapa: '@suoritustapa'
      }, {
        get: { method: 'GET', isArray: true },
        update: { method: 'PUT' }
      });
  })
  .factory('PerusteRakenteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne',
      {
        perusteId: '@id',
        suoritustapa: '@suoritustapa'
      });
  })
  .factory('RakenneVersiot', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne/versiot');
  })
  .factory('RakenneVersio', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne/versio/:versioId');
  })
  .factory('Perusteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId',
      {
        perusteId: '@id'
      });
  })
  .factory('PerusteenOsaviitteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteenosaviitteet/:viiteId');
  })
  .factory('Suoritustapa', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa');
  })
  .factory('SuoritustapaSisalto', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto',
    {
      perusteId: '@id',
      suoritustapa: '@suoritustapa'
    }, {
      add: {method: 'PUT'},
      addChild: {
        method: 'POST',
        url: SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto/:perusteenosaViiteId/lapsi'
      }
    });
  })
  .service('PerusteenRakenne', function(PerusteProjektiService, PerusteprojektiResource, PerusteRakenteet,
    PerusteTutkinnonosat, Perusteet, PerusteTutkinnonosa, Notifikaatiot) {

    function haeTutkinnonosat(perusteProjektiId, suoritustapa, success) {
      PerusteprojektiResource.get({ id: perusteProjektiId }, function(perusteprojekti) {
        PerusteTutkinnonosat.query({
          perusteId: perusteprojekti._peruste,
          suoritustapa: suoritustapa
        },
        success,
        Notifikaatiot.serverCb);
      });
    }

    function haeRakenne(perusteProjektiId, suoritustapa, success) {
      var response = {};
      PerusteprojektiResource.get({ id: perusteProjektiId }, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        Perusteet.get({
          perusteId: vastaus._peruste
        }, function(peruste) {
          suoritustapa = suoritustapa || peruste.suoritustavat[0].suoritustapakoodi;
          PerusteRakenteet.get({
            perusteId: peruste.id,
            suoritustapa: suoritustapa
          }, function(rakenne) {
            PerusteTutkinnonosat.query({
              perusteId: peruste.id,
              suoritustapa: suoritustapa
            }, function(tosat) {
              response.rakenne = rakenne;
              response.$peruste = peruste;
              response.tutkinnonOsaViitteet = _(tosat)
                                        .pluck('id')
                                        .zipObject(tosat)
                                        .value();
              response.tutkinnonOsat = _.zipObject(_.map(tosat, '_tutkinnonOsa'), tosat);
              success(response);
            });
          });
        });
      });
    }

    function kaikilleRakenteille(rakenne, f) {
      if (!rakenne || !f) { return; }
      _.forEach(rakenne.osat, function(r) {
        kaikilleRakenteille(r, f);
        f(r);
      });
    }

    function tallennaRakenne(rakenne, id, suoritustapa, success, after) {
      success = success || angular.noop;
      after = after || angular.noop;
      PerusteRakenteet.save({
        perusteId: id,
        suoritustapa: suoritustapa
      }, rakenne.rakenne,
      function() {
        after();
        success();
      },
      function(err) {
        after();
        Notifikaatiot.serverCb(err);
      });
    }

    function tallennaTutkinnonosat(rakenne, id, suoritustapa, success) {
      success = success || function() {};
      var after = _.after(_.size(rakenne.tutkinnonOsat), success);
      _.forEach(_.values(rakenne.tutkinnonOsat), function(osa) {
        PerusteTutkinnonosa.save({
          perusteId: id,
          suoritustapa: suoritustapa,
          osanId: osa.id
        },
        osa,
        after(),
        Notifikaatiot.serverCb);
      });
    }

    function validoiRakennetta(rakenne, testi) {
      if (testi(rakenne)) {
        return true;
      }
      else if (rakenne.osat) {
        var löyty = false;
        _.forEach(rakenne.osat, function(osa) {
          if (validoiRakennetta(osa, testi)) {
            löyty = true;
          }
        });
        return löyty;
      }
      return false;
    }

    function poistaTutkinnonOsaViite(osaId, _peruste, suoritustapa, success) {
      PerusteTutkinnonosa.remove({
          perusteId: _peruste,
          suoritustapa: suoritustapa,
          osanId: osaId
      }, function(res) {
        success(res);
      }, Notifikaatiot.serverCb);
    }

    function puustaLoytyy(rakenne) {
      var set = {};
      kaikilleRakenteille(rakenne, function(osa) {
        set[osa._tutkinnonOsa] = osa._tutkinnonOsa ? true : false;
      });
      return set;
    }

    return {
      hae: haeRakenne,
      tallennaRakenne: tallennaRakenne,
      haeTutkinnonosat: haeTutkinnonosat,
      tallennaTutkinnonosat: tallennaTutkinnonosat,
      poistaTutkinnonOsaViite: poistaTutkinnonOsaViite,
      kaikilleRakenteille: kaikilleRakenteille,
      validoiRakennetta: validoiRakennetta,
      puustaLoytyy: puustaLoytyy
    };
  });
