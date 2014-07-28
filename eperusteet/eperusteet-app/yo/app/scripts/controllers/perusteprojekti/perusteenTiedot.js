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
/* global _, URL */

angular.module('eperusteApp')
  .controller('PerusteenTiedotCtrl', function($scope, $stateParams, $state,
    Koodisto, Perusteet, YleinenData, PerusteProjektiService,
    perusteprojektiTiedot, Notifikaatiot, Pdf, Editointikontrollit, Kaanna,
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
    $scope.pdfToken = null;
    $scope.uusinDokumenttiId = null;
    $scope.pdfLinkki = null;
    $scope.generointiDisabled = false;
    $scope.generointiTeksti = 'generoi-pdf';
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
      angular.forEach(YleinenData.kielet, function(value) {
        if (_.isEmpty($scope.editablePeruste.nimi[value]) && !_.isNull(koodisto.nimi[value])) {
          // Nimi määräytyy ensimmäisen lisätyn koulutuksen perusteella
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

    $scope.generoiPdf = function(kieli) {
      $scope.generointiDisabled = true;
      $scope.pdfLinkki = null;
      $scope.generointiTeksti = 'generoidaan-dokumenttia';

      Pdf.generoiPdf($scope.peruste.id, kieli, function(res) {

          console.log('generoiPdf res', res);
          function polleri(t) {
            Pdf.haeTila(t, function(res2) {
              switch(res2.tila) {
                case 'luodaan':
                case 'ei_ole':
                  setTimeout(function() {
                    polleri(res2.id);
                  }, 3000);
                  break;
                case 'valmis':
                  Notifikaatiot.onnistui('dokumentti-luotu');
                  res2.linkki = Pdf.haeLinkki(res2.id);
                  $scope.dokumentit[kieli] = res2;
                  $scope.generointiDisabled = false;
                  $scope.generointiTeksti = 'generoi-pdf';
                  break;
                case 'epaonnistui':
                  Notifikaatiot.fataali(Kaanna.kaanna('dokumentin-luonti-epaonnistui') + ': ' + res2.virhekoodi);
                  $scope.generointiDisabled = false;
                  $scope.generointiTeksti = 'generoi-pdf';
                  break;
                default:
                  console.log('tuntematon tila', res2.tila);
                  break;
              }
            });
          }

          if (res.id !== null) {
            $scope.pdfToken = res.token;
            setTimeout(function() {
              polleri(res.id);
            }, 3000);
          }

      } );
    };

    $scope.paivitaUusin = function(kieli) {
      Pdf.haeUusin($scope.peruste.id, kieli, function(res) {
        if (res.id !== null) {
          res.linkki = Pdf.haeLinkki(res);
          $scope.dokumentit[kieli] = res;
        }
      });
    };

    $scope.lataaPdf = function() {

      Pdf.haeDokumentti($scope.pdfToken, function(res) {
        var file = new Blob([res], { type: 'application/pdf' });
        var fileURL = URL.createObjectURL(file);
        window.open(fileURL);
      });

    };

    Object.keys($scope.kielet).forEach(function (item) {
      var kieli = $scope.kielet[item];
      Pdf.haeUusin($scope.peruste.id, kieli, function(res) {
        if (res.id !== null) {
          res.linkki = Pdf.haeLinkki(res.id);
          $scope.dokumentit[kieli] = res;
        }
        console.log('Uusin kielella', item, ':', res);
      });
    });

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
      Varmistusdialogi.dialogi({
        otsikko: 'vahvista-poisto',
        teksti: 'poistetaanko-koulutus',
        primaryBtn: 'poista',
        successCb: function () {
          $scope.editablePeruste.koulutukset = _.remove($scope.editablePeruste.koulutukset, function(koulutus) {
            return koulutus.koulutuskoodi !== koulutuskoodi;
          });
          if (_.isEmpty($scope.editablePeruste.koulutukset)) {
            $scope.editablePeruste.nimi = {};
          }
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
