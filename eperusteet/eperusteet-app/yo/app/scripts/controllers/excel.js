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
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('excel', {
        url: '/excel',
        templateUrl: 'views/excel.html',
        controller: 'ExcelCtrl',
      });
  })
  .controller('ExcelCtrl', function($scope, ExcelService, PerusteenOsat, TutkinnonOsanValidointi,
                                    Koodisto, PerusteprojektiResource, PerusteTutkinnonosat,
                                    SuoritustapaSisalto, Perusteet, Notifikaatiot, YleinenData) {
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
    $scope.projekti = {};
    $scope.peruste = {};
    $scope.haettuPeruste = {};
    $scope.peruste.$perusteTallennettu = false;
    $scope.koulutustyypit = YleinenData.koulutustyypit;

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

    $scope.poistaOsatutkinto = function(ot) { _.remove($scope.osatutkinnot, ot); };

    $scope.liitaKoodiOT = function(ot) {
      Koodisto.modaali(function(koodi) {
        ot.koodiUri = koodi.koodi;
      }, { tyyppi: function() { return 'tutkinnonosat'; }, ylarelaatioTyyppi: function() {return '';} })();
    };

    $scope.rajaaKoodit = function(koodi) { return koodi.koodi.indexOf('_3') !== -1; };

    $scope.tallennaPerusteprojekti = function(perusteprojekti) {
      PerusteprojektiResource.update(perusteprojekti, function(resPerusteprojekti) {
        Perusteet.get({
          perusteenId: resPerusteprojekti._peruste
        }, function(resPeruste) {
          $scope.peruste.$perusteTallennettu = true;
          $scope.haettuPeruste = resPeruste;
        });
      }, function() {
        // TODO: Virhe notifikaatio
      });
    };

    $scope.tallennaTekstikappaleet = function(tekstikentat) {
      var doneSuccess = _.after(_.size(tekstikentat), function() { $scope.uploadSuccess = true; });

      _(tekstikentat).filter(function(tk) {
        return tk.$ladattu !== 0;
      })
      .forEach(function(tk) {
        PerusteenOsat.saveTekstikappale(tk, function(re) {
          SuoritustapaSisalto.add({
            perusteId: $scope.haettuPeruste.id,
            suoritustapa: $scope.haettuPeruste.suoritustavat[0].suoritustapakoodi
          }, {
            _perusteenOsa: re.id
          }, function() {
            tk.$ladattu = true;
            tk.id = re.id;
            doneSuccess();
          }, function(err) {
            tk.$syy = err.data.syy;
            // TODO: Lisää notifikaatio
          });
        }, function(err) {
          tk.$syy = err.data.syy;
        });
      });
    };

    // $scope.perusteHaku = function(koodisto) {
    //   $scope.hakemassa = true;

    //   $scope.peruste.nimi = koodisto.nimi;
    //   $scope.peruste.koodi = koodisto.koodi;
    //   $scope.peruste.koulutukset = [{}];
    //   $scope.peruste.koulutukset[0].koulutuskoodi = koodisto.koodi;
    //   $scope.peruste.suoritustavat = [{suoritustapakoodi: 'ops'}];

    //   Koodisto.haeAlarelaatiot($scope.peruste.koodi, function (relaatiot) {
    //     _.forEach(relaatiot, function(rel) {
    //       switch (rel.koodisto.koodistoUri) {
    //         case 'koulutusalaoph2002':
    //           $scope.peruste.koulutukset[0].koulutusalakoodi = rel.koodi;
    //           break;
    //         case 'opintoalaoph2002':
    //           $scope.peruste.koulutukset[0].opintoalakoodi = rel.koodi;
    //           break;
    //         case 'koulutustyyppi':
    //           if (rel.koodi === 'koulutustyyppi_1' || rel.koodi === 'koulutustyyppi_11' || rel.koodi === 'koulutustyyppi_12') {
    //             $scope.peruste.tutkintokoodi = rel.koodi;
    //           }
    //           break;
    //       }
    //     });
    //     $scope.hakemassa = false;
    //   }, function (virhe) {
    //     console.log('koodisto alarelaatio virhe', virhe);
    //     $scope.hakemassa = false;
    //   });
    // };

    $scope.poistaTekstikentta = function(tekstikentta) { _.remove($scope.peruste.tekstikentat, tekstikentta); };

    $scope.tallennaOsatutkinnot = function() {
      var doneSuccess = _.after(_.size($scope.osatutkinnot), function() {
        $scope.uploadSuccess = true;
        Notifikaatiot.onnistui('tallennus-onnistui', '');
      });

      _($scope.osatutkinnot).filter(function(ot) {
        return ot.$ladattu !== 0;
      })
      .forEach(function(ot) {
        var koodiUriKaytossa = _.any($scope.osatutkinnot, function(toinen) {
          return (ot !== toinen && ot.koodiUri !== '' && toinen.koodiUri === ot.koodiUri);
        });
        if (koodiUriKaytossa) {
          ot.$syy = ['excel-ei-kahta-samaa'];
        } else {
          var cop = _.clone(ot);
          // TutkinnonOsanValidointi.validoi(cop).then(function() {
            cop.tavoitteet = {};
            PerusteenOsat.saveTutkinnonOsa(cop, function(re) {
              PerusteTutkinnonosat.save({
                perusteenId: $scope.haettuPeruste.id,
                suoritustapa: $scope.haettuPeruste.suoritustavat[0].suoritustapakoodi // FIXME
              }, {
                _tutkinnonOsa: re.id
              }, function() {
                ot.$ladattu = 0;
                ot.id = re.id;
                ot.koodiUri = re.koodiUri;
                doneSuccess();
              }, function(err) {
                if (err) {
                  ot.$syy = [err.data.syy];
                  ot.$ladattu = 1;
                }
              });
            }, function(err) {
              if (err) {
                ot.$syy = [err.data.syy];
                ot.$ladattu = 1;
              }
            });
          // }, function(virhe) {
            // ot.$syy = virhe;
            // ot.$ladattu = 1;
          // });
        }
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
          $scope.peruste = _.omit(resolve.peruste, 'tekstikentat');
          $scope.tekstikentat = _.map(resolve.peruste.tekstikentat, function(tk) {
            return _.merge(tk, {
                $ladattu: -1,
                $syy: []
            });
          });
          $scope.osatutkinnot = _.map(resolve.osatutkinnot.osaperusteet, function(ot) {
            return _.merge(ot, {
                $ladattu: -1,
                koodiUri: '',
                $syy: []
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
