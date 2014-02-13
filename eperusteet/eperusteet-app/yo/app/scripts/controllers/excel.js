'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/excel', {
        templateUrl: 'views/excel.html',
        controller: 'ExcelCtrl'
      });
  })
  .controller('ExcelCtrl', function($scope, ExcelService) {
    $scope.osaperusteet = [];
    $scope.vaihe = [];
    $scope.errors = [];
    $scope.warnings = [];
    $scope.filename = '';
    $scope.lukeeTiedostoa = true;

    $scope.clearSelect = function() {
      $scope.$apply(function() {
        $scope.vaihe = [];
        $scope.errors = [];
        $scope.warnings = [];
        $scope.lukeeTiedostoa = true;
      });
    };

    $scope.editoiOsaperusteita = function() {
    };

    $scope.tallennaOsaperusteet = function() {
      console.log($scope.osaperusteet);
    };

    $scope.onFileSelect = function(err, file) {
      $scope.lukeeTiedostoa = true;
      $scope.alussa = false;

      if (err || !file) {
        // TODO: Hoida virhetilanteet
      } else {
        var promise = ExcelService.parseXLSXToOsaperuste(file);
        promise.then(function(resolve) {
          $scope.warnings = resolve.varoitukset;
          $scope.osaperusteet = resolve.osaperusteet;
          $scope.lukeeTiedostoa = false;
        }, function(errors) {
          $scope.errors = errors;
          $scope.lukeeTiedostoa = false;
        }, function() {
          // TODO: Ota tilannepäivitykset vastaan ja rendaa tilapalkki
        });
      }
    };
    $scope.onProgress = function() {
    };
  });
