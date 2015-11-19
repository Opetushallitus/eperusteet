'use strict';
/* global _ */

/// <reference path="../../ts_packages/tsd.d.ts" />

angular.module('eperusteApp')
  .controller('TutkinnonOsaOsaAlueCtrl', function ($scope, $state, $stateParams, Editointikontrollit,
    TutkinnonOsanOsaAlue, Lukitus, Notifikaatiot, Utils, Koodisto, Kielimapper, YleinenData) {
    $scope.osaamistavoitepuu = [];
    var tempId = 0;

    $scope.isVaTe = YleinenData.isValmaTelma($scope.peruste);
    $scope.vateConverter = Kielimapper.mapTutkinnonosatKoulutuksenosat($scope.isVaTe);

    $scope.tutkinnonOsa = $scope.$parent.tutkinnonOsaViite.tutkinnonOsa;
    $scope.osaAlue = {
      nimi:{},
      kuvaus:{},
    };

    if ($scope.tutkinnonOsa) {
      Koodisto.haeAlarelaatiot($scope.tutkinnonOsa.koodiUri, function(alarelaatiot) {
        alarelaatiot.unshift({});
        $scope.$alarelaatiot = alarelaatiot;
      });
    }

    if ($scope.isVaTe) {
      $scope.$$osaamistavoiteOpen = true;
      $scope.$$osaamisenArviointiOpen = true;
      $scope.osaAlue.valmaTelmaSisalto = {
        osaamistavoitteet: {
          kohde: {},
          kriteerit: [],
          tekstina: {}
        },
        osaamisenArviointi: {
          kuvaus: {},
          kriteerit: [],
          tekstina: {}
        },
      };
    }

    $scope.valitseAlarelaatio = function(ar) {
      $scope.osaAlue.koodiUri = ar ? ar.koodiUri : undefined;
      $scope.osaAlue.koodiArvo = ar ? ar.koodiArvo : undefined;
    };

    function luoOsaamistavoitepuu() {
      if ($scope.osaAlue && $scope.osaAlue.osaamistavoitteet) {
        $scope.osaamistavoitepuu = _($scope.osaAlue.osaamistavoitteet)
          .filter('pakollinen')
          .each(function(o){
            o.$poistettu = false;
          })
          .value();

        _($scope.osaAlue.osaamistavoitteet)
          .filter({pakollinen: false})
          .each(function (r) {
            r.$poistettu = false;
            if (r._esitieto) {
              lisaaOsaamistavoitteelleLapsi(r);
            } else {
              $scope.osaamistavoitepuu.push(r);
            }
          })
          .value();
      }
    }

    function lisaaOsaamistavoitteelleLapsi(lapsi) {
      _.each($scope.osaamistavoitepuu, function (osaamistavoite) {
        if (osaamistavoite.id === parseInt(lapsi._esitieto, 10)) {
          osaamistavoite.lapsi = lapsi;
        }
      });
    }

    $scope.lisaaOsaamistavoite = function () {
      var osaamistavoitePakollinen: any = {
        pakollinen: true,
        nimi: {},
        $open: true,
        $poistettu: false
      };
      var osaamistavoiteValinnainen = {
        pakollinen: false,
        $poistettu: false
      };
      osaamistavoitePakollinen.lapsi = osaamistavoiteValinnainen;
      $scope.osaamistavoitepuu.push(osaamistavoitePakollinen);
    };

    $scope.tuoOsaamistavoite = function () {
      console.log('Toteuta osaamistavoitteen tuonti!');
    };

    $scope.poistaTavoite = function(tavoite) {
      if (tavoite.pakollinen === true) {
        if (_.isObject(tavoite.lapsi)) {
          tavoite.lapsi.nimi = tavoite.nimi;
          tavoite.lapsi.$open = tavoite.$open;
          tavoite.lapsi._esitieto = null;
          $scope.osaamistavoitepuu.push(tavoite.lapsi);
        }
        tavoite.$poistettu = true;
      } else {
        tavoite.$poistettu = true;
      }
    };

    function goBack() {
      $state.go('^', {}, {reload: true});
    }

    var osaAlueCallbacks = {
      edit: function () {
        TutkinnonOsanOsaAlue.get({
          viiteId: $stateParams.tutkinnonOsaViiteId,
          osaalueenId: $stateParams.osaAlueId
        }, function(vastaus) {
          $scope.osaAlue = vastaus;
          luoOsaamistavoitepuu();
        }, function (virhe){
          Notifikaatiot.serverCb(virhe);
          goBack();
        });
      },
      cancel: function () {
        Lukitus.vapautaPerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId);
        goBack();
      },
      save: function () {
        $scope.osaAlue.osaamistavoitteet = kokoaOsaamistavoitteet();

        TutkinnonOsanOsaAlue.save({
          viiteId: $stateParams.tutkinnonOsaViiteId,
          osaalueenId: $stateParams.osaAlueId
        }, $scope.osaAlue, function(res) {
          Lukitus.vapautaPerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId);
          goBack();
        }, function(virhe) {
          Notifikaatiot.serverCb(virhe);
          goBack();
        });
      },
      validate: function () {
        if (!Utils.hasLocalizedText($scope.osaAlue.nimi)) {
          return false;
        }
        else {
          return $scope.isVaTe || _.all($scope.osaamistavoitepuu, function(osaamistavoite) {
            return Utils.hasLocalizedText(osaamistavoite.nimi);
          });
        }
      }
    };

    var kokoaOsaamistavoitteet = function() {
      var osaamistavoitteet = [];
      _.each($scope.osaamistavoitepuu, function(osaamistavoite) {
        if (osaamistavoite.pakollinen && !osaamistavoite.$poistettu) {
          if (!osaamistavoite.id) {
            tempId = (tempId - 1);
            osaamistavoite.id = tempId;
          }
          osaamistavoitteet.push(osaamistavoite);
          if (osaamistavoite.lapsi && !osaamistavoite.lapsi.$poistettu) {
            osaamistavoite.lapsi._esitieto = osaamistavoite.id;
            osaamistavoite.lapsi.nimi = osaamistavoite.nimi;
            if (!osaamistavoite.lapsi.id) {
              tempId = (tempId - 1);
              osaamistavoite.lapsi.id = tempId;
            }
            osaamistavoitteet.push(osaamistavoite.lapsi);
          }
        }
        else if (!osaamistavoite.pakollinen && !osaamistavoite.$poistettu) {
          if (!osaamistavoite.id) {
            tempId = (tempId - 1);
            osaamistavoite.id = tempId;
          }
          osaamistavoitteet.push(osaamistavoite);
        }

      });
      return osaamistavoitteet;
    };

    function lukitse(cb) {
      Lukitus.lukitsePerusteenosaByTutkinnonOsaViite($stateParams.tutkinnonOsaViiteId, cb);
    }

    Editointikontrollit.registerCallback(osaAlueCallbacks);
    lukitse(function () {
      Editointikontrollit.startEditing();
    });

  });
