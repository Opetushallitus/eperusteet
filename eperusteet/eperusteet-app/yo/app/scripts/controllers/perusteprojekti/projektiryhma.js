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
  .controller('ProjektiryhmaCtrl', function($scope, $modal, $stateParams, PerusteprojektiJasenet,
    PerusteProjektiService, ColorCalculator, VariHyrra, kayttajaToiminnot) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.ryhma = {};

    PerusteprojektiJasenet.get({
      id: $stateParams.perusteProjektiId
    }, function(jasenet) {
      VariHyrra.reset();
      _.forEach(jasenet, function(j) {
        // Käyttäjän nimi
        j.$nimi = (!_.isEmpty(j.kutsumanimi) ? j.kutsumanimi : j.etunimet) + ' ' + j.sukunimi;
        j.color = VariHyrra.next();

        if (!_.isEmpty(j.yhteystiedot)) {
          // Yhteystietotyyppit
          _.forEach(_.first(j.yhteystiedot).yhteystiedot, function(yt) {
            if (yt.yhteystietoTyyppi === 'YHTEYSTIETO_SAHKOPOSTI') {
              j.$sahkoposti = yt.yhteystietoArvo;
            }
            else if (yt.yhteystietoTyyppi === 'YHTEYSTIETO_MATKAPUHELINNUMERO' &&
              !_.isEmpty(yt.yhteystietoArvo)) {
              j.$puhelinnumero = yt.yhteystietoArvo;
            }
            else if (_.isEmpty(j.$puhelinnumero) &&
              yt.yhteystietoTyyppi === 'YHTEYSTIETO_PUHELINNUMERO' &&
              !_.isEmpty(yt.yhteystietoArvo)) {
              j.$puhelinnumero = yt.yhteystietoArvo;
            }
          });
        }
      });
      $scope.ryhma = _.groupBy(jasenet, 'tehtavanimike');
    });

    $scope.nimikirjaimet = kayttajaToiminnot.nimikirjaimet;

    $scope.styleFor = function(jasen) {
      return jasen.color ? {
        'background-color': '#' + jasen.color,
        'color': ColorCalculator.readableTextColorForBg(jasen.color)
      } : {};
    };
  })
  .service('kayttajaToiminnot', function() {
    this.nimikirjaimet = function(nimi) {
      return _.reduce(nimi.split(' '), function(memo, osa) {
        return memo + (osa ? osa[0] : '');
      }, '').toUpperCase();
    };
  });
