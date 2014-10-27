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
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/tutkinnonosat/:osanId', {
      perusteId: '@id',
      suoritustapa: '@suoritustapa',
      osanId: '@id'
    });
  })
  .factory('PerusteTutkinnonosat', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/tutkinnonosat', {
      perusteId: '@id',
      suoritustapa: '@suoritustapa'
    }, {
      get: { method: 'GET', isArray: true },
      update: { method: 'PUT' }
    });
  })
  .factory('PerusteRakenteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne', {
      perusteId: '@id',
      suoritustapa: '@suoritustapa'
    });
  })
  .factory('RakenneVersiot', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne/versiot');
  })
  .factory('RakenneVersio', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne/versio/:versioId', {
      perusteId: '@perusteId',
      suoritustapa: '@suoritustapa',
      versioId: '@versioId'
    }, {
      palauta: { method: 'POST', url: SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/rakenne/palauta/:versioId' }
    });
  })
  .factory('Perusteet', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId', {
      perusteId: '@id'
    }, {
      info: { method: 'GET', url: SERVICE_LOC + '/perusteet/info' }
    });
  })
  .factory('Vuosiluokkakokonaisuudet', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/vuosiluokkakokonaisuudet/:osanId', {
      osanId: '@id'
    });
  })
  .factory('Oppiaineet', function ($resource, SERVICE_LOC) {
    var baseUrl = SERVICE_LOC + '/perusteet/:perusteId/perusopetus/oppiaineet/:osanId';
    return $resource(baseUrl, { osanId: '@id' }, {
      oppimaarat: { method: 'GET', isArray: true, url: baseUrl + '/oppimaarat'}
    });
  })
  .factory('OppiaineenVuosiluokkakokonaisuudet', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/oppiaineet/:oppiaineId/vuosiluokkakokonaisuudet/:osanId', {
      osanId: '@id'
    });
  })
  .factory('LaajaalaisetOsaamiset', function ($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/laajaalaisetosaamiset/:osanId', {
      osanId: '@id'
    });
  })
  .factory('Suoritustapa', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa');
  })
  .factory('SuoritustapaSisalto', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto', {
      perusteId: '@id',
      suoritustapa: '@suoritustapa'
    }, {
      add: {method: 'PUT'},
      addChild: {
        method: 'POST',
        url: SERVICE_LOC + '/perusteet/:perusteId/suoritustavat/:suoritustapa/sisalto/:perusteenosaViiteId/lapsi/:childId'
      }
    });
  })
  .service('PerusteenRakenne', function(PerusteProjektiService, PerusteprojektiResource, PerusteRakenteet,
    PerusteTutkinnonosat, Perusteet, PerusteTutkinnonosa, Notifikaatiot) {

    function haeTutkinnonosatByPeruste(perusteId, suoritustapa, success) {
      PerusteTutkinnonosat.query({
        perusteId: perusteId,
        suoritustapa: suoritustapa
      },
      success,
      Notifikaatiot.serverCb);
    }

    function haeTutkinnonosat(perusteProjektiId, suoritustapa, success) {
      PerusteprojektiResource.get({ id: perusteProjektiId }, function(perusteprojekti) {
        haeTutkinnonosatByPeruste(perusteprojekti._peruste, suoritustapa, success);
      });
    }

    function pilkoTutkinnonOsat(tutkinnonOsat, response) {
      response = response || {};
      response.tutkinnonOsaViitteet = _(tutkinnonOsat).pluck('id')
                                                      .zipObject(tutkinnonOsat)
                                                      .value();
      response.tutkinnonOsat = _.zipObject(_.map(tutkinnonOsat, '_tutkinnonOsa'), tutkinnonOsat);
      return response;
    }

    function haeByPerusteprojekti(id, suoritustapa, success) {
      PerusteprojektiResource.get({ id: id }, function(vastaus) {
        hae(vastaus._peruste, suoritustapa, success);
      });
    }

    function rakennaPalaute(rakenne, peruste, tutkinnonOsat) {
      var response = {};
      rakenne.kuvaus = rakenne.kuvaus || {};
      response.rakenne = rakenne;
      response.$peruste = peruste;
      response.tutkinnonOsaViitteet = _(tutkinnonOsat).pluck('id')
                                                      .zipObject(tutkinnonOsat)
                                                      .value();
      response.tutkinnonOsat = _.zipObject(_.map(tutkinnonOsat, '_tutkinnonOsa'), tutkinnonOsat);
      return response;
    }

    function hae(perusteId, suoritustapa, success) {
      Perusteet.get({
        perusteId: perusteId
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
            success(pilkoTutkinnonOsat(tosat, rakennaPalaute(rakenne, peruste, tosat)));
          });
        });
      });
    }

    function kaikilleRakenteille(rakenne, f) {
      if (!rakenne || !f) { return; }
      _.forEach(rakenne.osat, function(r) {
        r.$parent = rakenne;
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

    function haePerusteita(haku, success) {
      Perusteet.info({
        nimi: haku,
        sivukoko: 15
      }, success, Notifikaatiot.serverCb);
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
        set[osa._tutkinnonOsaViite] = osa._tutkinnonOsaViite ? true : false;
      });
      return set;
    }

    return {
      hae: hae,
      haeByPerusteprojekti: haeByPerusteprojekti,
      haePerusteita: haePerusteita,
      pilkoTutkinnonOsat: pilkoTutkinnonOsat,
      haeTutkinnonosat: haeTutkinnonosat,
      haeTutkinnonosatByPeruste: haeTutkinnonosatByPeruste,
      kaikilleRakenteille: kaikilleRakenteille,
      poistaTutkinnonOsaViite: poistaTutkinnonOsaViite,
      puustaLoytyy: puustaLoytyy,
      tallennaRakenne: tallennaRakenne,
      tallennaTutkinnonosat: tallennaTutkinnonosat,
      validoiRakennetta: validoiRakennetta,
    };
  });
