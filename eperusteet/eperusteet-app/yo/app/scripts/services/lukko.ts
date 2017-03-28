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

angular.module('eperusteApp')
  .factory('LukkoRakenne', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:osanId/suoritustavat/:suoritustapa/rakenne/lukko/', {
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
  .factory('LukkoLukioOppiaine', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/lukiokoulutus/oppiaineet/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLukiokurssi', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/lukiokoulutus/kurssit/:kurssiId/lukko', {
      kurssiId: '@kurssiId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLukioRakenne', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/lukiokoulutus/rakenne/lukko', {
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLukioAihekokonaisuus', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/lukiokoulutus/aihekokonaisuudet/:aihekokonaisuusId/lukko', {
      aihekokonaisuusId: '@aihekokonaisuusId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLukioAihekokonaisuudet', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/lukiokoulutus/aihekokonaisuudet/lukko', {
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLukioYleisetTavoitteet', function(SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/lukiokoulutus/yleisettavoitteet/lukko', {
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoLaajaalainenOsaaminen', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/laajaalaisetosaamiset/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoAIPELaajaalainenOsaaminen', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/aipe/laajaalaisetosaamiset/:osanId/lukko', {
      osanId: '@osanId',
      perusteId: '@perusteId'
    });
  })
  .factory('LukkoVuosiluokkakokonaisuus', function (SERVICE_LOC, $resource) {
    return $resource(SERVICE_LOC + '/perusteet/:perusteId/perusopetus/vuosiluokkakokonaisuudet/:osanId/lukko', {
      osanId: '@osanId',
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
  .service('Lukitus', function ($rootScope, $state, $stateParams, LUKITSIN_MINIMI, LUKITSIN_MAKSIMI, $timeout, $q,
                                Profiili, LukkoPerusteenosa, LukkoRakenne, Notifikaatiot, $modal, Editointikontrollit, Kaanna,
                                LukkoOppiaine, LukkoLukioOppiaine, LukkoLukiokurssi, LukkoLukioAihekokonaisuudet, PerusopetusService, LukiokoulutusService,
                                LukkoOppiaineenVuosiluokkakokonaisuus, LukkoPerusteenosaByTutkinnonOsaViite,
                                LukkoVuosiluokkakokonaisuus, LukkoLaajaalainenOsaaminen, LukkoLukioRakenne,
                                LukkoLukioAihekokonaisuus, LukkoLukioYleisetTavoitteet, LukkoAIPELaajaalainenOsaaminen) {

    var lukitsin = null;
    var etag = null;
    var vapautin = null;

    $rootScope.$on('$stateChangeSuccess', function() {
      lukitsin = null;
      etag = null;
    });

    function lueLukitus(Resource, obj, cb = _.noop, errorCb = Notifikaatiot.serverLukitus) {
      Resource.get(obj, cb, errorCb);
    }

    var genericResourcesByState = {
      'root.perusteprojekti.suoritustapa.lukioosaalue': function() {
        switch($stateParams.osanTyyppi) {
          case 'oppiaineet_oppimaarat':return LukkoLukioOppiaine;
          case 'aihekokonaisuudet':return LukkoLukioAihekokonaisuudet;
          default: return null;
        }
      },
      'root.perusteprojekti.suoritustapa.osaalue': function() {
        switch($stateParams.osanTyyppi) {
          // case '': return LukkoPerusteenosa;
          // case '': return LukkoRakenne;
          case 'oppiaineet': return LukkoOppiaine;
          // case '': return LukkoOppiaineenVuosiluokkakokonaisuus;
          // case '': return LukkoPerusteenosaByTutkinnonOsaViite;
          case 'vuosiluokat': return LukkoVuosiluokkakokonaisuus;
          case 'osaaminen': return LukkoLaajaalainenOsaaminen;
          default: return null;
        }
      },
      'root.perusteprojekti.suoritustapa.aipeosaalue': function() {
        switch($stateParams.osanTyyppi) {
          //case 'oppiaineet': return LukkoOppiaine;
          //case 'vuosiluokat': return LukkoVuosiluokkakokonaisuus;
          case 'osaaminen': return LukkoAIPELaajaalainenOsaaminen;
          default: return null;
        }
      },
      'root.perusteprojekti.suoritustapa.lisaaLukioKurssi': _.constant(LukkoLukiokurssi),
      'root.perusteprojekti.suoritustapa.muokkaakurssia': _.constant(LukkoLukiokurssi)
    };

    function tilaToLukkoResource() {
      var stateProvider = genericResourcesByState[$state.current.name];
      if (stateProvider) {
        return stateProvider();
      }
    }

    function tilaToLukkoParams():any {
      // TODO: Lisää muille tiloille vastaavat
      if ($state.current.name === 'root.perusteprojekti.suoritustapa.osaalue' && $stateParams.osanTyyppi && $stateParams.osanId !== 'uusi') {
        return { perusteId: PerusopetusService.getPerusteId(), osanId: $stateParams.osanId };
      }
      if ($state.current.name === 'root.perusteprojekti.suoritustapa.lukioosaalue' && $stateParams.osanTyyppi && $stateParams.osanId !== 'uusi') {
        return { perusteId: PerusopetusService.getPerusteId(), osanId: $stateParams.osanId };
      }
      if ($state.current.name === 'root.perusteprojekti.suoritustapa.muokkaakurssia') {
        return { perusteId: PerusopetusService.getPerusteId(), kurssiId: $stateParams.kurssiId };
      }
      return null;
    }

    function genericVapauta(cb) {
      if (vapautin) {
        var v = vapautin;
        vapautin = null;
        return v(cb);
      }
      if (cb) {
        cb();
      }
      var d = $q.defer();
      d.resolve();
      return d.promise;
    }

    function genericLukitse(cb) {
      var lukkotyyppi = tilaToLukkoResource();
      if (lukkotyyppi) {
        var params = tilaToLukkoParams();
        if (params) {
          return lukitse(lukkotyyppi, params, cb);
        } else { // Lukkoa ei tarvita koska kyseessä uusi
          var d = $q.defer();
          d.resolve();
          if (cb) {
            cb();
          }
          return d.promise;
        }
      } else {
        console.warn('Tilalle "' + $state.current.name + '" ei ole määritetty lukkotyyppiä');

        var d = $q.defer();
        d.resolve();
        if (cb) {
          cb();
        }
        return d.promise;
      }
    }

    function genericTarkistaLukitus(success, error) {
      success = success || angular.noop;
      error = error || angular.noop;

      function tarkistaLukitusCb(res) {
        if (res.status !== 404 && res.haltijaOid && new Date() <= new Date(res.vanhentuu) && !res.oma) {
          error(Kaanna.kaanna('lukitus-kayttajalla', {
            user: res.haltijaNimi || res.haltijaOid
          }));
        }
        else {
          success();
        }
      }

      var resource = tilaToLukkoResource();
      if (resource) {
        var params = tilaToLukkoParams();
        resource.get(params, tarkistaLukitusCb, tarkistaLukitusCb);
      }
    }

    function lukitse(Resource, obj, cb, editointiCheck?) {
      if (editointiCheck === undefined) {
        editointiCheck = true;
      }
      var d = $q.defer();
      vapautin = function(vcb) {
        return vapauta(Resource, obj, vcb);
      };
      lukitsin = function() {
        Resource.save(obj, function(res, headers) {
          if (editointiCheck && etag && headers().etag !== etag && Editointikontrollit.getEditMode()) {
            $modal.open({
              templateUrl: 'views/modals/sisaltoMuuttunut.html',
              controller: 'LukittuSisaltoMuuttunutModalCtrl'
            }).result.then(function() {
              etag = headers().etag;
            }, Editointikontrollit.cancelEditing);
          } else {
            etag = headers().etag;
            d.resolve(res);
            if (cb) {
              cb(res, d);
            }
          }
        }, Notifikaatiot.serverLukitus);
      };
      lukitsin();
      return d.promise;
    }

    function vapauta(Resource, obj, cb) {
      var d = $q.defer();
      Resource.remove(obj, function(res) {
        if (cb) {
          cb(res);
        }
        d.resolve(res);
      }, Notifikaatiot.serverLukitus);
      lukitsin = null;
      etag = null;
      return d.promise;
    }

    function lukitseSisalto(id, suoritustapa, cb) {
      return lukitse(LukkoRakenne, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function vapautaSisalto(id, suoritustapa, cb = _.noop) {
      return vapauta(LukkoRakenne, {
        osanId: id,
        suoritustapa: suoritustapa
      }, cb);
    }

    function lukitsePerusteenosa(id, cb) {
      return lukitse(LukkoPerusteenosa, {
        osanId: id
      }, cb);
    }

    function vapautaPerusteenosa(id, cb) {
      return vapauta(LukkoPerusteenosa, {
        osanId: id
      }, cb);
    }

    function lukitsePerusteenosaByTutkinnonOsaViite(id, cb) {
      return lukitse(LukkoPerusteenosaByTutkinnonOsaViite, {
        viiteId: id
      }, cb);
    }

    function vapautaPerusteenosaByTutkinnonOsaViite(id, cb) {
      return vapauta(LukkoPerusteenosaByTutkinnonOsaViite, {
        viiteId: id
      }, cb);
    }

    function tarkistaLukitus(id, scope, suoritustapa) {
      var okCb = function(res) {
        if (res.haltijaOid && new Date() <= new Date(res.vanhentuu) && !res.oma) {
          scope.isLocked = true;
          scope.lockNotification = Kaanna.kaanna('lukitus-kayttajalla', {
            user: res.haltijaNimi || res.haltijaOid
          });
        }
        else {
          scope.isLocked = false;
          scope.lockNotification = '';
        }
      };

      if (suoritustapa) {
        lueLukitus(LukkoRakenne, {osanId: id, suoritustapa: suoritustapa}, okCb);
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
          lueLukitus(LukkoRakenne, { osanId: parametrit.id, suoritustapa: parametrit.suoritustapa }, cb);
          break;
        case 'perusteenosa':
          lueLukitus(LukkoPerusteenosa, { osanId: parametrit.id }, cb);
          break;
        default:
          break;
      }
    }

    function lukitseOppiaine(id, cb) {
      return lukitse(LukkoOppiaine, {perusteId: PerusopetusService.getPerusteId(), osanId: id}, cb);
    }

    function lukitseLukioOppiaine(id, ch) {
      return lukitse(LukkoLukioOppiaine, {perusteId: LukiokoulutusService.getPerusteId(), osanId: id}, ch);
    }

    function vapautaOppiaine(id, cb) {
      return vapauta(LukkoOppiaine, {perusteId: PerusopetusService.getPerusteId(), osanId: id}, cb);
    }

    function vapautaLukioOppiaine(id, cb) {
      return vapauta(LukkoLukioOppiaine, {perusteId: LukiokoulutusService.getPerusteId(), osanId: id}, cb);
    }

    function lukitseLukioKurssi(id, cb, editointiCheck) {
      return lukitse(LukkoLukiokurssi, {perusteId: parseInt(LukiokoulutusService.getPerusteId(),10), kurssiId: id}, cb, editointiCheck);
    }

    function vapautaLukioKurssi(id, cb) {
      return vapauta(LukkoLukiokurssi, {perusteId: parseInt(LukiokoulutusService.getPerusteId(), 10), kurssiId: id}, cb);
    }

    function lukitseLukiorakenne(cb, editointiCheck) {
      return lukitse(LukkoLukioRakenne, {perusteId: parseInt(LukiokoulutusService.getPerusteId(), 10)}, cb, editointiCheck);
    }

    function vapautaLukiorakenne(cb) {
      return vapauta(LukkoLukioRakenne, {perusteId: parseInt(LukiokoulutusService.getPerusteId(), 10)}, cb);
    }

    function lukitseLukioAihekokonaisuus(id, cb) {
      return lukitse(LukkoLukioAihekokonaisuus, {perusteId: LukiokoulutusService.getPerusteId(), aihekokonaisuusId: id}, cb);
    }

    function vapautaLukioAihekokonaisuus(id, cb) {
      return vapauta(LukkoLukioAihekokonaisuus, {perusteId: LukiokoulutusService.getPerusteId(), aihekokonaisuusId: id}, cb);
    }

    function lukitseLukioAihekokonaisuudet(id, ch) {
      return lukitse(LukkoLukioAihekokonaisuudet, {perusteId: LukiokoulutusService.getPerusteId()}, ch);
    }

    function vapautaLukioAihekokonaisuudet(id, ch) {
      return vapauta(LukkoLukioAihekokonaisuudet, {perusteId: LukiokoulutusService.getPerusteId()}, ch);
    }

    function lukitseLukioYleisettavoitteet(id, ch) {
      return lukitse(LukkoLukioYleisetTavoitteet, {perusteId: LukiokoulutusService.getPerusteId()}, ch);
    }

    function vapautaLukioYleisettavoitteet(id, ch) {
      return vapauta(LukkoLukioYleisetTavoitteet, {perusteId: LukiokoulutusService.getPerusteId()}, ch);
    }

    function lukitseOppiaineenVuosiluokkakokonaisuus(oppiaineId, vuosiluokkaId, cb) {
      return lukitse(LukkoOppiaineenVuosiluokkakokonaisuus, {
        perusteId: PerusopetusService.getPerusteId(),
        oppiaineId: oppiaineId,
        vuosiluokkaId: vuosiluokkaId
      }, cb);
    }

    function vapautaOppiaineenVuosiluokkakokonaisuus(oppiaineId, vuosiluokkaId, cb) {
      return vapauta(LukkoOppiaineenVuosiluokkakokonaisuus, {
        perusteId: PerusopetusService.getPerusteId(),
        oppiaineId: oppiaineId,
        vuosiluokkaId: vuosiluokkaId
      }, cb);
    }

    return {
      lukitse: genericLukitse,
      vapauta: genericVapauta,
      hae: hae,
      tarkista: tarkistaLukitus,
      genericTarkista: genericTarkistaLukitus,
      lukitseSisalto: lukitseSisalto,
      vapautaSisalto: vapautaSisalto,
      lukitsePerusteenosa: lukitsePerusteenosa,
      vapautaPerusteenosa: vapautaPerusteenosa,

      lukitseOppiaine: lukitseOppiaine,
      lukitseLukioOppiaine: lukitseLukioOppiaine,
      vapautaOppiaine: vapautaOppiaine,
      lukitseLukioKurssi: lukitseLukioKurssi,
      vapautaLukioKurssi: vapautaLukioKurssi,
      lukitseLukiorakenne: lukitseLukiorakenne,
      vapautaLukiorakenne: vapautaLukiorakenne,
      vapautaLukioOppiaine: vapautaLukioOppiaine,
      lukitseOppiaineenVuosiluokkakokonaisuus: lukitseOppiaineenVuosiluokkakokonaisuus,
      vapautaOppiaineenVuosiluokkakokonaisuus: vapautaOppiaineenVuosiluokkakokonaisuus,
      lukitsePerusteenosaByTutkinnonOsaViite: lukitsePerusteenosaByTutkinnonOsaViite,
      vapautaPerusteenosaByTutkinnonOsaViite: vapautaPerusteenosaByTutkinnonOsaViite,
      lukitseLukioAihekokonaisuus: lukitseLukioAihekokonaisuus,
      vapautaLukioAihekokonaisuus: vapautaLukioAihekokonaisuus,
      lukitseLukioAihekokonaisuudet: lukitseLukioAihekokonaisuudet,
      vapautaLukioAihekokonaisuudet: vapautaLukioAihekokonaisuudet,
      lukitseLukioYleisettavoitteet: lukitseLukioYleisettavoitteet,
      vapautaLukioYleisettavoitteet: vapautaLukioYleisettavoitteet
    };
  });
