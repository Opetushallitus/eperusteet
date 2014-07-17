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
  .directive('tree', function($compile, $state, Muodostumissaannot, Kaanna) {
    function generoiOtsikko() {
      var tosa = '{{ tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].nimi | kaanna:true }}<span ng-if="apumuuttujat.suoritustapa !== \'naytto\' && tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuus">, <b>{{ + tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuus || 0 }}</b>{{ apumuuttujat.laajuusYksikko | kaanna }}</span>';
      var editointiIkoni =
      '<span ng-click="togglaaPakollisuus(rakenne)">' +
        '  <span ng-show="!rakenne.pakollinen"><img src="images/tutkinnonosa.png" alt=""></span> ' +
        '  <span ng-show="rakenne.pakollinen"><img src="images/tutkinnonosa_pakollinen.png" alt=""></span> ' +
        '</span>';
      return '' +
        '<span ng-if="rakenne._tutkinnonOsaViite && muokkaus">' + editointiIkoni + tosa + '</span>' +
        '<span ng-if="rakenne._tutkinnonOsaViite && !muokkaus">' + editointiIkoni + '<a href="" ui-sref="root.perusteprojekti.suoritustapa.perusteenosa({ perusteenOsaId: tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite]._tutkinnonOsa, suoritustapa: apumuuttujat.suoritustapa, perusteenOsanTyyppi: \'tutkinnonosa\' })">' + tosa + '</a></span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsaViite && muokkaus"><a href="" icon-role="remove" ng-click="poista(rakenne, vanhempi)"></a></span>' +
        '<span ng-if="!rakenne._tutkinnonOsaViite && rakenne.nimi">' +
        '  <b>{{ rakenne.nimi | kaanna:true }}</b>' +
        '</span>';
    }

    return {
      restrict: 'AE',
      transclude: false,
      terminal: true,
      scope: {
        rakenne: '=',
        tutkinnonOsaViitteet: '=',
        uusiTutkinnonOsa: '=',
        vanhempi: '=',
        apumuuttujat: '=',
        muokkaus: '=',
        poistoTehtyCb: '='
      },
      link: function(scope, el) {
        scope.lisaaUusi = 0;
        scope.lisataanUuttaPerusteenOsaa = false;
        scope.scratchpad = [];
        scope.roskakori = [];

        scope.poista = function(i, a) {
          _.remove(a.osat, i);
          scope.poistoTehtyCb();
        };

        scope.togglaaPakollisuus = function(rakenne) {
          if (scope.muokkaus) {
            rakenne.pakollinen = !rakenne.pakollinen;
          }
        };

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
          var avaamattomat = _(scope.rakenne.osat).reject(function(osa) { return osa._tutkinnonOsaViite || osa.$collapsed || osa.osat.length === 0; }).size();
          if (avaamattomat !== 0) {
            _.forEach(scope.rakenne.osat, function(r) {
              if (r.osat && _.size(r.osat) > 0) {
                r.$collapsed = true;
              }
            });
          }
          else {
            _.forEach(scope.rakenne.osat, function(r) {
              if (r.osat && _.size(r.osat) > 0) {
                r.$collapsed = false;
              }
            });
          }
        };

        scope.tkaanna = function(input) {
          return _.reduce(_.map(input, function(str) {
            switch (str) {
              case '$laajuusYksikko':
                str = scope.apumuuttujat.laajuusYksikko;
                break;
              default:
                break;
            }
            return Kaanna.kaanna(str);
          }), function(str, next) {
            return str + ' ' + next;
          });
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
          },
          cancel: '.ui-state-disabled',
        };

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
        });

        scope.piilotaVirheet = function() {
          scope.apumuuttujat.piilotaVirheet = !scope.apumuuttujat.piilotaVirheet;
        };

        var varivalinta = '{ \'background\': rakenne.rooli === \'määrittelemätön\'' +
                                              '? \'#93278F\'' +
                                              ': rakenne.osat.length === 0' +
                                                '? \'#FDBB07\'' +
                                                ': rakenne.$collapsed' +
                                                  '? \'#06526c\'' +
                                                  ': \'#29ABE2\' }';

        var koonIlmaisu = '<span ng-show="rakenne.muodostumisSaanto.koko.minimi === rakenne.muodostumisSaanto.koko.maksimi">' +
                          '  {{ rakenne.muodostumisSaanto.koko.minimi || 0 }} {{ \'kpl\' | kaanna }}' +
                          '</span>' +
                          '<span ng-hide="rakenne.muodostumisSaanto.koko.minimi === rakenne.muodostumisSaanto.koko.maksimi">' +
                          '  {{ rakenne.muodostumisSaanto.koko.minimi || 0 }} - {{ rakenne.muodostumisSaanto.koko.maksimi || 0 }} {{ \'kpl\' | kaanna }}' +
                          '</span>';

        var laajuudenIlmaisu = '<span ng-show="rakenne.muodostumisSaanto.laajuus.minimi === rakenne.muodostumisSaanto.laajuus.maksimi">' +
                               '  {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }} {{ apumuuttujat.laajuusYksikko | kaanna }}' +
                               '</span>' +
                               '<span ng-hide="rakenne.muodostumisSaanto.laajuus.minimi === rakenne.muodostumisSaanto.laajuus.maksimi">' +
                               '  {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }} - {{ rakenne.muodostumisSaanto.laajuus.maksimi || 0 }} {{ apumuuttujat.laajuusYksikko | kaanna }}' +
                               '</span>';

        var optiot = '' +
          '<span ng-click="rakenne.$collapsed = rakenne.osat.length > 0 ? !rakenne.$collapsed : false" ng-if="!rakenne._tutkinnonOsaViite" class="colorbox" ng-style="' + varivalinta + '">' +
          '  <span ng-show="rakenne.rooli !== \'määrittelemätön\'">' +
          '    <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
          '    <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
          '  </span>' +
          '</span>' +
          '<div class="right">' +
          '  <div ng-if="!rakenne._tutkinnonOsaViite && muokkaus" class="right-item">' +
          '    <a href="" icon-role="edit" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)"></a>' +
          '  </div>' +
          '  <div class="pull-right" ng-if="!rakenne._tutkinnonOsaViite">' +
          '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.muodostumisSaanto.laajuus.minimi">' +
          laajuudenIlmaisu +
          '    </span>' +
          '    <span class="right-item" ng-if="rakenne.muodostumisSaanto.koko.minimi">' +
          koonIlmaisu +
          '    </span>' +
          // '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.$vaadittuLaajuus"><b>{{ rakenne.$laajuus || 0 }}</b>/<b>{{ rakenne.$vaadittuLaajuus || 0 }}</b>{{ apumuuttujat.laajuusYksikko | kaanna }}</span>' +
          // '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\' && !rakenne.$vaadittuLaajuus"><b>{{ rakenne.$laajuus || 0 }}</b>{{ apumuuttujat.laajuusYksikko | kaanna }}</span>' +
          // '    <span class="right-item"><b>{{ rakenne.osat.length }}kpl</b></span>' +
          '  </div>' +
          '</div>' +
          '<div class="left">' +
          '  <span class="tree-item">' + generoiOtsikko() + '</span>' +
          '</div>';

        var kentta = '<div ng-if="rakenne._tutkinnonOsaViite" ng-class="{ \'pointer\': muokkaus, \'huomio\': tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].$elevate }" class="bubble-osa">' + optiot + '</div>';
        kentta += '<div ng-if="!rakenne._tutkinnonOsaViite" ng-class="{ \'pointer\': muokkaus }" class="bubble">' + optiot + '</div>';
        kentta += '<div ng-model="rakenne" ng-show="muokkaus && rakenne.$virhe && !apumuuttujat.piilotaVirheet" class="virhe">' +
                  '  <span>{{ tkaanna(rakenne.$virhe.selite) }}<span ng-show="rakenne.$virhe.selite.length > 0">. </span>{{ rakenne.$virhe.virhe | kaanna }}.</span>' +
                  '</div>';

        var avaaKaikki = '<div class="pull-right">' +
                         '  <a ng-show="muokkaus" style="margin-right: 10px;" href="" ng-click="piilotaVirheet()" class="group-toggler">' +
                         '    <span ng-hide="apumuuttujat.piilotaVirheet" class="avaa-sulje"> {{ "piilota-virheet" | kaanna }}</span>' +
                         '    <span ng-show="apumuuttujat.piilotaVirheet" class="avaa-sulje"> {{ "nayta-virheet" | kaanna }}</span>' +
                         '  </a>' +
                         '  <a href="" ng-click="togglaaPolut()" class="group-toggler">' +
                         '    <span class="avaa-sulje"><img src="images/expander.png" alt=""> {{ "avaa-sulje-kaikki" | kaanna }}</span>' +
                         '  </a>' +
                         '</div>';

        var template =
          '<div class="tree-box" ng-if="!vanhempi">' +
          '  <a ng-if="zoomaus" icon-role="back" class="back" href=""></a>' +
          '  <div class="tree-otsikko">' +
          '    <h4>' +
          '      <a ng-show="muokkaus" href="" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)">' +
          '        <span class="tree-otsikko-left"><b>{{ rakenne.nimi | kaanna:true }}</b></span>' +
          '      </a>' +
          '      <span ng-hide="muokkaus"><b>{{ rakenne.nimi | kaanna:true }}</b></span>' +
          '      <span class="tree-otsikko-laajuus" ng-show="apumuuttujat.suoritustapa !== \'naytto\'"> ' +
          '        <span ng-show="muokkaus">{{ rakenne.$laajuus || 0 }} / </span>' +
          laajuudenIlmaisu +
          '      </span>' +
          avaaKaikki +
          '    </h4>' +
          '  </div>' +
          '  <div ng-show="muokkaus && rakenne.$virhe && !apumuuttujat.piilotaVirheet" class="isovirhe-otsikko">{{ tkaanna(rakenne.$virhe.selite) }}<span ng-show="rakenne.$virhe.selite.length > 0">. </span>{{ rakenne.$virhe.virhe | kaanna }}</div>' +
          '</div>' +
          '<div ng-if="vanhempi">' + kentta + '</div>' +
          '<div ng-if="rakenne.rooli !== \'määrittelemätön\'" class="collapser" ng-show="!rakenne.$collapsed">' +
          '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
          '    <li ng-repeat="osa in rakenne.osat">' +
          '      <tree apumuuttujat="apumuuttujat" muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osa-viitteet="tutkinnonOsaViitteet" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true" poisto-tehty-cb="poistoTehtyCb"></tree>' +
          '    </li>' +
          '    <li class="ui-state-disabled" ng-if="muokkaus && !vanhempi && rakenne.osat.length > 0">' +
          '      <span class="tree-anchor"></span>' +
          '    </li>' +
          '  </ul>' +
          '</div>';

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  })
  .directive('treeWrapper', function($stateParams, $state, Editointikontrollit, TutkinnonOsanTuonti, Kaanna,
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
        scope.kaytetytUniikit = {};
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
          var filtered = !_.isEmpty(input);
          scope.uniikit = _.reject(scope.kaikkiUniikit, function(yksi) {
            var nimi = Kaanna.kaanna(scope.rakenne.tutkinnonOsaViitteet[yksi._tutkinnonOsaViite].nimi).toLowerCase();
            return (filtered && nimi.indexOf(input.toLowerCase()) === -1) ||
                   (scope.piilotaKaikki && scope.kaytetytUniikit[yksi._tutkinnonOsaViite]);
          });
        };

        scope.toggleNotUsed = function () {
          scope.piilotaKaikki = !scope.piilotaKaikki;
          scope.paivitaRajaus();
        };

        function paivitaUniikit() {
          scope.uniikit = [];
          _(scope.rakenne.tutkinnonOsaViitteet)
            .reject(function(osa) { return osa.poistettu; })
            .each(function (osa) {
              var match = scope.tutkinnonOsat.rajaus &&
                _.contains(Kaanna.kaanna(osa.nimi).toLowerCase(),
                scope.tutkinnonOsat.rajaus.toLowerCase());
              if (!scope.tutkinnonOsat.rajaus || match) {
                scope.uniikit.push({_tutkinnonOsaViite: osa.id});
              }
            })
            .value();
          scope.tutkinnonOsat.multiPage = _.size(scope.uniikit) > scope.tutkinnonOsat.perSivu;
          scope.kaikkiUniikit = _.sortBy(scope.uniikit, function(osa) {
            return Kaanna.kaanna(scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].nimi).toLowerCase();
          });
          scope.uniikit = scope.kaikkiUniikit;
          scope.paivitaRajaus();
          scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy(scope.rakenne.rakenne);
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
            else {
              ryhma = _.merge(ryhma, uusiryhma);
            }
          }
          else { _.remove(scope.skratchpad, ryhma); }
        });

        scope.poista = function(i, a) {
          _.remove(i, a);
          scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy(scope.rakenne.rakenne);
        };

        scope.poistoTehtyCb = function() {
          scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy(scope.rakenne.rakenne);
        };

        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };

        scope.$watch('rakenne.$suoritustapa', function() {
          var sts = null;
          if (scope.rakenne.$peruste) {
            sts = _(scope.rakenne.$peruste.suoritustavat).filter(function(st) { return st.laajuusYksikko; }) .value();
            sts = _.zipObject(_.map(sts, 'suoritustapakoodi'), sts)[scope.rakenne.$suoritustapa];
          }

          scope.apumuuttujat = {
            suoritustapa: scope.rakenne.$suoritustapa,
            laajuusYksikko: sts ? sts.laajuusYksikko : null,
            vanhin: scope.rakenne,
            piilotaVirheet: false
          };
        });

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
          scope.sortableOptionsUnique.disabled = !scope.muokkaus;
        });
      }
    };
  });
