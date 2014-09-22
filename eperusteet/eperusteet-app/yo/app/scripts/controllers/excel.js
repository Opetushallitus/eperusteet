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
      .state('root.excel', {
        url: '/excel',
        templateUrl: 'views/excel.html',
        controller: 'ExcelCtrl',
      });
  })
  .controller('ExcelCtrl', function($scope, $modal, ExcelService, PerusteenOsat, TutkinnonOsanValidointi,
    Koodisto, PerusteprojektiResource, PerusteTutkinnonosat, $translate,
    SuoritustapaSisalto, Perusteet, Notifikaatiot, YleinenData, Utils) {
    $scope.alussa = true;
    $scope.filename = '';
    $scope.naytaVirheet = false;
    $scope.parsinnanTyyppi = 'peruste';
    $scope.projekti = {};
    $scope.projekti.koulutustyyppi = 'koulutustyyppi_1';
    $scope.projekti.laajuusYksikko = 'OSAAMISPISTE';
    $scope.peruste = {};
    $scope.haettuPeruste = {};
    $scope.peruste.$perusteTallennettu = false;
    $scope.koulutustyypit = YleinenData.koulutustyypit;
    $scope.yksikot = YleinenData.yksikot;
    $scope.suoritustavat = YleinenData.suoritustavat;
    $scope.suoritustapa = 'naytto';

    $scope.supportsFileReader = Utils.supportsFileReader();
    $scope.lang = $translate.use() || $translate.preferredLanguage();

    $scope.clearSelect = function() {
      $scope.parsinnanTila = [];
      $scope.osatutkinnot = [];
      $scope.errors = [];
      $scope.warnings = [];
      $scope.lukeeTiedostoa = true;
      $scope.uploadErrors = [];
      $scope.uploadSuccessTekstikappaleet = false;
      $scope.uploadSuccessTutkinnonosat = false;
    };
    $scope.clearSelect();

    $scope.editoiOsatutkintoa = function() {
    };

    $scope.poistaOsatutkinto = function(ot) {
      _.remove($scope.osatutkinnot, ot);
    };

    $scope.liitaKoodiOT = function(ot) {
      Koodisto.modaali(function(koodi) {
        ot.koodiUri = koodi.koodiUri;
        ot.$koodiArvo = koodi.koodiArvo;
      }, {
        tyyppi: function() {
          return 'tutkinnonosat';
        },
        ylarelaatioTyyppi: function() {
          return '';
        }
      })();
    };

    $scope.rajaaKoodit = function(koodi) {
      return koodi.koodi.indexOf('_3') !== -1;
    };

    $scope.tallennaPerusteprojekti = function(perusteprojekti) {
      perusteprojekti.tyyppi = perusteprojekti.tyyppi || 'normaali';
      if (perusteprojekti.koulutustyyppi !== 'koulutustyyppi_1') {
        delete perusteprojekti.laajuusYksikko;
      }
      PerusteprojektiResource.save(perusteprojekti, function(resPerusteprojekti) {
        $scope.haettuProjekti = resPerusteprojekti;
        Perusteet.get({
          perusteId: resPerusteprojekti._peruste
        },
        function(resPeruste) {
          $scope.peruste.$perusteTallennettu = true;
          $scope.haettuPeruste = resPeruste;
        });
      });
    };

    $scope.tallennaTekstikappaleet = function(tekstikentat) {
      var filtered = _.filter(tekstikentat, function(tk) {
        return tk.$ladattu !== 0;
      });
      var doneSuccess = _.after(_.size(filtered), function() {
        Notifikaatiot.onnistui('tallennus-onnistui');
        $scope.uploadSuccessTekstikappaleet = true;
      });

      _.forEach(filtered, function(tk) {
        tk.tila = 'luonnos';
        tk.osanTyyppi = 'tekstikappale';
        var viite = { perusteenOsa: tk };
        SuoritustapaSisalto.save({
          perusteId: $scope.haettuPeruste.id,
          suoritustapa: $scope.suoritustapa || $scope.haettuPeruste.suoritustavat[0].suoritustapakoodi
        }, viite, function(re) {
          tk.$ladattu = true;
          tk.id = re._perusteenOsa;
          doneSuccess();
        }, function(err) {
          tk.$syy = err.data.syy;
        });

      });
    };

    $scope.poistaTekstikentta = function(tekstikentta) {
      _.remove($scope.tekstikentat, tekstikentta);
    };

    $scope.tallennaOsatutkinnot = function() {
      var filtered = _.filter($scope.osatutkinnot, function(ot) {
        return ot.$ladattu !== 0;
      });
      var doneSuccess = _.after(_.size(filtered), function() {
        $scope.uploadSuccessTutkinnonosat = true;
        Notifikaatiot.onnistui('tallennus-onnistui');
      });

      _.forEach(filtered, function(ot) {
        var koodiUriKaytossa = _.any($scope.osatutkinnot, function(toinen) {
          return (ot !== toinen && ot.koodiUri !== '' && toinen.koodiUri === ot.koodiUri);
        });
        if (koodiUriKaytossa) {
          ot.$syy = ['excel-ei-kahta-samaa'];
        }
        else {
          var cop = _.clone(ot);
          cop.tavoitteet = {};
          cop.tila = 'luonnos';
          cop.osanTyyppi = 'tutkinnonosa';
          cop.$laajuus = cop.laajuus;
          delete cop.laajuus;
          PerusteTutkinnonosat.save({
            perusteId: $scope.haettuPeruste.id,
            suoritustapa: $scope.suoritustapa || $scope.haettuPeruste.suoritustavat[0].suoritustapakoodi
          }, {
            tutkinnonOsa: cop,
            laajuus: cop.$laajuus
          }, function(re) {
            ot.$ladattu = 0;
            ot.id = re._tutkinnonOsa;
            doneSuccess();
          }, function(err) {
            if (err) {
              ot.$syy = [err.data.syy];
              ot.$ladattu = 1;
            }
          });
        }
      });
    };

    $scope.haeRyhma = function() {
      $modal.open({
        templateUrl: 'views/modals/tuotyoryhma.html',
        controller: 'TyoryhmanTuontiModalCtrl'
      })
      .result.then(function(ryhma) {
        $scope.projekti.ryhmaOid = ryhma.oid;
        $scope.projekti.$ryhmaNimi = ryhma.nimi;
      });
    };

    $scope.paivitaTilaa = function(notifier) {
      $scope.parsinnanTila.push(notifier);
    };

    $scope.onFileSelect = function(err, file) {
      $scope.lukeeTiedostoa = true;
      $scope.alussa = false;
      $scope.osatutkinnot = [];

      if (err || !file) {
        Notifikaatiot.varoitus('virhe-tiedosto-epäonnistui');
      }
      else {
        ExcelService.parseXLSXToOsaperuste(
          file,
          $scope.projekti.koulutustyyppi === 'koulutustyyppi_1' ? 'perustutkinto' : 'ammattitutkinto',
          $scope.paivitaTilaa
          )
          .then(function(resolve) {
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

            $scope.projekti.diaarinumero = $scope.peruste.diaarinumero || '';
            $scope.projekti.laajuusYksikko = YleinenData.yksikotMap[$scope.peruste.laajuusYksikko] || '';
            $scope.lukeeTiedostoa = false;
          }, function(errors) {
            $scope.errors = errors;
            $scope.lukeeTiedostoa = false;
          });
      }
    };
  });
