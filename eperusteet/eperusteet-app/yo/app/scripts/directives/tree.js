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
/*global $*/

angular.module('eperusteApp')
  .directive('tree', function($compile, $state, $modal, Muodostumissaannot) {
    function generoiOtsikko() {
      var tosa = '{{ tutkinnonOsat[rakenne._tutkinnonOsa].nimi | kaanna:true }}<span ng-if="apumuuttujat.suoritustapa !== \'naytto\' && tutkinnonOsat[rakenne._tutkinnonOsa].laajuus">, <b>{{ + tutkinnonOsat[rakenne._tutkinnonOsa].laajuus || 0 }}</b>{{ tutkinnonOsat[rakenne._tutkinnonOsa].yksikko | kaanna }}</span>';
      var editointiIkoni = '<img src="images/tutkinnonosa.png" alt=""> ';
      return '' +
        '<span ng-if="rakenne._tutkinnonOsa && muokkaus">' + editointiIkoni + tosa + '</span>' +
        '<span ng-if="rakenne._tutkinnonOsa && !muokkaus">' + editointiIkoni + '<a href="" ui-sref="perusteprojekti.suoritustapa.perusteenosa({ perusteenOsaId: rakenne._tutkinnonOsa, perusteenOsanTyyppi: \'tutkinnonosa\' })">' + tosa + '</a></span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsa && muokkaus"><a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></a></span>' +
        '<span ng-if="!rakenne._tutkinnonOsa && rakenne.nimi">' +
        '  <b>{{ rakenne.nimi | kaanna:true }}</b>' +
        '</span>';
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

        scope.togglaaPolut = function() {
          var avaamattomat = _(scope.rakenne.osat).reject(function(osa) { return osa._tutkinnonOsa || osa.$collapsed || osa.osat.length === 0; }).size();
          if (avaamattomat !== 0) {
            _.forEach(scope.rakenne.osat, function(r) {
              if (r.osat && _.size(r.osat) > 0) {
                r.$collapsed = true;
              }
            });
          } else {
            _.forEach(scope.rakenne.osat, function(r) {
              if (r.osat && _.size(r.osat) > 0) {
                r.$collapsed = false;
              }
            });
          }
        };

        scope.sortableOptions = {
          connectWith: '.tree-group',
          cursor: 'move',
          cursorAt: { top : 2, left: 2 },
          delay: 100,
          disabled: !scope.muokkaus,
          placeholder: 'placeholder',
          tolerance: 'pointer',
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
        });

        var optiot = '' +
          '<span ng-click="rakenne.$collapsed = rakenne.osat.length > 0 ? !rakenne.$collapsed : false" ng-if="!rakenne._tutkinnonOsa" class="colorbox" ng-style="{ \'background\': rakenne.osat.length === 0 ? \'#FDBB07\' : rakenne.$collapsed ? \'#06526c\' : \'#29ABE2\' }">' +
          '  <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
          '  <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
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

        var avaaKaikki = '<div class="pull-right">' +
                         '  <a href="" ng-click="togglaaPolut()" class="group-toggler">' +
                         '    <span class="avaa-sulje"><img src="images/expander.png" alt=""> {{ "avaa-sulje-kaikki" | kaanna }}</span>' +
                         '  </a>' +
                         '</div>';

        var template =
          '<div class="tree-box" ng-if="!vanhempi">' +
          '  <a ng-if="zoomaus" class="back" href=""><span class="glyphicon glyphicon-chevron-left"></span></a>' +
          '  <div class="tree-otsikko">' +
          '    <h4>' +
          '      <a ng-show="muokkaus" href="" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)">' +
          '        <span class="tree-otsikko-left"><b>{{ rakenne.nimi | kaanna:true }}</b></span>' +
          '      </a>' +
          '      <span ng-hide="muokkaus"><b>{{ rakenne.nimi | kaanna:true }}</b></span>' +
          '      <span class="tree-otsikko-laajuus" ng-show="apumuuttujat.suoritustapa !== \'naytto\'"> ' +
          '        (<b>{{ rakenne.$laajuus || 0 }}</b> / ' +
          '        <span ng-show="rakenne.muodostumisSaanto.laajuus.minimi === rakenne.muodostumisSaanto.laajuus.maksimi"><b>{{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}</b>ov)</span>' +
          '        <span ng-hide="rakenne.muodostumisSaanto.laajuus.minimi === rakenne.muodostumisSaanto.laajuus.maksimi"><b>{{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}</b> - {{ rakenne.muodostumisSaanto.laajuus.maksimi || 0 }}ov)</span>' +
          '      </span>' +
          avaaKaikki +
          '    </h4>' +
          '  </div>' +
          '  <div ng-show="muokkaus && rakenne.$virhe" class="isovirhe-otsikko">{{ rakenne.$virhe | kaanna }}</div>' +
          '</div>' +
          '<div ng-if="vanhempi">' + kentta + '</div>' +
          '<div class="collapser" ng-show="!rakenne.$collapsed">' +
          '<ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
          '<li ng-repeat="osa in rakenne.osat">' +
          '<tree apumuuttujat="apumuuttujat" muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osat="tutkinnonOsat" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true"></tree>' +
          '</li>' +
          '</ul>' +
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
        scope.kaikkiUniikit = [];
        scope.topredicate = 'nimi.fi';
        scope.tosarajaus = '';
        scope.tutkinnonOsat = {
          perSivu: 8,
          rajaus: '',
          multiPage: false,
          sivu: 1
        };

        scope.paivitaRajaus = function(input) {
          input = input === undefined ? scope.tosarajaus : input;
          scope.tosarajaus = input;
          if (_.isEmpty(input)) {
            scope.uniikit = scope.kaikkiUniikit;
          } else {
            scope.uniikit = _.reject(scope.kaikkiUniikit, function(yksi) {
              var nimi = Kaanna.kaanna(scope.rakenne.tutkinnonOsat[yksi._tutkinnonOsa].nimi).toLowerCase();
              return nimi.indexOf(input.toLowerCase()) === -1;
            });
          }
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
          scope.kaikkiUniikit = _.sortBy(scope.uniikit, function(osa) {
            return Kaanna.kaanna(scope.rakenne.tutkinnonOsat[osa._tutkinnonOsa].nimi);
          });
          scope.uniikit = scope.kaikkiUniikit;
          scope.paivitaRajaus();
        }
        paivitaUniikit();

        scope.sortableOptions = {
          connectWith: '.tree-group',
          cursor: 'move',
          cursorAt: { top : 2, left: 2 },
          delay: 100,
          disabled: !scope.muokkaus,
          placeholder: 'placeholder',
          tolerance: 'pointer',
          stop: function() {
            PerusteenRakenne.kaikilleRakenteille(scope.rakenne.rakenne, function(r) {
              delete r.$uusi;
            });
          },
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.sortableOptionsUnique = {
          connectWith: '.tree-group',
          cursor: 'move',
          cursorAt: { top : 2, left: 2 },
          delay: 100,
          disabled: !scope.muokkaus,
          placeholder: 'placeholder',
          tolerance: 'pointer',
          // http://stackoverflow.com/questions/6940390/how-do-i-duplicate-item-when-using-jquery-sortable
          helper: function(el, ui) {
            this.copyHelper = ui.clone().insertAfter(ui);
            $(this).data('copied', false);
            return ui.clone();
          },
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
            if (ryhma === undefined) {
              uusiryhma.$uusi = true;
              scope.skratchpad.push(uusiryhma);
            }
            else { _.merge(ryhma, uusiryhma); }
          }
          else { _.remove(scope.skratchpad, ryhma); }
        });

        scope.poista = function(i, a) {
          _.remove(i, a);
        };
        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };

        scope.$watch('rakenne.$suoritustapa', function() {
          scope.apumuuttujat = {
            suoritustapa: scope.rakenne.$suoritustapa,
            vanhin: scope.rakenne
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
