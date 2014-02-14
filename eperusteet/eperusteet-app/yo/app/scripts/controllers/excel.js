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
    $scope.uploadErrors = [];
    $scope.uploadSuccess = false;

    $scope.clearSelect = function() {
      $scope.$apply(function() {
        $scope.vaihe = [];
        $scope.errors = [];
        $scope.warnings = [];
        $scope.lukeeTiedostoa = true;
        $scope.lukeeTiedostoa = true;
        $scope.uploadErrors = [];
        $scope.uploadSuccess = false;
      });
    };

    $scope.editoiOsaperusteita = function() {
    };

    $scope.tallennaOsaperusteet = function() {
      var doneSuccess = _.after(_.size($scope.osaperusteet), function() {
        $scope.uploadSuccess = true;
      });
      _.forEach($scope.osaperusteet, function(op) {

        var saveop = ExcelService.saveOsaperuste({
          arvioinninKohdealueet: _.clone(op.arvioinninKohdealueet),
          lisatiedot: {
            fi: 'testi'
          }
        });

        saveop.success(function(re, status) {
          doneSuccess();
        }).error(function(err) {
          $scope.uploadErrors.push({
            name: op.nimi,
            message: err.syy
          });
        });
      });
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
