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
  .service('TreeCache', function() {
    var puuId = -1;
    var puu = {};

    return {
      nykyinen: function() { return puuId; },
      hae: function() { console.log('haetaan'); return _.clone(puu); },
      tallenna: function(rakenne, id) {
        if (id) {
          puu = _.clone(rakenne);
          puuId = id;
        }
      },
      puhdista: function() {
        puuId = -1;
        puu = {};
      }
    };
  })
  .directive('tree', function($compile, $state, $modal, Muodostumissaannot, PerusteenRakenne) {
    function generoiOtsikko() {
      var tosa = '{{ tutkinnonOsat[rakenne._tutkinnonOsa].nimi | kaanna:true }}<span ng-if="apumuuttujat.suoritustapa !== \'naytto\' && tutkinnonOsat[rakenne._tutkinnonOsa].laajuus">, <b>{{ + tutkinnonOsat[rakenne._tutkinnonOsa].laajuus || 0 }}</b>{{ tutkinnonOsat[rakenne._tutkinnonOsa].yksikko | kaanna }}</span>';
      return '' +
        '<span ng-if="rakenne._tutkinnonOsa && muokkaus">' + tosa + '</span>' +
        '<span ng-if="rakenne._tutkinnonOsa && !muokkaus"><a href="" ui-sref="perusteprojekti.suoritustapa.perusteenosa({ perusteenOsaId: rakenne._tutkinnonOsa, perusteenOsanTyyppi: \'tutkinnonosa\' })">' + tosa + '</a></span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsa && muokkaus"><a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></a></span>' +
        '<span ng-if="!rakenne._tutkinnonOsa && rakenne.nimi"><b>{{ rakenne.nimi | kaanna:true }}</b></span>';
    }

    return {
      restrict: 'AE',
      transclude: false,
      terminal: true,
      scope: {
        rakenne: '=',
        tutkinnonOsat: '=',
        uusiTutkinnonOsa: '=',
        vanhempi: '=',
        apumuuttujat: '=',
        muokkaus: '='
      },
      link: function(scope, el) {
        scope.lisaaUusi = 0;
        scope.lisataanUuttaPerusteenOsaa = false;
        scope.scratchpad = [];
        scope.roskakori = [];

        scope.poista = function(i, a) { _.remove(a.osat, i); };

        scope.ryhmaModaali = Muodostumissaannot.ryhmaModaali(function(ryhma, vanhempi, uusiryhma) {
          if (!scope.vanhempi) {
            scope.rakenne = uusiryhma;
          } else {
            var indeksi = scope.vanhempi.osat.indexOf(ryhma);
            if (!uusiryhma) { _.remove(scope.vanhempi.osat, ryhma); }
            else if (indeksi !== -1) { scope.vanhempi.osat[indeksi] = uusiryhma; }
          }
        });

        scope.$watch('rakenne', function(uusirakenne) {
          Muodostumissaannot.laskeLaajuudet(scope.rakenne, scope.tutkinnonOsat);
          Muodostumissaannot.validoiRyhma(uusirakenne, scope.tutkinnonOsat);
        }, true);

        scope.suljePolut = function() {
          scope.rakenne.rakenne.$collapsed = scope.suljettuViimeksi;
          PerusteenRakenne.kaikilleRakenteille(scope.rakenne.rakenne.osat, function(osa) {
            osa.$collapsed = scope.suljettuViimeksi;
          });
          scope.suljettuViimeksi = !scope.suljettuViimeksi;
        };

        scope.sortableOptions = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          //tolerance: 'pointer',
          //cursorAt: { top : 2, left: 2 },
          cursor: 'move',
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
        });

        var optiot = '' +
          '<span ng-if="!rakenne._tutkinnonOsa" class="colorbox">' +
          '  <a href="" ng-click="rakenne.$collapsed = !rakenne.$collapsed" class="group-toggler">' +
          '    <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
          '    <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
          '  </a>' +
          '</span>' +
          '<div class="right">' +
          '  <div ng-if="!rakenne._tutkinnonOsa && muokkaus" class="right-item">' +
          '    <a href="" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)"><span class="glyphicon glyphicon-pencil"></span></a>' +
          '  </div>' +
          '  <div class="pull-right" ng-if="!rakenne._tutkinnonOsa && muokkaus">' +
          '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.$vaadittuLaajuus"><b>{{ rakenne.$laajuus || 0 }}</b>/<b>{{ rakenne.$vaadittuLaajuus || 0 }}</b>ov</span>' +
          '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\'"><b>{{ rakenne.$laajuus || 0 }}</b>ov</span>' +
          '    <span class="right-item"><b>{{ rakenne.osat.length }}kpl</b></span>' +
          '  </div>' +
          '</div>' +
          '<div class="left">' +
          '  <span class="tree-item">' + generoiOtsikko() + '</span>' +
          '</div>';

        var kentta = '<div ng-if="rakenne._tutkinnonOsa" ng-class="{ \'pointer\': muokkaus }" class="bubble-osa">' + optiot + '</div>';
        kentta += '<div ng-if="!rakenne._tutkinnonOsa" ng-class="{ \'pointer\': muokkaus }" class="bubble">' + optiot + '</div>';
        kentta += '<div ng-model="rakenne" ng-show="muokkaus && rakenne.$virhe" class="virhe"><span>{{ rakenne.$virhe | translate }}</span></div>';

        var avaaKaikki = '<a href="" ng-click="rakenne.$collapsed = !rakenne.$collapsed" title="{{ \'avaa-sulje-kaikki\' | kaanna }}" class="group-toggler">' +
                         '  <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
                         '  <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
                         '</a>';

        var template =
          '<div ng-if="!vanhempi">' +
          '  <div class="otsikko">' +
          '    <h4 ng-show="muokkaus">' + avaaKaikki + ' <a href="" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)">{{ rakenne.nimi | kaanna:true }}</a><span ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.$vaadittuLaajuus">, {{ rakenne.$laajuus || 0 }} / {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}ov</span></h4>' +
          '    <h4 ng-hide="muokkaus">' + avaaKaikki + ' {{ rakenne.nimi | kaanna:true }}<span ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.$vaadittuLaajuus">, {{ rakenne.$laajuus || 0 }} / {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}ov</span></h4>' +
          '    <div ng-if="rakenne.$virhe" class="isovirhe">{{ rakenne.$virhe | kaanna }}</div>' +
          '  </div>' +
          '</div>' +
          '<div ng-if="vanhempi">' + kentta + '</div>' +
          '<div class="collapser" ng-show="!rakenne.$collapsed">' +
          '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
          '    <li ng-repeat="osa in rakenne.osat">' +
          '      <tree apumuuttujat="apumuuttujat" muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osat="tutkinnonOsat" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true"></tree>' +
          '    </li>' +
          '  </ul>' +
          '</div>';

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  })
  .directive('treeWrapper', function($stateParams, $modal, $state, Editointikontrollit, TutkinnonOsanTuonti, Kaanna,
                                     PerusteTutkinnonosa, Notifikaatiot, PerusteenRakenne, Muodostumissaannot) {

    return {
      restrict: 'AE',
      transclude: true,
      terminal: true,
      templateUrl: 'views/partials/tree.html',
      scope: {
        rakenne: '=',
        voiLiikuttaa: '=',
        ajaKaikille: '=',
        muokkaus: '='
      },
      link: function(scope) {
        scope.suljettuViimeksi = true;
        scope.lisataanUuttaOsaa = false;
        scope.uusiOsa = null;
        scope.skratchpad = [];
        scope.uniikit = [];
        scope.topredicate = 'nimi.fi';
        scope.tutkinnonOsat = {
          perSivu: 8,
          rajaus: '',
          multiPage: false,
          sivu: 1
        };

        function paivitaUniikit() {
          scope.uniikit = [];
          _.each(scope.rakenne.tutkinnonOsat, function (osa) {
            var match = scope.tutkinnonOsat.rajaus &&
              _.contains(Kaanna.kaanna(osa.nimi).toLowerCase(),
              scope.tutkinnonOsat.rajaus.toLowerCase());
            if (!scope.tutkinnonOsat.rajaus || match) {
              scope.uniikit.push({_tutkinnonOsa: osa._tutkinnonOsa});
            }
          });
          scope.tutkinnonOsat.multiPage = _.size(scope.uniikit) > scope.tutkinnonOsat.perSivu;
        }
        paivitaUniikit();

        scope.sortableOptions = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          cursor: 'move',
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.sortableOptionsUnique = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          cursor: 'move',
          stop: function() {
            paivitaUniikit();
          },
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
            // Adjust index according to pagination
            ui.item.sortable.index += (scope.tutkinnonOsat.sivu - 1) * scope.tutkinnonOsat.perSivu;
          }
        };

        scope.ryhmaModaali = Muodostumissaannot.ryhmaModaali(function(ryhma, vanhempi, uusiryhma) {
          if (uusiryhma) {
            if (ryhma === undefined) { scope.skratchpad.push(uusiryhma); }
            else { ryhma = uusiryhma; }
          }
          else { _.remove(scope.skratchpad, ryhma); }
        });

        scope.suljePolut = function() {
          scope.rakenne.rakenne.$collapsed = scope.suljettuViimeksi;
          PerusteenRakenne.kaikilleRakenteille(scope.rakenne.rakenne.osat, function(osa) {
            osa.$collapsed = scope.suljettuViimeksi;
          });
          scope.suljettuViimeksi = !scope.suljettuViimeksi;
        };

        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };

        scope.$watch('rakenne.$suoritustapa', function() {
          scope.apumuuttujat = {
            suoritustapa: scope.rakenne.$suoritustapa
          };
        });

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
          scope.sortableOptionsUnique.disabled = !scope.muokkaus;
        });

        scope.$watch('tutkinnonOsat.rajaus', _.debounce(function () {
          scope.$apply(paivitaUniikit);
        }, 200, {leading: false}));
      }
    };
  });
