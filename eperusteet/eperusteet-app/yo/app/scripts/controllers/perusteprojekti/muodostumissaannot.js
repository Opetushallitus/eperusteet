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

angular.module('eperusteApp')
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $stateParams,
    PerusteenRakenne, Notifikaatiot, Editointikontrollit, PerusteProjektiService,
    Kommentit, KommentitBySuoritustapa, Lukitus, VersionHelper, Muodostumissaannot,
    virheService, PerusteProjektiSivunavi, perusteprojektiTiedot, $q, Varmistusdialogi) {
    $scope.editoi = false;
    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {
      $resolved: false,
      rakenne: {osat: []},
      tutkinnonOsat: {}
    };
    $scope.versiot = {};
    $scope.isLocked = false;
    $scope.peruste = perusteprojektiTiedot.getPeruste();

    Kommentit.haeKommentit(KommentitBySuoritustapa, {id: $stateParams.perusteProjektiId, suoritustapa: $scope.suoritustapa});

    function lukitse(cb) {
      Lukitus.lukitseSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa, cb);
    }

    var errorCb = function() {
      virheService.virhe('virhe-perusteenosaa-ei-löytynyt');
    };
    var successCb = function (res) {
      res.$suoritustapa = $scope.suoritustapa;
      res.$resolved = true;
      $scope.rakenne = res;
      Muodostumissaannot.laskeLaajuudet($scope.rakenne.rakenne, $scope.rakenne.tutkinnonOsaViitteet, $scope.rakenne.tutkinnonOsat);
      haeVersiot();
      Lukitus.tarkista($scope.rakenne.$peruste.id, $scope, $scope.suoritustapa);
    };

    function haeRakenne(cb, versio) {
      cb = cb || angular.noop;
      haeVersiot(true, function() {
        var revNumber = VersionHelper.select($scope.versiot, versio);
        if (versio && !$scope.versiot.latest) {
          if (!revNumber) {
            errorCb();
          } else {
            PerusteenRakenne.haeTutkinnonosatByPeruste($scope.peruste.id, $scope.suoritustapa, function(tutkinnonOsat) {
              var vastaus = PerusteenRakenne.pilkoTutkinnonOsat(tutkinnonOsat, {});
              $scope.tutkinnonOsat = vastaus.tutkinnonOsat;
              VersionHelper.changeRakenne($scope.versiot, {id: $scope.peruste.id, suoritustapa: $scope.suoritustapa}, function(response) {
                $scope.rakenne.rakenne = response;
                $scope.rakenne.$resolved = true;
                $scope.rakenne.$suoritustapa = $scope.suoritustapa;
                $scope.rakenne.$peruste = $scope.peruste;
                $scope.rakenne.tutkinnonOsat = vastaus.tutkinnonOsat;
                $scope.rakenne.tutkinnonOsaViitteet = vastaus.tutkinnonOsaViitteet;
                cb();
              });
            });

          }
        } else {
          PerusteenRakenne.haeByPerusteprojekti($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
            successCb(res);
            cb();
          });
        }
      });

    }
    $scope.haeRakenne = haeRakenne;
    var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
    haeRakenne(angular.noop, versio);

    function tallennaRakenne(rakenne) {
      PerusteenRakenne.tallennaRakenne(
        rakenne,
        rakenne.$peruste.id,
        $scope.suoritustapa,
        function() {
          Notifikaatiot.onnistui('tallennus-onnistui');
          haeVersiot(true, function() {
            VersionHelper.setUrl($scope.versiot, true);
          });
          Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        },
        function() {
          Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        }
      );
      $scope.isLocked = false;
    }

    function haeVersiot(force, cb) {
      VersionHelper.getRakenneVersions($scope.versiot, {id: $scope.peruste.id, suoritustapa: $scope.suoritustapa}, force, cb);
    }

    $scope.vaihdaVersio = function(cb) {
      cb = cb || angular.noop;
      $scope.versiot.hasChanged = true;
      // Ideally we would reload the data and rewrite version to url without changing state
      VersionHelper.setUrl($scope.versiot, true);
//      VersionHelper.changeRakenne($scope.versiot, {id: $scope.peruste.id, suoritustapa: $scope.suoritustapa}, function(response) {
//        console.log('cb kutsuttu', response);
//        $scope.rakenne.rakenne = response;
//        $scope.rakenne.$resolved = true;
//        //VersionHelper.setUrl($scope.versiot, true);
//        cb();
//      });
    };

    $scope.revert = function () {
      haeRakenne(function () {
        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        haeVersiot(true);
      });
    };

    $scope.muokkaa = function() {
      lukitse(function() {
        haeRakenne(function() {
          $scope.rakenne.rakenne.$virheetMaara = Muodostumissaannot.validoiRyhma($scope.rakenne.rakenne, $scope.rakenne.tutkinnonOsaViitteet, $scope.tutkinnonOsat);
          Editointikontrollit.startEditing();
          $scope.editoi = true;
        });
      });
    };

    var leikelautaDialogi = function (successCb, failureCb) {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-liikkuminen',
        teksti: 'leikelauta-varoitus',
        lisaTeksti: 'haluatko-jatkaa',
        successCb: successCb,
        failureCb: failureCb || angular.noop,
        primaryBtn: 'poistu-sivulta'
      })();
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.editoi = true;
      },
      asyncValidate: function(cb) {
        lukitse(function() {
          if (Muodostumissaannot.skratchpadNotEmpty()) {
            leikelautaDialogi(cb);
          } else {
            cb();
          }
        });
      },
      save: function(kommentti) {
        $scope.rakenne.rakenne.metadata = { kommentti: kommentti };
        tallennaRakenne($scope.rakenne);
        $scope.editoi = false;
      },
      cancel: function() {
        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        haeRakenne(function() {
          $scope.editoi = false;
        });
      },
      canCancel: function () {
        var deferred = $q.defer();
        if (Muodostumissaannot.skratchpadNotEmpty()) {
          leikelautaDialogi(function () {
            deferred.resolve();
          }, function () {
            deferred.reject();
          });
        } else {
          deferred.resolve();
        }
        return deferred.promise;
      }
    });

    $scope.$watch('rakenne.rakenne', function(uusirakenne) {
      if ($scope.editoi) {
        Muodostumissaannot.laskeLaajuudet(uusirakenne, $scope.rakenne.tutkinnonOsaViitteet);
        uusirakenne.$virheetMaara = Muodostumissaannot.validoiRyhma(uusirakenne, $scope.rakenne.tutkinnonOsaViitteet);
      }
    }, true);

    $scope.$watch('editoi', function(editoi) {
      PerusteProjektiSivunavi.setVisible(!editoi);
    });
  });
