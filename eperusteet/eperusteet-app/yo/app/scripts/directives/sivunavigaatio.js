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

/**
 * Perusteprojektin sivunavigaatioelementti
 */
angular.module('eperusteApp')
  .directive('sivunavigaatio', function() {
    return {
      templateUrl: 'views/partials/sivunavigaatio.html',
      restrict: 'E',
      transclude: true,
      controller: 'sivunavigaatioCtrl'
    };
  })

  .controller('sivunavigaatioCtrl', function($rootScope, $scope, $state, SivunavigaatioService, PerusteProjektiService) {
    $scope.menuCollapsed = true;
    $rootScope.$on('$stateChangeStart', function () {
      $scope.menuCollapsed = true;
    });
    $scope.goBackToMain = function () {
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projekti.id, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {reload: true});
    };
    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };
    $scope.isHidden = function () {
      return $scope.data.piilota;
    };
    SivunavigaatioService.bind($scope);
  })

  .service('SivunavigaatioService', function ($stateParams) {
    this.data = {
      osiot: false,
      piilota: false,
      projekti: {id: 0},
      service: null
    };
    this.bind = function (scope) {
      scope.data = this.data;
    };

    /**
     * Asettaa sivunavigaation tiettyyn tilaan.
     * Suositeltu käyttöpaikka: $stateProvider.state -> onEnter
     * @param data {Object} Mahdolliset asetukset:
     *     osiot: true näyttää projektin kaikki osiot,
     *            false näyttää vain "takaisin"-linkin
     *     piilota: true piilottaa koko navigaatioelementin
     */
    this.aseta = function (data) {
      if (data.perusteprojektiTiedot) {
        this.data.service = data.perusteprojektiTiedot;
        this.setData();
      }

      if (!_.isUndefined(data.osiot)) {
        this.data.osiot = data.osiot;
      }
      this.data.piilota = !!data.piilota;
    };

    this.setData = function () {
      this.data.projekti = this.data.service.getProjekti();
      this.data.projekti.peruste =  this.data.service.getPeruste();
      this.data.projekti.peruste.sisalto =  this.data.service.getSisalto();
    };

    this.update = function () {
      var self = this;
      this.data.service.alustaPerusteenSisalto($stateParams, true).then(function () {
        self.setData();
      });
    };

  });
