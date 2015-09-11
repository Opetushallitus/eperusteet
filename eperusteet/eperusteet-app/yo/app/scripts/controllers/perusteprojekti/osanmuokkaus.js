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
  .service('OsanMuokkausHelper', function ($q, $stateParams, PerusopetusService, $state, Lukitus) {
    var vuosiluokat = [];
    var model = null;
    var isLocked = false;
    var backState = null;
    var vuosiluokka = null;
    var path = null;
    var oppiaine = null;
    var osaamiset = null;

    function reset() {
      backState = null;
      vuosiluokka = null;
      osaamiset = null;
      path = null;
      oppiaine = null;
    }

    function getModel() {
      return path ? model[path] : model;
    }

    function setup(uusiModel, uusiPath, uusiOppiaine, cb) {
      cb = cb || angular.noop;
      oppiaine = $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ? uusiModel : null;
      if (uusiOppiaine) {
        oppiaine = uusiOppiaine;
      }
      model = uusiModel;
      path = uusiPath;
      isLocked = false;
      backState = [$state.current.name, _.clone($stateParams)];

      $q.all([
        PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT, true),
        PerusopetusService.getOsat(PerusopetusService.OSAAMINEN, true)
      ]).then(function(res) {
        vuosiluokat = res[0];
        osaamiset = res[1];
        vuosiluokka = uusiModel._vuosiluokkaKokonaisuus ? _.find(vuosiluokat, function(vl) {
          return vl.id === parseInt(model._vuosiluokkaKokonaisuus, 10);
        }) : null;
        if (isVuosiluokkakokonaisuudenOsa()) {
          Lukitus.lukitseOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function () {
            isLocked = true;
            cb();
          });
        } else if (oppiaine) {
          Lukitus.lukitseOppiaine(oppiaine.id, function() {
            isLocked = true;
            cb();
          });
        }
      });
    }

    function save() {
      if (isVuosiluokkakokonaisuudenOsa()) {
        PerusopetusService.saveVuosiluokkakokonaisuudenOsa(model, oppiaine, function () {
          Lukitus.vapautaOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function () {
            isLocked = false;
            goBack();
          });
        });
      } else if (path) {
        var payload = _.pick(model, ['id', path]);
        PerusopetusService.saveOsa(payload, backState[1], function () {
          if (isLocked && oppiaine) {
            Lukitus.vapautaOppiaine(oppiaine.id, function () {
              isLocked = false;
              goBack();
            });
          }
        });
      }
    }

    function goBack() {
      if (!backState) {
        return;
      }

      if (isLocked) {
        if (isVuosiluokkakokonaisuudenOsa()) {
          Lukitus.vapautaOppiaineenVuosiluokkakokonaisuus(oppiaine.id, model.id, function () {
            isLocked = false;
          });
        } else if (oppiaine) {
          Lukitus.vapautaOppiaine(oppiaine.id, function () {
            isLocked = false;
          });
        }
      }
      var params = _.clone(backState);
      reset();
      $state.go.apply($state, params, {reload: true});
    }

    function isVuosiluokkakokonaisuudenOsa() {
      return !!vuosiluokka;
    }

    function getOsaamiset() {
      return osaamiset;
    }

    function getOppiaine() {
      return oppiaine;
    }

    function getOppiaineenVuosiluokkakokonaisuus() {
      if (oppiaine && vuosiluokka) {
        return _.find(oppiaine.vuosiluokkakokonaisuudet, function(ovlk) {
          return vuosiluokka.id === _.parseInt(ovlk._vuosiluokkaKokonaisuus);
        });
      }
    }

    function getVuosiluokkakokonaisuus() {
      return vuosiluokka;
    }

    return {
      reset: reset,
      getModel: getModel,
      setup: setup,
      save: save,
      goBack: goBack,
      getOppiaine: getOppiaine,
      getOsaamiset: getOsaamiset,
      getVuosiluokkakokonaisuus: getVuosiluokkakokonaisuus,
      getOppiaineenVuosiluokkakokonaisuus: getOppiaineenVuosiluokkakokonaisuus,
      isVuosiluokkakokonaisuudenOsa: isVuosiluokkakokonaisuudenOsa
    };
  })

  .controller('OsanMuokkausController', function($scope, $stateParams, $compile, OsanMuokkausHelper,
      Editointikontrollit, $rootScope) {
    $scope.objekti = OsanMuokkausHelper.getModel();

    if (!$scope.objekti) {
      return;
    }

    var MAPPING = {
      tekstikappale: {
        directive: 'osanmuokkaus-tekstikappale',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            $rootScope.$broadcast('notifyCKEditor');
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      sisaltoalueet: {
        directive: 'osanmuokkaus-sisaltoalueet',
        attrs: {
          model: 'objekti'
        },
        callbacks: {
          save: function () {
            $rootScope.$broadcast('notifyCKEditor');
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      kohdealueet: {
        directive: 'osanmuokkaus-kohdealueet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            OsanMuokkausHelper.save();
          },
          edit: function () {},
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      },
      tavoitteet: {
        directive: 'osanmuokkaus-tavoitteet',
        attrs: {
          model: 'objekti',
        },
        callbacks: {
          save: function () {
            OsanMuokkausHelper.save();
          },
          edit: function() {
            console.log('wat');
          },
          cancel: function () {
            OsanMuokkausHelper.goBack();
          },
        }
      }
    };

    var config = MAPPING[$stateParams.osanTyyppi];
    $scope.config = config;
    var muokkausDirective = angular.element('<' + config.directive + '>').attr('config', 'config');
    _.each(config.attrs, function (value, key) {
      muokkausDirective.attr(key, value);
    });
    var el = $compile(muokkausDirective)($scope);

    angular.element('#muokkaus-elementti-placeholder').replaceWith(el);

    Editointikontrollit.registerCallback(config.callbacks);
    Editointikontrollit.startEditing();
    // TODO muokkauksesta poistumisvaroitus
  })

  .directive('osanmuokkausTekstikappale', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaustekstikappale.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '=',
        editMode: '@'
      },
      controller: 'OsanmuokkausTekstikappaleController',
      link: function (scope, element, attrs) {
        attrs.$observe('editMode', function (val) {
          scope.editMode = val !== 'false';
        });
        scope.editMode = attrs.editMode !== 'false';
      }
    };
  })

  .controller('OsanmuokkausTekstikappaleController', function ($scope, OsanMuokkausHelper, $rootScope,
      YleinenData, $state, $stateParams) {
    $scope.valitseKieli = _.bind(YleinenData.valitseKieli, YleinenData);
    $scope.getTitle = function () {
      return OsanMuokkausHelper.isVuosiluokkakokonaisuudenOsa() ?
        'muokkaus-vuosiluokkakokonaisuuden-osa' :
        ($scope.model.$isNew ? 'luonti-tekstikappale' : 'muokkaus-tekstikappale');
    };
    // Odota tekstikenttien alustus ja päivitä editointipalkin sijainti
    var received = 0;
    $scope.$on('ckEditorInstanceReady', function() {
      if (++received === 2) {
        $rootScope.$broadcast('editointikontrollitRefresh');
      }
    });

    $scope.edit = function () {
      OsanMuokkausHelper.setup($scope.model);
      $state.go('root.perusteprojekti.suoritustapa.muokkaus', $stateParams);
    };
  })

  .directive('osanmuokkausKohdealueet', function () {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkauskohdealueet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      },
      controller: function($scope) {
        $scope.model = $scope.model || [];
        $scope.poistaKohdealue = _.partial(_.remove, $scope.model);
        $scope.lisaaKohdealue = function() {
          $scope.model.push({ nimi: { fi: '' } });
        };
      }
    };
  })

  .directive('osanmuokkausTavoitteet', function() {
    return {
      templateUrl: 'views/directives/perusopetus/osanmuokkaustavoitteet.html',
      restrict: 'E',
      scope: {
        model: '=',
        config: '='
      },
      controller: function($rootScope, $scope, $modal, ProxyService, $q, Notifikaatiot, OsanMuokkausHelper, $document) {
        function uudetKohdealueetCb(kohdealueet) {
          OsanMuokkausHelper.getOppiaine().kohdealueet = kohdealueet;
          $rootScope.$broadcast('update:oppiaineenkohdealueet');
        }

        function clickHandler(event) {
          var ohjeEl = angular.element(event.target).closest('.popover, .popover-element');
          if (ohjeEl.length === 0) {
            $rootScope.$broadcast('ohje:closeAll');
          }
        }
        $document.on('click', clickHandler);
        $scope.$on('$destroy', function () {
          $document.off('click', clickHandler);
        });

        $scope.muokkaaKohdealueita = function() {
          $modal.open({
            templateUrl: 'views/directives/perusopetus/osanmuokkauskohdealueet.html',
            controller: function($scope, $modalInstance, Oppiaineet, OsanMuokkausHelper) {
              $scope.kohdealueet = _.map(_.clone(OsanMuokkausHelper.getOppiaine().kohdealueet) || [], function(ka) {
                ka.$vanhaNimi = _.clone(ka.nimi);
                return ka;
              });
              $scope.poistaKohdealue = function(ka) {
                Oppiaineet.poistaKohdealue({
                  perusteId: ProxyService.get('perusteId'),
                  osanId: OsanMuokkausHelper.getOppiaine().id,
                  kohdealueId: ka.id
                }, function() {
                  _.remove($scope.kohdealueet, ka);
                }, Notifikaatiot.serverCb);
              };
              $scope.lisaaKohdealue = function() {
                Oppiaineet.lisaaKohdealue({
                  perusteId: ProxyService.get('perusteId'),
                  osanId: OsanMuokkausHelper.getOppiaine().id
                }, {}, function(res) {
                  $scope.kohdealueet.push(res);
                });
              };
              $scope.ok = function(kohdealueet) {
                $q.all(_(kohdealueet).reject(function(ka) {
                    return _.isEqual(ka.nimi, ka.$vanhaNimi);
                  })
                  .map(function(ka) {
                    return Oppiaineet.lisaaKohdealue({
                      perusteId: ProxyService.get('perusteId'),
                      osanId: OsanMuokkausHelper.getOppiaine().id
                    }, ka).$promise;
                  })
                  .value())
                .then(Oppiaineet.kohdealueet({
                      perusteId: ProxyService.get('perusteId'),
                      osanId: OsanMuokkausHelper.getOppiaine().id
                  }, $modalInstance.close)
                );
              };
            }
          })
          .result.then(uudetKohdealueetCb);
        };
      }
    };
  })

  .directive('perusopetusMuokkausInfo', function (OsanMuokkausHelper) {
    return {
      templateUrl: 'views/directives/perusopetus/muokkausinfo.html',
      restrict: 'AE',
      link: function (scope, element, attrs) {
        scope.muokkausinfoOsa = attrs.osa || '';
      },
      controller: function ($scope) {
        $scope.getOppiaine = function () {
          var oppiaine = OsanMuokkausHelper.getOppiaine();
          return oppiaine ? oppiaine.nimi : '';
        };
        $scope.getVuosiluokkakokonaisuus = function () {
          var vlk = OsanMuokkausHelper.getVuosiluokkakokonaisuus();
          return vlk ? vlk.nimi : '';
        };
      }
    };
  });
