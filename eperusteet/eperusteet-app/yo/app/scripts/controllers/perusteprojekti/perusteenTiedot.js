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
  .service('PerusteenTutkintonimikkeet', function(PerusteTutkintonimikekoodit, YleinenData) {
    this.perusteellaTutkintonimikkeet = function(peruste) {
      if (_.isObject(peruste)) {
        peruste = peruste.koulutustyyppi;
      }
      return _.isString(peruste) &&
        YleinenData.koulutustyyppiInfo[peruste] && YleinenData.koulutustyyppiInfo[peruste].hasTutkintonimikkeet;
    };

    this.get = function (perusteId, object) {
      PerusteTutkintonimikekoodit.get({ perusteId: perusteId }, function(res) {
        object.koodisto = _.map(res, function(osa) {
          function parsiNimi(kentta) {
            if (osa[kentta + 'Arvo']) {
              var nimi = osa.b[osa[kentta + 'Arvo']].metadata;
              osa['$' + kentta + 'Nimi'] = _.zipObject(_.map(nimi, 'kieli'), _.map(nimi, 'nimi'));
            }
          }

          parsiNimi('osaamisala');
          parsiNimi('tutkintonimike');
          parsiNimi('tutkinnonOsa');
          delete osa.b;
          return osa;
        });
        object.koodisto.$resolved = true;
      });
    };
  })

  .controller('PerusteenTiedotCtrl', function($scope, $stateParams, $state,
    Koodisto, Perusteet, YleinenData, PerusteProjektiService,
    perusteprojektiTiedot, Notifikaatiot, Editointikontrollit, Kaanna,
    Varmistusdialogi, $timeout, $rootScope, PerusteTutkintonimikekoodit, $modal,
    PerusteenTutkintonimikkeet, valittavatKielet, Kieli) {
    $scope.showKoulutukset = function () {
      return YleinenData.showKoulutukset($scope.editablePeruste);
    };

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
        return $scope.projektinPerusteForm.$valid && !_.isEmpty($scope.editablePeruste.kielet);
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

    function noudaKorvattavienDiaarienNimet(korvattavatDiaarinumerot) {
      if (korvattavatDiaarinumerot !== null && angular.isDefined(korvattavatDiaarinumerot)) {
        angular.forEach(korvattavatDiaarinumerot, function(value) {
          noudaDiaarilleNimi(value);
        });
      }
      $scope.ladataanKorvattavia = false;
    }

    function noudaDiaarilleNimi(diaari) {
      Perusteet.diaari({diaarinumero: diaari}, function(vastaus) {
        $scope.korvattavaDiaariNimiMap[diaari] = vastaus.nimi;
      }, function () {
        $scope.korvattavaDiaariNimiMap[diaari] = 'korvattavaa-ei-loydy-jarjestelmasta';
      });
    }

    $scope.lisaaKorvattavaDiaari = function(uusiKorvattavaDiaari) {
      if (_.indexOf($scope.editablePeruste.korvattavatDiaarinumerot, uusiKorvattavaDiaari) !== -1) {
        Notifikaatiot.varoitus('diaari-jo-listalla');
      }
      if (uusiKorvattavaDiaari !== $scope.editablePeruste.diaarinumero) {
        $scope.editablePeruste.korvattavatDiaarinumerot.push(uusiKorvattavaDiaari);
        noudaDiaarilleNimi(uusiKorvattavaDiaari);
        $scope.uusiKorvattavaDiaari = '';
      } else {
        Notifikaatiot.varoitus('oma-diaarinumero');
      }
    };

    $scope.poistaKorvattavaDiaari = function(diaarinumero) {
      var index = _.indexOf($scope.editablePeruste.korvattavatDiaarinumerot, diaarinumero);
      if (index >= 0) {
         $scope.editablePeruste.korvattavatDiaarinumerot.splice(index, 1);
      }
    };

    $scope.ladataanKorvattavia = true;
    $scope.korvattavaDiaariNimiMap = {};
    $scope.hakemassa = false;
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    noudaKorvattavienDiaarienNimet($scope.peruste.korvattavatDiaarinumerot);
    $scope.editablePeruste = $scope.peruste;
    $scope.peruste.nimi = $scope.peruste.nimi || {};
    $scope.peruste.kuvaus = $scope.peruste.kuvaus || {};
    $scope.projektiId = $stateParams.perusteProjektiId;
    //$scope.open = {};
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa() || 'naytto';
    $scope.kielet = YleinenData.kielet;
    $scope.dokumentit = {};
    $scope.koodisto = [];
    $scope.$koodistoResolved = false;
    $scope.$perusteellaTutkintonimikkeet = PerusteenTutkintonimikkeet.perusteellaTutkintonimikkeet($scope.peruste);
    $scope.kieliOrder = Kieli.kieliOrder;



    function valitseValittavatKielet(kielet) {
      var current = (kielet || $scope.editablePeruste.kielet);
      $scope.valittavatKielet = _(valittavatKielet).sortBy($scope.kieliOrder).map(function (kielikoodi) {
        return {available: _.indexOf(current, kielikoodi) === -1, koodi: kielikoodi};
      }).value();
    }
    valitseValittavatKielet($scope.peruste.kielet);

    $scope.lisaaKieli = function(kieli) {
      $scope.editablePeruste.kielet.push(kieli);
      $scope.editablePeruste.kielet = _.unique($scope.editablePeruste.kielet);
      valitseValittavatKielet();
    };

    $scope.poistaKieli = function(kieli) {
      _.remove($scope.editablePeruste.kielet, function(v) { return v === kieli; });
      valitseValittavatKielet();
    };

    $scope.kaikkiKieletValittu = function() {
      return _.size($scope.editablePeruste.kielet) === _.size(valittavatKielet);
    };

    $scope.lisaaNimike = function() {
      $modal.open({
        templateUrl: 'views/modals/lisaaTutkintonimike.html',
        controller: function($q, $scope, $modalInstance) {
          $scope.koodit = {};
          $scope.valmisCb = function(res) {
            $scope.koodit[res.koodisto.koodistoUri] = res;
          };

          $scope.ok = $modalInstance.close;
          $scope.peru = $modalInstance.dismiss;
        }
      })
      .result.then(function(uusi) {
        var obj = {
          peruste: $scope.peruste.id,
          tutkintonimikeUri: uusi.tutkintonimikkeet.koodiUri,
          tutkintonimikeArvo: uusi.tutkintonimikkeet.koodiArvo,
          $tutkintonimikeNimi: uusi.tutkintonimikkeet.nimi,
        };

        if (uusi.osaamisala) {
          obj.osaamisalaUri = uusi.osaamisala.koodiUri;
          obj.osaamisalaArvo = uusi.osaamisala.koodiArvo;
          obj.$osaamisalaNimi = uusi.osaamisala.nimi;
        }

        if (uusi.tutkinnonosat) {
          obj.tutkinnonOsaUri = uusi.tutkinnonosat.koodiUri;
          obj.tutkinnonOsaArvo = uusi.tutkinnonosat.koodiArvo;
          obj.$tutkinnonOsaNimi = uusi.tutkinnonosat.nimi;
        }

        $scope.koodisto.push(obj);
        PerusteTutkintonimikekoodit.save({ perusteId: $scope.peruste.id }, obj, function(res) {
          obj.id = res.id;
          Notifikaatiot.onnistui('tutkintonimikkeen-lisays-onnistui');
        }, function() {
          _.remove($scope.koodisto, obj);
          Notifikaatiot.varoitus('tutkintonimikkeen-lisays-epaonnistui');
        });
      });
    };

    PerusteenTutkintonimikkeet.get($scope.peruste.id, $scope);

    $scope.poistaTutkintonimike = function(nimike) {
      PerusteTutkintonimikekoodit.remove({
        perusteId: $scope.peruste.id,
        nimikeId: nimike.id
      }, function() {
        _.remove($scope.koodisto, nimike);
        Notifikaatiot.onnistui('tutkintonimike-poistettu-onnistuneesti');
      }, Notifikaatiot.serverCb);
    };

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
        Notifikaatiot.fataali('tallentaminen-epaonnistui');
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
