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
  .directive('osallinenOsa', function ($compile) {
    return {
      templateUrl: 'views/directives/osallinenosa.html',
      restrict: 'AE',
      transclude: true,
      scope: {
        model: '=',
        config: '=',
        versiot: '=',
        editableModel: '='
      },
      controller: 'OsallinenOsaController',
      link: function (scope, element) {
        var el = $compile(angular.element(scope.config.fieldRenderer))(scope);
        element.find('.tutkinnonosa-sisalto').empty().append(el);
      }
    };
  })

  .controller('OsallinenOsaController', function ($scope, $state, VersionHelper, $q,
      Editointikontrollit, FieldSplitter, Varmistusdialogi, $rootScope, Utils, $timeout) {
    $scope.isLocked = false;
    $scope.isNew = false;
    $scope.editEnabled = false;

    function refreshPromise() {
      var deferred = $q.defer();
      $scope.modelPromise = deferred.promise;
      deferred.resolve($scope.editableModel);
    }
    refreshPromise();

    $scope.isPublished = function () {
      return $scope.model.tila === 'julkaistu';
    };

    $scope.canAdd = function () {
      return true;
    };

    $scope.generateBackHref = function () {
      if ($scope.config && $scope.config.backState) {
        return $state.href.apply($state, $scope.config.backState);
      }
    };

    $scope.vaihdaVersio = function () {
      $scope.versiot.hasChanged = true;
      VersionHelper.setUrl($scope.versiot);
      //VersionHelper.changePerusteenosa($scope.versiot, {id: $scope.tutkinnonOsa.id}, responseFn);
    };

    $scope.revertCb = function (/*response*/) {
      // TODO
      //responseFn(response);
      //saveCb(response);
    };

    if ($scope.config.editingCallbacks) {
      Editointikontrollit.registerCallback($scope.config.editingCallbacks);
      Editointikontrollit.registerEditModeListener(function (mode) {
        $scope.editEnabled = mode;
      });
    }

    $scope.muokkaa = function () {
      // TODO lukitus
      Editointikontrollit.startEditing();
    };

    $scope.addField = function (field) {
      var splitfield = FieldSplitter.process(field);
      var cssClass;
      if (splitfield.isMulti()) {
        var index = splitfield.addArrayItem($scope.editableModel);
        $rootScope.$broadcast('osafield:update');
        cssClass = splitfield.getClass(index);
        field.$setEditable = index;
      } else {
        field.visible = true;
        field.$added = true;
        cssClass = FieldSplitter.getClass(field);
      }
      ($scope.config.addFieldCb || angular.noop)(field);
      $timeout(function () {
        Utils.scrollTo('li.' + cssClass);
      }, 200);
    };

    $scope.removeWhole = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        teksti: $scope.config.removeWholeConfirmationText || '',
        primaryBtn: 'poista',
        successCb: function () {
          Editointikontrollit.cancelEditing();
          $scope.config.removeWholeFn();
        }
      })();
    };

    $scope.$watch('editableModel', refreshPromise);
 });
