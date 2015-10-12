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
      scope: {
        model: '=',
        versiot: '='
      },
      controller: 'LukioTavoitteetController'
    };
  })
  .controller('LukioTavoitteetController', function ($scope, LukioYleisetTavoitteetService,
                                                     PerusteProjektiSivunavi,
                                                     Editointikontrollit,
                                                     $rootScope, $filter) {


    Editointikontrollit.registerCallback({
      edit: function() {
      },
      save: function() {
        $rootScope.$broadcast('notifyCKEditor');
        LukioYleisetTavoitteetService.updateYleistTavoitteet($scope.yleisetTavoitteet).then(function() {
          init();
        });
      },
      cancel: function() {
        $scope.cancel();
      },
      validate: function() { return $filter('kaanna')($scope.yleisetTavoitteet.otsikko) != ''; },
      notify: function () {
      }
    });

    function init() {
      LukioYleisetTavoitteetService.getYleisetTavoitteet().then(function(yleisetTavoitteet) {
        $scope.yleisetTavoitteet = yleisetTavoitteet;
      });

      $scope.editEnabled = false;
      $scope.editMode = false;
      PerusteProjektiSivunavi.setVisible(true);
    }

    init();
    $scope.edit = function() {
      PerusteProjektiSivunavi.setVisible(false);
      $scope.editEnabled = true;
      $scope.editMode = true;
      Editointikontrollit.startEditing();
    };

    $scope.save = function() {
    };

    $scope.cancel = function() {
      init();
    };

  });
