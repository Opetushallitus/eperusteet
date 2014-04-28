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
      controller: 'sivunavigaatioCtrl'
    };
  })
  .controller('sivunavigaatioCtrl', function($scope, $state, SivunavigaatioService) {
    $scope.takaisin = function () {
      $state.go('perusteprojekti.editoi.sisalto', {perusteProjektiId: $scope.projekti.id});
    };
    SivunavigaatioService.sido($scope);
  })
  .service('SivunavigaatioService', function (Suoritustapa) {
    this.data = {
      osiot: false,
      piilota: false,
      projekti: {id: 0}
    };
    this.sido = function (scope) {
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
      if (data.projekti) {
        this.data.projekti = data.projekti;
        // TODO: kevyempi API jolla haetaan pelkät otsikot/linkkeihin
        // tarvittavat tiedot, ei koko sisältöä
        var that = this;
        Suoritustapa.get({perusteenId: this.data.projekti.peruste.id, suoritustapa: 'ops'}, function(vastaus) {
          that.data.projekti.peruste.sisalto = vastaus;
        });
        return;
      }
      if (!_.isUndefined(data.osiot)) {
        this.data.osiot = data.osiot;
      }
      this.data.piilota = !!data.piilota;
    };
    /**
     * Asettaa projektiobjektin, täytyy sisältää ainakin 'id'
     */
    this.asetaProjekti = function (projekti) {
      this.aseta({projekti: projekti});
    };
  });
