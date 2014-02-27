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
  .controller('ExcelCtrl', function($scope, ExcelService, PerusteenOsat, SpinnerService) {
    $scope.osatutkinnot = [];
    $scope.vaihe = [];
    $scope.errors = [];
    $scope.warnings = [];
    $scope.filename = '';
    $scope.lukeeTiedostoa = true;
    $scope.naytaVirheet = false;
    $scope.uploadErrors = [];
    $scope.uploadSuccess = false;
    $scope.tutkinnonTyyppi = 'ammattitutkinto';
    $scope.parsinnanTyyppi = 'peruste';

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

    $scope.tallennaPeruste = function(peruste) {
      PerusteenOsat.saveTutkinnonOsa(peruste).success(function() {
        $scope.uploadSuccess = true;
      }).error(function(err) {
        $scope.uploadErrors = err.syy;
      });
    };

    $scope.tallennaOsatutkinnot = function() {
      var doneSuccess = _.after(_.size($scope.osatutkinnot), function() {
        $scope.uploadSuccess = true;
      });
      _($scope.osatutkinnot).filter(function(ot) {
        return ot.ladattu !== 0;
      }).forEach(function(ot) {
        var cop = _.omit(_.clone(ot), 'ladattu', 'syy');
        PerusteenOsat.saveTutkinnonOsa(cop, function(re) {
          ot.ladattu = 0;
          ot.id = re.id;
          ot.koodi = re.koodi;
          doneSuccess();
        }, function(err) {
          if (err) {
            ot.syy = err.syy;
            ot.ladattu = 1;
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
        var promise = ExcelService.parseXLSXToOsaperuste(file, $scope.tutkinnonTyyppi, $scope.parsinnanTyyppi);
        promise.then(function(resolve) {
          $scope.warnings = resolve.osatutkinnot.varoitukset;
          $scope.peruste = resolve.peruste;
          $scope.osatutkinnot = _.map(resolve.osatutkinnot.osaperusteet, function(ot) {
            return _.merge(ot, {
                ladattu: -1,
                koodi: '',
                syy: ''
            });
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
