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
  .controller('PerusteenTiedotCtrl', function($scope, $stateParams, $state,
    Koodisto, Perusteet, YleinenData, PerusteProjektiService,
    perusteprojektiTiedot, Notifikaatiot, Editointikontrollit, Kaanna,
    Varmistusdialogi, $timeout, $rootScope) {

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
        // Fix msd-elastic issue of setting incorrect initial height
        $timeout(function () {
          $rootScope.$broadcast('elastic:adjust');
        });
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
    $scope.kielet = YleinenData.kielet;
    $scope.dokumentit = {};

    function fixTimefield(field) {
      if (typeof $scope.peruste[field] === 'number') {
        $scope.peruste[field] = new Date($scope.peruste[field]);
      }
    }
    fixTimefield('voimassaoloAlkaa');
    fixTimefield('voimassaoloLoppuu');


    $scope.rajaaKoodit = function(koodi) {
      return koodi.koodi.indexOf('_3') !== -1;
    };

    $scope.hasContent = function (value) {
      if (_.isEmpty(value)) {
        return false;
      }
      if (_.isObject(value)) {
        return _.any(_.values(value));
      }
      return !!value;
    };

    $scope.koodistoHaku = function(koodisto) {
      var added = {nimi: koodisto.nimi, koulutuskoodiArvo: koodisto.koodiArvo, koulutuskoodiUri: koodisto.koodiUri};
      // Kun ensimmäinen koodi lisätään, perusteen nimi kopioidaan koodistosta
      if ($scope.editablePeruste.koulutukset.length === 0) {
        _.each(_.values(YleinenData.kielet), function (kieli) {
          $scope.editablePeruste.nimi[kieli] = koodisto.nimi[kieli];
        });
      }
      $scope.editablePeruste.koulutukset.push(added);

      //$scope.open[koodisto.koodi] = true;

      Koodisto.haeAlarelaatiot(koodisto.koodiUri, function(relaatiot) {
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              added.koulutusalakoodi = rel.koodiUri;
              break;
            case 'opintoalaoph2002':
              added.opintoalakoodi = rel.koodiUri;
              break;
          }
        });
      }, Notifikaatiot.fataali);
    };

    $scope.tallennaPeruste = function() {
      Perusteet.save({perusteId: $scope.peruste.id}, $scope.editablePeruste, function(vastaus) {
        $scope.peruste = vastaus;
        PerusteProjektiService.update();
        Notifikaatiot.onnistui('tallennettu');
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
          return $scope.editablePeruste.koulutustyyppi;
        }
      }, angular.noop, null)();
    };

    $scope.poistaKoulutus = function (koulutuskoodiArvo) {
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-koulutus',
        primaryBtn: 'poista',
        successCb: function () {
          $scope.editablePeruste.koulutukset = _.remove($scope.editablePeruste.koulutukset, function(koulutus) {
            return koulutus.koulutuskoodiArvo !== koulutuskoodiArvo;
          });
        }
      })();
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
