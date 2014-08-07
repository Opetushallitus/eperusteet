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
  .factory('Dokumentti', function($resource, SERVICE_LOC) {

      // api:
      //
      // Generointi:
      // /dokumentti/create/:id/:kieli
      // /dokumentti/create/:id // oletuskieli
      //
      // Luonnin seuranta/tilakysely:
      // /dokumentti/query/:token
      //
      // Valmiin dokumentin hakeminen:
      // /dokumentti/get/:token
      //
      // Yksivaiheinen luominen
      // /dokumentti/:id/:kieli
      // /dokumentti/:id // oletuskieli

    return $resource(SERVICE_LOC + '/dokumentti/create/:id/:kieli', {
      id: '@id',
      kieli: '@kieli'
    });
  })
  .factory('DokumenttiHaku', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/dokumentti/:tapa/:token', {
      tapa: '@tapa', // query | get
      token: '@token'
    },
    {
        hae: {method: 'GET', responseType: 'arraybuffer'}
    });
  })
  .factory('DokumenttiKysely', function($resource, SERVICE_LOC) {

    return $resource(SERVICE_LOC + '/dokumentti/uusin/:perusteId/:kieli', {
      perusteId: '@id',
      kieli: '@kieli'
    });
  })
  .service('Pdf', function(Dokumentti, DokumenttiHaku, DokumenttiKysely, SERVICE_LOC) {

    function generoiPdf(perusteId, kieli, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      Dokumentti.get({
        id: perusteId,
        kieli: kieli
      }, success, failure);
    }

    function haeTila(tokenId, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      DokumenttiHaku.get({
        tapa: 'query',
        token: tokenId
      }, success);
    }

    function haeDokumentti(tokenId, success, failure) {
        success = success || angular.noop;
        failure = failure || angular.noop;

        return DokumenttiHaku.hae({
            tapa: 'get',
            token: tokenId
        }, success);
    }

    function haeLinkki(tokenId) {
        // dis like, ewwww
        return SERVICE_LOC + '/dokumentti/get/'+tokenId;
    }

    function haeUusin(perusteId, kieli, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      return DokumenttiKysely.get({
        perusteId: perusteId,
        kieli: kieli
      }, success);

    }

    return {
      generoiPdf: generoiPdf,
      haeDokumentti: haeDokumentti,
      haeTila: haeTila,
      haeLinkki: haeLinkki,
      haeUusin: haeUusin
    };
  })

  .factory('PdfCreation', function ($modal, YleinenData) {
    var service = {};
    var perusteId = null;

    service.setPerusteId = function (id) {
      perusteId = id;
    };

    service.openModal = function () {
      $modal.open({
        templateUrl: 'views/modals/pdfcreation.html',
        controller: 'PdfCreationController',
        resolve: {
          perusteId: function () { return perusteId; },
          kielet: function () {
            return {
              lista: _.sortBy(YleinenData.kielet),
              valittu: YleinenData.kieli
            };
          }
        }
      });
    };

    return service;
  })
  .controller('PdfCreationController', function ($scope, kielet, Pdf, perusteId,
    $timeout, Notifikaatiot, Kaanna) {
    $scope.kielet = kielet;
    $scope.docs = {};
    var pdfToken = null;

    $scope.hasPdf = function () {
      return !!$scope.docs[$scope.kielet.valittu];
    };

    function fetchLatest(lang) {
      var kielet;
      if (_.isString(lang)) {
        kielet = [lang];
      } else {
        kielet = kielet.lista;
      }
      _.each(kielet, function (kieli) {
        Pdf.haeUusin(perusteId, kieli, function(res) {
          if (kieli === $scope.kielet.valittu) {
            $scope.tila = res.tila;
          }
          if (res.id !== null) {
            res.url = Pdf.haeLinkki(res.id);
            $scope.docs[kieli] = res;
          }
        });
      });
    }

    function enableActions(disable) {
      $scope.generateInProgress = _.isUndefined(disable) ? false : !disable;
    }

    function getStatus(id) {
      Pdf.haeTila(id, function(res) {
        $scope.tila = res.tila;
        switch(res.tila) {
          case 'luodaan':
          case 'ei_ole':
            startPolling(res.id);
            break;
          case 'valmis':
            Notifikaatiot.onnistui('dokumentti-luotu');
            res.url = Pdf.haeLinkki(res.id);
            $scope.docs[$scope.kielet.valittu] = res;
            enableActions();
            break;
          default: // 'epaonnistui' + others(?)
            Notifikaatiot.fataali(Kaanna.kaanna('dokumentin-luonti-epaonnistui') +
              ': ' + res.virhekoodi || res.tila);
            enableActions();
            break;
        }
      });
    }

    function startPolling(id) {
      $scope.poller = $timeout(function () {
        getStatus(id);
      }, 3000);
    }

    $scope.$on('$destroy', function() {
      $timeout.cancel($scope.poller);
    });

    $scope.generate = function () {
      enableActions(false);
      $scope.docs[$scope.kielet.valittu] = null;
      $scope.tila = 'luodaan';
      Pdf.generoiPdf(perusteId, $scope.kielet.valittu, function(res) {
        if (res.id !== null) {
          pdfToken = res.id;
          startPolling(res.id);
        }
      }, function () {
        enableActions();
        $scope.tila = 'ei_ole';
      });
    };

    $scope.$watch('kielet.valittu', function (value) {
      fetchLatest(value);
    });
  });
