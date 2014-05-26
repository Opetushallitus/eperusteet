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
/*global _*/

angular.module('eperusteApp')
  .controller('MuokkausCtrl', function($scope, $stateParams, $state, $compile, Navigaatiopolku, PerusteenOsat) {

    $scope.tyyppi = $stateParams.perusteenOsanTyyppi;
    $scope.objekti = null;

    if ($stateParams.perusteenOsaId !== 'uusi') {
      $scope.objekti = PerusteenOsat.get({ osanId: $stateParams.perusteenOsaId }, function(re) {
        Navigaatiopolku.asetaElementit({ perusteenOsaId: re.nimi });
      }, function() {
        console.log('unable to find perusteen osa #' + $stateParams.perusteenId);
        $state.go('aloitussivu');
      });
    } else {
      Navigaatiopolku.asetaElementit({ perusteenOsaId: 'uusi' });
    }

    var muokkausDirective = null;
    if ($stateParams.perusteenOsanTyyppi === 'tekstikappale') {
      muokkausDirective = angular.element('<muokkaus-tekstikappale tekstikappale="objekti"></muokkaus-tekstikappale>');
    } else if ($stateParams.perusteenOsanTyyppi === 'tutkinnonosa') {
      muokkausDirective = angular.element('<muokkaus-tutkinnonosa tutkinnon-osa="objekti"></muokkaus-tutkinnonosa>');
    } else {
      console.log('invalid perusteen osan tyyppi');
      $state.go('aloitussivu');
    }
    var el = $compile(muokkausDirective)($scope);

    angular.element('#muokkaus-elementti-placeholder').replaceWith(el);


  })
  .service('MuokkausUtils', function() {
    this.hasValue = function(obj, path) {
      if (this.nestedHas(obj, path, '.')) {
        if (angular.isString(this.nestedGet(obj, path, '.'))) {
          if(this.nestedGet(obj, path, '.').length > 0) {
            return true;
          } else {
            return false;
          }
        } else if(!angular.isUndefined(this.nestedGet(obj, path, '.')) && this.nestedGet(obj, path, '.') !== null) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    };

    this.nestedHas = function(obj, path, delimiter) {

      function innerNestedHas(obj, names) {
        if(_.has(obj, names[0])) {
          return names.length > 1 ? innerNestedHas(obj[names[0]], names.splice(1, names.length)) : true;
        } else {
          return false;
        }
      }

      var propertyNames = path.split(delimiter);

      return innerNestedHas(obj, propertyNames);
    };

    this.nestedGet = function(obj, path, delimiter) {
      function innerNestedGet(obj, names) {
        if(names.length > 1) {
          return innerNestedGet(obj[names[0]], names.splice(1, names.length));
        } else {
          return obj[names[0]];
        }
      }

      if(!this.nestedHas(obj, path, delimiter)) {
        return undefined;
      }
      var propertyNames = path.split(delimiter);

      return innerNestedGet(obj, propertyNames);
    };

    this.nestedSet = function(obj, path, delimiter, value) {

      function innerNestedSet(obj, names, newValue) {
        if(names.length > 1) {
          if(!_.has(obj, names[0])) {
            obj[names[0]] = {};
          }
          innerNestedSet(obj[names[0]], names.splice(1, names.length), newValue);
        }  else {
          obj[names[0]] = newValue;
        }
      }

      var propertyNames = path.split(delimiter);

      innerNestedSet(obj, propertyNames, value);
    };

    this.nestedOmit = function(obj, path, delimiter) {

      function innerNestedOmit(obj, names) {
        if(names.length > 1) {
          obj[names[0]] = innerNestedOmit(obj[names[0]], names.splice(1, names.length));
          return obj;
        } else {
          return _.omit(obj, names[0]);
        }
      }

      var propertyNames = path.split(delimiter);

      return innerNestedOmit(obj, propertyNames);
    };
  });
