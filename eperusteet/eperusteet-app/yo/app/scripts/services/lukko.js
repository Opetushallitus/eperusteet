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
  .factory('LukkoSisalto', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:osanId/suoritustavat/:suoritustapa/lukko/', {
      osanId: '@osanId',
      suoritustapa: '@suoritustapa',
      tyyppi: '@tyyppi'
    }, {
      multiple: { method: 'GET', url: SERVICE_LOC + '/perusteet/:osanId/suoritustavat/:suoritustapa/lukko/:tyyppi'}
    });
  })
  .factory('LukkoPerusteenosa', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteenosat/:osanId/lukko', {
      osanId: '@osanId'
    });
  })
  .factory('LukkoOppiaine', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/oppiaineet/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoVuosiluokkakokonaisuus', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/vuosiluokkakokonaisuudet/:vuosiluokkaId/lukko', {
      vuosiluokkaId: '@vuosiluokkaId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoOppiaineenVuosiluokkakokonaisuus', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/oppiaineet/:oppiaineId/vuosiluokkakokonaisuudet/:vuosiluokkaId/lukko', {
      oppiaineId: '@oppiaineId',
      perusteId: '@perusteId',
      vuosiluokkaId: '@vuosiluokkaId'
    });
  })
  .factory('LukkoPerusteenosaByTutkinnonOsaViite', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteenosat/tutkinnonosaviite/:viiteId/lukko', {
      viiteId: '@viiteId'

    });
  })
  .controller('LukittuSisaltoMuuttunutModalCtrl', function($scope, $modalInstance) {
    $scope.ok = function() { $modalInstance.close(); };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
    $scope.$on('$stateChangeSuccess', function() { $scope.peruuta(); });
  })
  .service('Lukitus', function($rootScope, LUKITSIN_MINIMI, LUKITSIN_MAKSIMI, Profiili,
    LukkoPerusteenosa, LukkoSisalto, Notifikaatiot, $modal, Editointikontrollit, Kaanna,
    LukkoOppiaine, PerusopetusService, LukkoOppiaineenVuosiluokkakokonaisuus, LukkoPerusteenosaByTutkinnonOsaViite, LukkoVuosiluokkakokonaisuus) {

    var lukitsin = null;
    var etag = null;

    var onevent = _.debounce(function() {
      if (lukitsin) { lukitsin(); }
    }, LUKITSIN_MINIMI, {
      leading: true,
      trailing: false,
      maxWait: LUKITSIN_MAKSIMI
    });
    angular.element(window).on('click', onevent);

    $rootScope.$on('$stateChangeSuccess', function() {
      lukitsin = null;
      etag = null;
    });

    function lueLukitus(Resource, obj, cb, errorCb) {
      Resource.get(obj, cb || angular.noop, errorCb || Notifikaatiot.serverLukitus);
    }

    function lukitse(Resource, obj, cb) {
      cb = cb || angular.noop;

      lukitsin = function() {
        Resource.save(obj, function(res, headers) {
          if (etag && headers().etag !== etag) {
            $modal.open({
              templateUrl: 'views/modals/sisaltoMuuttunut.html',
              controller: 'LukittuSisaltoMuuttunutModalCtrl'
            })
            .result.then(function() {
              etag = headers().etag;
            }, Editointikontrollit.cancelEditing);
          }
          else {
            etag = headers().etag;
            cb(res);
          }
        }, Notifikaatiot.serverLukitus);
      };
      lukitsin();
    }

    function vapauta(Resource, obj, cb) {
      cb = cb || angular.noop;
      Resource.remove(obj, cb, Notifikaatiot.serverLukitus);
      lukitsin = null;
    }

    function lukitseSisalto(id, suoritustapa, cb) {
      lukitse(LukkoSisalto, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function vapautaSisalto(id, suoritustapa, cb) {
      vapauta(LukkoSisalto, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function lukitsePerusteenosa(id, cb) {
      lukitse(LukkoPerusteenosa, {
        osanId: id
      }, cb);
    }

    function vapautaPerusteenosa(id, cb) {
      vapauta(LukkoPerusteenosa, {
        osanId: id
      }, cb);
    }

    function lukitsePerusteenosaByTutkinnonOsaViite(id, cb) {
      lukitse(LukkoPerusteenosaByTutkinnonOsaViite, {
        viiteId: id
      }, cb);
    }

    function vapautaPerusteenosaByTutkinnonOsaViite(id, cb) {
      vapauta(LukkoPerusteenosaByTutkinnonOsaViite, {
        viiteId: id
      }, cb);
    }

    function tarkistaLukitus(id, scope, suoritustapa) {
      var okCb = function(res) {
        if (res.haltijaOid && new Date() <= new Date(res.vanhentuu) && !res.oma) {
          scope.isLocked = true;
          scope.lockNotification = Kaanna.kaanna('lukitus-kayttajalla', {
            user: res.data ? res.data.haltijaOid : ''
          });
        }
        else {
          scope.isLocked = false;
          scope.lockNotification = '';
        }
      };

      if (suoritustapa) {
        lueLukitus(LukkoSisalto, {osanId: id, suoritustapa: suoritustapa}, okCb);
      } else {
        lueLukitus(LukkoPerusteenosa, {osanId: id}, okCb);
      }
    }

    function hae(parametrit, cb) {
      parametrit = _.merge({
        id: 0,
        tyyppi: 'sisalto',
        suoritustapa: 'naytto'
      }, parametrit);

      switch (parametrit.tyyppi) {
        case 'sisalto':
          lueLukitus(LukkoSisalto, { osanId: parametrit.id, suoritustapa: parametrit.suoritustapa }, cb);
          break;
        case 'perusteenosa':
          lueLukitus(LukkoPerusteenosa, { osanId: parametrit.id }, cb);
          break;
        default:
          break;
      }
    }

    return {
      hae: hae,
      tarkista: tarkistaLukitus,
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa,

      lukitseOppiaine: function (id, cb) {
        lukitse(LukkoOppiaine, {perusteId: PerusopetusService.getPerusteId(), osanId: id}, cb);
      },
      vapautaOppiaine: function (id, cb) {
        vapauta(LukkoOppiaine, {perusteId: PerusopetusService.getPerusteId(), osanId: id}, cb);
      },
      lukitseOppiaineenVuosiluokkakokonaisuus: function (oppiaineId, vuosiluokkaId, cb) {
        lukitse(LukkoOppiaineenVuosiluokkakokonaisuus, {
          perusteId: PerusopetusService.getPerusteId(),
          oppiaineId: oppiaineId,
          vuosiluokkaId: vuosiluokkaId
        }, cb);
      },
      vapautaOppiaineenVuosiluokkakokonaisuus: function (vuosiluokkaId, cb) {
        vapauta(LukkoVuosiluokkakokonaisuus, {
          perusteId: PerusopetusService.getPerusteId(),
          vuosiluokkaId: vuosiluokkaId
        }, cb);
      },
      lukitseVuosiluokkakokonaisuus: function (vuosiluokkaId, cb) {
        lukitse(LukkoVuosiluokkakokonaisuus, {
          perusteId: PerusopetusService.getPerusteId(),
          vuosiluokkaId: vuosiluokkaId
        }, cb);
      },
      vapautaVuosiluokkakokonaisuus: function (vuosiluokkaId, cb) {
        vapauta(LukkoVuosiluokkakokonaisuus, {
          perusteId: PerusopetusService.getPerusteId(),
          vuosiluokkaId: vuosiluokkaId
        }, cb);
      },
      lukitsePerusteenosaByTutkinnonOsaViite: lukitsePerusteenosaByTutkinnonOsaViite,
      vapautaPerusteenosaByTutkinnonOsaViite: vapautaPerusteenosaByTutkinnonOsaViite

    };
  });
