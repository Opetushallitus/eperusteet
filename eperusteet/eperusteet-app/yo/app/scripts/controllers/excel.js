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
  .controller('ExcelCtrl', function($scope, ExcelService, PerusteenOsat, TutkinnonOsanValidointi, Koodisto) {
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
      $scope.osatutkinnot = [];
      $scope.vaihe = [];
      $scope.errors = [];
      $scope.warnings = [];
      $scope.lukeeTiedostoa = true;
      $scope.lukeeTiedostoa = true;
      $scope.uploadErrors = [];
      $scope.uploadSuccess = false;
    };

    $scope.editoiOsatutkintoa = function() {
    };

    $scope.poistaOsatutkinto = function(ot) {
      _.remove($scope.osatutkinnot, ot);
    };

    $scope.liitaKoodiOT = function(ot) {
      Koodisto.modaali(function(koodi) {
        ot.koodiUri = koodi;
      }, { tyyppi: function() { return 'tutkinnonosat'; } })();
    };

    $scope.tallennaPeruste = function(peruste) {
      var doneSuccess = _.after(_.size(peruste.tekstikentat), function() { $scope.uploadSuccess = true; });
      _(peruste.tekstikentat).filter(function(tk) {
        return tk.ladattu !== 0;
      }).forEach(function(tk) {
        PerusteenOsat.saveTekstikappale(_.omit(tk, 'uploadErrors', 'syy', 'ladattu'), function(re) {
          tk.ladattu = true;
          tk.id = re.id;
          doneSuccess();
        }, function(err) {
          tk.syy = err.data.syy;
        });
      });
    };

    $scope.poistaTekstikentta = function(tekstikentta) {
      _.remove($scope.peruste.tekstikentat, tekstikentta);
    };

    $scope.tallennaOsatutkinnot = function() {
      var doneSuccess = _.after(_.size($scope.osatutkinnot), function() { $scope.uploadSuccess = true; });
      _($scope.osatutkinnot).filter(function(ot) {
        return ot.ladattu !== 0;
      }).forEach(function(ot) {
        var cop = _.omit(_.clone(ot), 'ladattu', 'syy');
        TutkinnonOsanValidointi.validoi(cop).then(function() {
          PerusteenOsat.saveTutkinnonOsa(cop, function(re) {
            ot.ladattu = 0;
            ot.id = re.id;
            ot.koodiUri = re.koodi;
            doneSuccess();
          }, function(err) {
            if (err) {
              ot.syy = err.data.syy;
              ot.ladattu = 1;
            }
          });
        }, function(virhe) {
          ot.syy = virhe;
          ot.ladattu = 1;
        });
      });
    };

    $scope.onFileSelect = function(err, file) {
      $scope.lukeeTiedostoa = true;
      $scope.alussa = false;
      $scope.osatutkinnot = [];

      if (err || !file) {
        // TODO: Hoida virhetilanteet
      } else {
        var promise = ExcelService.parseXLSXToOsaperuste(file, $scope.tutkinnonTyyppi, $scope.parsinnanTyyppi);
        promise.then(function(resolve) {
          $scope.warnings = resolve.osatutkinnot.varoitukset;
          $scope.peruste = resolve.peruste;
          $scope.peruste.tekstikentat = _.map($scope.peruste.tekstikentat, function(tk) {
            return _.merge(tk, {
                ladattu: -1,
                syy: ''
            });
          });
          $scope.osatutkinnot = _.map(resolve.osatutkinnot.osaperusteet, function(ot) {
            return _.merge(ot, {
                ladattu: -1,
                koodiUri: '',
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
