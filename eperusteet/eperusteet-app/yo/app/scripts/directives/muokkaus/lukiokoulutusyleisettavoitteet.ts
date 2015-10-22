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

  //LUKIO Opetuksen yleiset tavoitteet
  .directive('lukioMuokkausTavoitteet', function() {
    return {
      templateUrl: 'views/directives/lukiokoulutus/tavoitteet.html',
      restrict: 'E',
      controller: 'LukioTavoitteetController'
    };
  })
  .controller('LukioTavoitteetController', function ($scope, LukioYleisetTavoitteetService,
                                                     Lukitus,
                                                     PerusteProjektiSivunavi,
                                                     LukiokoulutusService,
                                                     virheService,
                                                     Notifikaatiot,
                                                     Editointikontrollit, VersionHelper,
                                                     $rootScope, $filter, $stateParams) {

    $scope.versiot = {};
    $scope.perusteId = LukiokoulutusService.getPerusteId();

    var setEditMode = function() {
      PerusteProjektiSivunavi.setVisible(false);
      $scope.editEnabled = true;
      $scope.editMode = true;

    }

    Editointikontrollit.registerCallback({
      edit: function() {
        Lukitus.lukitseLukioYleisettavoitteet().then(function() {
          setEditMode();
        });

      },
      save: function(kommentti) {
        $rootScope.$broadcast('notifyCKEditor');
        $scope.yleisetTavoitteet.metadata = {kommentti: kommentti};
        LukioYleisetTavoitteetService.updateYleistTavoitteet($scope.yleisetTavoitteet).then(function() {
          Lukitus.vapauta();
          init();
        });
      },
      cancel: function() {
        Lukitus.vapauta();
        $scope.cancel();
      },
      validate: function() { return $filter('kaanna')($scope.yleisetTavoitteet.otsikko) != ''; },
      notify: function () {
      }
    });

    $scope.haeVersiot = function (force, cb) {
      VersionHelper.getLukioYleisetTavoitteetVersions($scope.versiot, {id: LukiokoulutusService.getPerusteId()},force, cb);
    };

    function init() {
      var versio = $stateParams.versio ? $stateParams.versio.replace(/\//g, '') : null;
      if(versio) {
        VersionHelper.getLukioYleisetTavoitteetVersions($scope.versiot, {id: LukiokoulutusService.getPerusteId()},true, function(versiot) {
          var revNumber = VersionHelper.select($scope.versiot, versio);
          LukioYleisetTavoitteetService.getYleisetTavoitteet(revNumber).then(function(yleisetTavoitteet) {
            $scope.yleisetTavoitteet = yleisetTavoitteet;
          });

        });
      } else {
        $scope.haeVersiot(true);
        LukioYleisetTavoitteetService.getYleisetTavoitteet().then(function(yleisetTavoitteet) {
          $scope.yleisetTavoitteet = yleisetTavoitteet;
        });
      }

      $scope.editEnabled = false;
      $scope.editMode = false;
      PerusteProjektiSivunavi.setVisible(true);
    }

    init();
    $scope.edit = function() {
      if( _.isEmpty($scope.yleisetTavoitteet.otsikko) ) {
        $scope.yleisetTavoitteet.otsikko = {"fi": "Opetuksen yleiset tavoitteet"};
      }
      Editointikontrollit.startEditing();
    };

    $scope.cancel = function() {
      init();
    };

    $scope.vaihdaVersio = function () {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
    };

    $scope.revertCb = function (response) {
      Lukitus.vapauta();
      $scope.haeVersiot(true, function () {
        VersionHelper.setUrl($scope.versiot);
      });
      Notifikaatiot.onnistui('aihekokonaisuudet-palautettu');

    };

  });
