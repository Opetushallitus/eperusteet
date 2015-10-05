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

  // ------------------------------------------------------------------------------------------------------------------
  //    LUKIO Aihekokonaisuudet
  // ------------------------------------------------------------------------------------------------------------------

  .directive('lukioMuokkausAihekokonaisuudet', function() {
    return {
      templateUrl: 'views/directives/lukiokoulutus/aihekokonaisuudet.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'LukioAihekokonaisuudetController'
    };
  })

  .controller('LukioAihekokonaisuudetController', function ($scope, LukioAihekokonaisuudetService,
                                                            PerusteProjektiSivunavi) {
    function init() {
      LukioAihekokonaisuudetService.getAihekokonaisuudetYleiskuvaus().then(function(aihekokonaisuudet) {
        $scope.aihekokonaisuudet = aihekokonaisuudet;
      });
      $scope.editEnabled = false;
      PerusteProjektiSivunavi.setVisible(true);
    }

    init();
    $scope.editEnabled = false;
    $scope.edit = function() {
      $scope.editEnabled = true;
      PerusteProjektiSivunavi.setVisible(false);
    };

    $scope.save = function() {
      LukioAihekokonaisuudetService.saveAihekokonaisuudetYleiskuvaus($scope.aihekokonaisuudet).then(function() {
        init();
      });
    };

    $scope.cancel = function() {
      init();
    };


  })
  .directive('lukioMuokkausTavoitteet', function() {
    return {
      templateUrl: 'views/directives/lukiokoulutus/tavoitteet.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'LukioTavoitteetController'
    };
  })
  .controller('LukioTavoitteetController', function ($scope, $log) {

    $log.info('LukioTavoitteetController - kesken');


  })

  .directive('lukioMuokkausAihekokonaisuus', function() {
    return {
      templateUrl: 'views/directives/lukiokoulutus/aihekokonaisuus.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'LukioAihekokonaisuusController',
      link: function (scope, element) {
        scope.$watch('editEnabled', function (value) {
          if (!value) {
            element.find('.info-placeholder').hide();
          }
        });
      }
    };
  })

  .controller('LukioAihekokonaisuusController', function ($scope,
                                                          $state,
                                                          $stateParams,
                                                          LukioAihekokonaisuudetService,
                                                          PerusteProjektiSivunavi,
                                                          LukiokoulutusService) {

    function init() {
      LukiokoulutusService.getOsa($stateParams).then(function(aihekokonaisuus) {
        $scope.aihekokonaisuus = aihekokonaisuus;
      });
      $scope.editEnabled = false;
      PerusteProjektiSivunavi.setVisible(true);
    }

    $scope.isNew = false;
    $scope.editEnabled = false;
    $scope.versiot = {latest: true};
    $scope.aihekokonaisuus = {};

    if( $stateParams.osanId === 'uusi') {
      $scope.editEnabled = true;
      $scope.isNew = true;
      PerusteProjektiSivunavi.setVisible(false);
    } else {
      init();
    }

    $scope.edit = function() {
      $scope.editEnabled = true;
      PerusteProjektiSivunavi.setVisible(false);
    };

    $scope.cancel = function() {
      if( $scope.isNew ) {
        $scope.back();
      } else {
        init();
      }
    };

    $scope.save = function() {
      LukioAihekokonaisuudetService.saveAihekokonaisuus($scope.aihekokonaisuus).then(function() {
        $scope.back();
      });
    };

    $scope.update = function() {
      LukioAihekokonaisuudetService.updateAihekokonaisuus($scope.aihekokonaisuus).then(function() {
        $scope.back();
      });
    };

    $scope.back = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: LukiokoulutusService.AIHEKOKONAISUUDET});
    };
  });
