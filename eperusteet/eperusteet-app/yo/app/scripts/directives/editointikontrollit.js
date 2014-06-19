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
  .directive('editointikontrollit', function($window) {
    return {
      templateUrl: 'views/partials/editointikontrollit.html',
      restrict: 'E',
      link: function(scope) {
        var window = angular.element($window),
            container = angular.element('.edit-controls'),
            wrapper = angular.element('.editointi-wrapper');

        /**
         * Editointipalkki asettuu staattisesti footerin päälle kun skrollataan
         * tarpeeksi alas. Ylempänä editointipalkki kelluu.
         */
        scope.updatePosition = function () {
          if (window.scrollTop() + window.innerHeight() < wrapper.offset().top + container.height()) {
            container.addClass('floating');
            container.removeClass('static');
            container.css('width', wrapper.width());
          } else {
            container.removeClass('floating');
            container.addClass('static');
            container.css('width', '100%');
          }
        };

        window.on('scroll resize', function() {
          if (scope.editStarted) {
            scope.updatePosition();
          }
        });

        scope.updatePosition();
      }
    };
  })
  .controller('EditointiCtrl', function($scope, $rootScope, Editointikontrollit, $timeout) {

    $scope.kommentti = '';
    $scope.hideControls = true;
    function setEditControls() {
      if(Editointikontrollit.editingEnabled()) {
        $scope.hideControls = false;
      } else {
        $scope.hideControls = true;
        $scope.editStarted = false;
      }
    }

    setEditControls();

    $scope.$on('$stateChangeSuccess', function() {
      Editointikontrollit.unregisterCallback();
      setEditControls();
    });

    Editointikontrollit.registerCallbackListener(setEditControls);

    $scope.$on('enableEditing', function () {
      $scope.editStarted = true;
      $scope.kommentti = '';
      $timeout(function () {
        $scope.updatePosition();
      });
    });
    $scope.$on('disableEditing', function () {
      $scope.editStarted = false;
    });

    $scope.start = function() {
      Editointikontrollit.startEditing();
    };
    $scope.save = function() {
      Editointikontrollit.saveEditing();
    };
    $scope.cancel = function() {
      Editointikontrollit.cancelEditing();
    };
  });
