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
        model: '=aihekokonaisuudet'
      },
      controller: 'LukioAihekokonaisuudetController'
    };
  })

  .controller('LukioAihekokonaisuudetController', function ($scope, LukioAihekokonaisuudetService,
                                                            LukiokoulutusService,
                                                            Lukitus,
                                                            PerusteProjektiSivunavi,
                                                            Editointikontrollit,
                                                            $rootScope, $state, $filter) {

    var setEditMode = function() {
      $scope.editEnabled = true;
      $scope.editMode = true;
      PerusteProjektiSivunavi.setVisible(false);
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        Lukitus.lukitseLukioAihekokonaisuudet().then(function() {
          setEditMode();
        });
      },
      save: function() {
        $rootScope.$broadcast('notifyCKEditor');
        LukioAihekokonaisuudetService.saveAihekokonaisuudetYleiskuvaus($scope.aihekokonaisuudet).then(function() {
          Lukitus.vapauta().then(function() {
            init();
          })
        });
      },
      cancel: function() {
        $scope.cancel();
      },
      validate: function() { return $filter('kaanna')($scope.aihekokonaisuudet.otsikko) != ''; },
      notify: function () {
      }
    });

    function init() {
      LukioAihekokonaisuudetService.getAihekokonaisuudetYleiskuvaus().then(function(aihekokonaisuudet) {
        $scope.aihekokonaisuudet = aihekokonaisuudet;
      });
      $scope.editEnabled = false;
      $scope.editMode = false;
      PerusteProjektiSivunavi.setVisible(true);
    }

    init();
    $scope.edit = function() {
      Editointikontrollit.startEditing();
    };

    $scope.gotoEditAihekokonaisuus = function(aihekokonaisuusId) {
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue',
        {osanTyyppi: LukiokoulutusService.AIHEKOKONAISUUDET,
          osanId: aihekokonaisuusId,
          tabId: 0,
          editEnabled: true});
    }

    $scope.cancel = function() {
      Lukitus.vapauta().then( function() {
          init();
        }
      );
    };


  })
  .directive('lukioMuokkausAihekokonaisuus', function() {
    return {
      templateUrl: 'views/directives/lukiokoulutus/aihekokonaisuus.html',
      restrict: 'E',
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'LukioAihekokonaisuusController'
    };
  })

  .controller('LukioAihekokonaisuusController', function ($scope,
                                                          $state,
                                                          $stateParams,
                                                          Lukitus,
                                                          LukioAihekokonaisuudetService,
                                                          PerusteProjektiSivunavi,
                                                          LukiokoulutusService,
                                                          Editointikontrollit,
                                                          $rootScope, $filter) {

    var setEditMode = function() {
      $scope.editEnabled = true;
      PerusteProjektiSivunavi.setVisible(false);
    }

    Editointikontrollit.registerCallback({
      edit: function() {
        if( !$scope.isNew ) {
          Lukitus.lukitseLukioAihekokonaisuus($scope.aihekokonaisuus.id).then(function() {
            setEditMode();
          });
        }

      },
      save: function() {
        $rootScope.$broadcast('notifyCKEditor');

        if( $scope.isNew ) {
          LukioAihekokonaisuudetService.saveAihekokonaisuus($scope.aihekokonaisuus).then(function(aihekokonaisuus) {
            Lukitus.vapauta();
            if($stateParams.editEnabled) {
              $scope.back();
            } else {
              $state.go('root.perusteprojekti.suoritustapa.lukioosaalue',
                {osanTyyppi: LukiokoulutusService.AIHEKOKONAISUUDET,
                  osanId: aihekokonaisuus.id,
                  tabId: 0,
                  editEnabled: false});
            }
          });
        } else {
          LukioAihekokonaisuudetService.updateAihekokonaisuus($scope.aihekokonaisuus).then(function() {
            Lukitus.vapauta();
            if($stateParams.editEnabled) {
              $scope.back();
            } else {
              init();
            }
          });
        }
      },
      cancel: function() {
        Lukitus.vapauta();
        $scope.cancel();
      },
      validate: function() { return $filter('kaanna')($scope.aihekokonaisuus.otsikko) != ''; },
      notify: function () {
      }
    });


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
      Editointikontrollit.startEditing();
    } else {
      init();
      if( $stateParams.editEnabled === 'true' ) {
        $scope.editEnabled = true;
        PerusteProjektiSivunavi.setVisible(false);
        Editointikontrollit.startEditing();
      } else {
        $scope.editEnabled = false;
      }
    }

    $scope.edit = function() {
      Editointikontrollit.startEditing();
    };

    $scope.cancel = function() {
      if( $scope.isNew || $stateParams.editEnabled ) {
        $scope.back();
      } else {
        init();
      }
    };

    $scope.back = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: LukiokoulutusService.AIHEKOKONAISUUDET});
    };

    $scope.delete = function() {
      LukioAihekokonaisuudetService.deleteAihekokonaisuus($scope.aihekokonaisuus.id).then(function() {
        $scope.back();
      });
    };

  });
