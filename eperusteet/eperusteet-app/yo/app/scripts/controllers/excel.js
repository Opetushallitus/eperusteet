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
    $scope.osatutkinnot = [];
    $scope.vaihe = [];
    $scope.errors = [];
    $scope.warnings = [];
    $scope.filename = '';
    $scope.lukeeTiedostoa = true;
    $scope.uploadErrors = [];
    $scope.uploadSuccess = false;
    $scope.tutkinnonTyyppi = 'perustutkinto';

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

    $scope.editoiOsatutkintoa = function() {
    };

    $scope.poistaOsatutkinto = function(ot) {
      _.remove($scope.osatutkinnot, ot);
    };

    $scope.tallennaOsatutkinnot = function() {
      var doneSuccess = _.after(_.size($scope.osatutkinnot), function() {
        $scope.uploadSuccess = true;
      });
      _($scope.osatutkinnot).filter(function(ot) {
        return ot.ladattu !== 0;
      }).forEach(function(ot) {
        var cop = _.omit(_.clone(ot), 'ladattu');
        var saveop = ExcelService.saveOsaperuste(cop);
        console.log(cop);
        saveop.success(function() {
          ot.ladattu = 0;
          doneSuccess();
        }).error(function(err) {
          if (err) {
            $scope.uploadErrors.push({
                name: ot.nimi,
                message: err.syy
            });
          }
        });
      });
    };

    $scope.onFileSelect = function(err, file) {
      $scope.lukeeTiedostoa = true;
      $scope.alussa = false;

      if (err || !file) {
        // TODO: Hoida virhetilanteet
      } else {
        var promise = ExcelService.parseXLSXToOsaperuste(file, $scope.tutkinnonTyyppi);
        promise.then(function(resolve) {
          $scope.warnings = resolve.varoitukset;
          $scope.osatutkinnot = _.map(resolve.osaperusteet, function(ot) {
            return _.merge(ot, { ladattu: -1, koodi: '' });
          });
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
