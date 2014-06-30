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
  .controller('PerusteenTiedotCtrl', function($scope, $stateParams, $state,
    Koodisto, Perusteet, YleinenData, PerusteProjektiService,
    perusteprojektiTiedot, Notifikaatiot, Pdf, Editointikontrollit) {

    $scope.editEnabled = false;
    var editingCallbacks = {
      edit: function () {
        fixTimefield('voimassaoloAlkaa');
        fixTimefield('voimassaoloLoppuu');
        $scope.editablePeruste = angular.copy($scope.peruste);
      },
      save: function () {
        $scope.tallennaPeruste();
      },
      validate: function () {
        return $scope.projektinPerusteForm.$valid;
      },
      cancel: function () {
        $scope.editablePeruste = $scope.peruste;
      },
      notify: function (mode) {
        $scope.editEnabled = mode;
      }
    };
    Editointikontrollit.registerCallback(editingCallbacks);

    $scope.voiMuokata = function () {
      // TODO Vain omistaja/sihteeri voi muokata
      return true;
    };

    $scope.muokkaa = function () {
      Editointikontrollit.startEditing();
    };

    $scope.hakemassa = false;
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.editablePeruste = $scope.peruste;
    $scope.peruste.nimi = $scope.peruste.nimi || {};
    $scope.peruste.kuvaus = $scope.peruste.kuvaus || {};
    $scope.projektiId = $stateParams.perusteProjektiId;
    //$scope.open = {};
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa() || 'naytto';
    $scope.pdf_token = null;
    $scope.pdfLinkki = null;

    function fixTimefield(field) {
      if (typeof $scope.peruste[field] === 'number') {
        $scope.peruste[field] = new Date($scope.peruste[field]);
      }
    }
    fixTimefield('voimassaoloAlkaa');
    fixTimefield('voimassaoloLoppuu');

    $scope.voimaantuloPvmOpen = false;
    $scope.voimaantuloLoppuuPvmOpen = false;
    $scope.dateOptions = YleinenData.dateOptions;
    $scope.format = YleinenData.dateFormatDatepicker;
    $scope.open = function(value, $event) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope[value] = !$scope[value];
    };


    $scope.rajaaKoodit = function(koodi) {
      return koodi.koodi.indexOf('_3') !== -1;
    };

    $scope.koodistoHaku = function(koodisto) {
      angular.forEach(YleinenData.kielet, function(value) {
        if (_.isEmpty($scope.editablePeruste.nimi[value]) && !_.isNull(koodisto.nimi[value])) {
          $scope.editablePeruste.nimi[value] = koodisto.nimi[value];
        }
      });

      var added = {nimi: koodisto.nimi, koulutuskoodi: koodisto.koodi};
      $scope.editablePeruste.koulutukset.push(added);

      //$scope.open[koodisto.koodi] = true;

      Koodisto.haeAlarelaatiot(koodisto.koodi, function(relaatiot) {
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              added.koulutusalakoodi = rel.koodi;
              break;
            case 'opintoalaoph2002':
              added.opintoalakoodi = rel.koodi;
              break;
          }
        });
      }, function(virhe) {
        Notifikaatiot.fataali(virhe);
      });
    };

    $scope.generoiPdf = function() {
      Pdf.generoiPdf($scope.peruste.id, function(res) {
          $scope.pdf_token = res.token;
          $scope.pdfLinkki = Pdf.haeLinkki(res.token);
      } );
    };

    $scope.lataaPdf = function() {

      Pdf.haeDokumentti($scope.pdf_token, function(res) {
        var file = new Blob([res], { type: 'application/pdf' });
        var fileURL = URL.createObjectURL(file);
        window.open(fileURL);
      });

    };

    $scope.tallennaPeruste = function() {
      Perusteet.save({perusteId: $scope.peruste.id}, $scope.editablePeruste, function(vastaus) {
        $scope.peruste = vastaus;
        PerusteProjektiService.update();
      }, function() {
        Notifikaatiot.fataali('tallentaminen-epäonnistui');
      });
    };

    $scope.avaaKoodistoModaali = function() {
      Koodisto.modaali(function(koodi) {
        $scope.koodistoHaku(koodi);
      }, {
        tyyppi: function() {
          return 'koulutus';
        },
        ylarelaatioTyyppi: function() {
          return $scope.editablePeruste.tutkintokoodi;
        }
      }, angular.noop, null)();
    };

    $scope.poistaKoulutus = function (koulutuskoodi) {
      $scope.editablePeruste.koulutukset = _.remove($scope.editablePeruste.koulutukset, function(koulutus) {
        return koulutus.koulutuskoodi !== koulutuskoodi;
      });
    };

    $scope.koulutusalaNimi = function(koodi) {
      return $scope.Koulutusalat.haeKoulutusalaNimi(koodi);
    };

    $scope.opintoalaNimi = function(koodi) {
      return $scope.Opintoalat.haeOpintoalaNimi(koodi);
    };

    $scope.$on('event:spinner_on', function() {
      $scope.hakemassa = true;
    });

    $scope.$on('event:spinner_off', function() {
      $scope.hakemassa = false;
    });

  });
