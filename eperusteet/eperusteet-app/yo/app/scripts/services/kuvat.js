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

// FIXME
angular.module('eperusteApp')
  .service('EpImageService', function ($q, Upload, SERVICE_LOC, PerusteprojektiTiedotService) {
    var pts = null;
    PerusteprojektiTiedotService.then(function(resolved) { pts = resolved; });

    this.getAll = function () {
      console.log('getting all');
      return [];
      // return OpsinKuvat.query({opsId: OpsService.getId()}).$promise;
    };

    this.save = function (image) {
      var deferred = $q.defer();
      var url = (SERVICE_LOC + '/perusteet/' + pts.getPeruste().id + '/kuvat');
      var data = {
        url: url,
        fields: {
          nimi: image.name
        },
        file: image,
      };

      Upload.upload(data)
        .success(deferred.resolve)
        .error(deferred.reject);
      return deferred.promise;
    };

    this.getUrl = function (image) {
      console.log('Getting url');
      return '';
      // return (SERVICE + '/kuvat').replace(':opsId', '' + OpsService.getId()) + '/' + image.id;
    };
  })

  .controller('EpImagePluginController', function ($scope, EpImageService, Kaanna, Algoritmit, $timeout) {
    $scope.service = EpImageService;
    $scope.filtered = [];
    $scope.images = [];
    $scope.showPreview = false;
    $scope.model = {
      files: [],
      rejected: [],
      chosen: null
    };

    $scope.$watch('model.files[0]', function () {
      if (_.isArray($scope.model.files) && $scope.model.files.length > 0) {
        $scope.showPreview = true;
      }
    });
    $scope.$watch('model.chosen', function () {
      $scope.showPreview = false;
    });

    var callback = angular.noop;
    var setDeferred = null;

    function setChosenValue (value) {
      var found = _.find($scope.images, function (image) {
        return image.id === value;
      });
      $scope.model.chosen = found || null;
    }

    function doSort(items) {
      return _.sortBy(items, function (item) {
        return Kaanna.kaanna(item.nimi).toLowerCase();
      });
    }

    $scope.urlForImage = function (image) {
      return $scope.service.getUrl(image);
    };

    $scope.init = function () {
      $scope.service.getAll().then(function (res) {
        $scope.images = res;
        $scope.filtered = doSort(res);
        if (setDeferred) {
          setChosenValue(_.cloneDeep(setDeferred));
          setDeferred = null;
        }
      });
    };

    $scope.filterImages = function (value) {
      $scope.filtered = _.filter(doSort($scope.images), function (item) {
        return Algoritmit.match(value, item.nimi);
      });
    };

    // data from angular model to plugin
    $scope.registerListener = function (cb) {
      callback = cb;
    };
    $scope.$watch('model.chosen', function (value) {
      callback(value);
    });

    // data from plugin to angular model
    $scope.setValue = function (value) {
      $scope.$apply(function () {
        if (_.isEmpty($scope.images)) {
          setDeferred = value;
        } else {
          setChosenValue(value);
        }
      });
    };

    $scope.closeMessage = function () {
      $scope.message = null;
    };

    $scope.saveNew = function () {
      var image = $scope.model.files[0];
      $scope.service.save(image).then(function (res) {
        console.log(res);
        $scope.message = 'epimage-plugin-tallennettu';
        $scope.model.files = [];
        $timeout(function () {
          $scope.closeMessage();
        }, 8000);
        setDeferred = _.clone(res);
        $scope.init();
      }, function (res) {
        console.log(res);
        $scope.message = res.syy || 'epimage-plugin-tallennusvirhe';
        $scope.model.files = [];
        $timeout(function () {
          $scope.closeMessage();
        }, 8000);
      });
    };

  })
  .filter('kuvalinkit', function(EpImageService) {
    return function(text) {
      var modified = false;
      var tmp = angular.element('<div>'+text+'</div>');
      tmp.find('img[data-uid]').each(function () {
            var el = angular.element(this);
            var url = EpImageService.getUrl({id: el.attr('data-uid')});
            if ( el.attr('src') !== url ) {
              modified = true;
              el.attr('src', EpImageService.getUrl({id: el.attr('data-uid')}));
            }
      });
      if ( modified ) {
        return tmp.html();
      }
      return text;
    };
  });
