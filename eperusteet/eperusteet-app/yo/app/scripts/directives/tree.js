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
  .directive('tree', function($compile, $state, Muodostumissaannot, Kaanna, TreeDragAndDrop, $translate, Algoritmit) {
    function generoiOtsikko() {
      var tosa = '{{ tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].nimi || "nimetön" | kaanna }}<span ng-if="apumuuttujat.suoritustapa !== \'naytto\' && tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuus">, <b>{{ + tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].laajuus || 0 }}</b>{{ apumuuttujat.laajuusYksikko | kaanna }}</span>';
      var editointiIkoni =
      '<span ng-click="togglaaPakollisuus(rakenne)">' +
        '  <span ng-show="!rakenne.pakollinen"><img src="images/tutkinnonosa.png" alt=""></span> ' +
        '  <span ng-show="rakenne.pakollinen"><img src="images/tutkinnonosa_pakollinen.png" alt=""></span> ' +
        '</span>';
      return '' +
        '<span ng-if="rakenne._tutkinnonOsaViite && muokkaus">' + editointiIkoni + tosa + '</span>' +
        '<span ng-if="rakenne._tutkinnonOsaViite && !muokkaus">' + editointiIkoni +
        '  <a ng-if="esitystilassa" href="" ui-sref="root.esitys.peruste.tutkinnonosa({ id: rakenne._tutkinnonOsaViite, suoritustapa: apumuuttujat.suoritustapa })">' + tosa + '</a>' +
        '  <a ng-if="!esitystilassa" href="" ui-sref="root.perusteprojekti.suoritustapa.perusteenosa({ perusteenOsaId: tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite]._tutkinnonOsa, suoritustapa: apumuuttujat.suoritustapa, perusteenOsanTyyppi: \'tutkinnonosa\' })">' + tosa + '</a>' +
        '</span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsaViite && muokkaus"><a class="action-link" icon-role="remove" ng-click="poista(rakenne, vanhempi)"></a></span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsaViite && muokkaus"><a class="action-link" icon-role="edit" ng-click="rakenneosaModaali(rakenne)"></a></span>' +
        '<span ng-if="!rakenne._tutkinnonOsaViite && rakenne.nimi">' +
        '  <b>{{ rakenne.nimi || "nimetön" | kaanna }}</b>' +
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
      controller: function($scope) {
        $scope.lisaaUusi = 0;
        $scope.lisataanUuttaPerusteenOsaa = false;
        $scope.scratchpad = [];
        $scope.roskakori = [];
        $scope.esitystilassa = $state.includes('**.esitys.**');
        $scope.lang = $translate.use() || $translate.preferredLanguage();
      },
      link: function(scope, el) {
        scope.poista = function(i, a) {
          _.remove(a.osat, i);
          scope.poistoTehtyCb();
        };

        scope.rakenneosaModaali = Muodostumissaannot.rakenneosaModaali(function(rakenneosa) {
          if (rakenneosa) {
            scope.rakenne = rakenneosa;
          }
        });

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

        scope.togglaaKuvaukset = function() {
          var jokuAuki = false;
          Algoritmit.kaikilleLapsisolmuille(scope.rakenne, 'osat', function(osa) {
            if (osa.$showKuvaus) {
              jokuAuki = true;
              return true;
            }
          });
          Algoritmit.kaikilleLapsisolmuille(scope.rakenne, 'osat', function(osa) { osa.$showKuvaus = !jokuAuki; });
        };

        scope.togglaaPolut = function() {
          var avaamattomat = _(scope.rakenne.osat).reject(function(osa) {
            return osa._tutkinnonOsaViite || osa.$collapsed || osa.osat.length === 0;
          }).size();

          _.forEach(scope.rakenne.osat, function(r) {
            if (r.osat && _.size(r.osat) > 0) {
              r.$collapsed = avaamattomat !== 0;
            }
          });
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

        // Drag & drop: puun sisällä
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
          update: TreeDragAndDrop.update
        };

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
        });

        scope.piilotaVirheet = function() {
          scope.apumuuttujat.piilotaVirheet = !scope.apumuuttujat.piilotaVirheet;
        };

        var varivalinta = 'ng-class="{maarittelematon: rakenne.rooli === \'määrittelemätön\', tyhja: rakenne.osat.length === 0, ' +
            'suljettu: rakenne.$collapsed, osaamisala: rakenne.rooli === \'osaamisala\'}"';

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
          '<span ng-click="rakenne.$collapsed = rakenne.osat.length > 0 ? !rakenne.$collapsed : false" ng-if="!rakenne._tutkinnonOsaViite" class="colorbox" ' + varivalinta + '>' +
          '  <span ng-show="rakenne.rooli !== \'määrittelemätön\'">' +
          '    <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
          '    <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
          '  </span>' +
          '</span>' +
          '<div class="right">' +
          '  <div ng-if="!rakenne._tutkinnonOsaViite && muokkaus" class="right-item">' +
          '    <a class="action-link" icon-role="edit" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)"></a>' +
          '  </div>' +
          '  <div class="pull-right" ng-if="!rakenne._tutkinnonOsaViite">' +
          '    <span class="right-item" ng-show="apumuuttujat.suoritustapa !== \'naytto\' && rakenne.muodostumisSaanto.laajuus.minimi">' +
          laajuudenIlmaisu +
          '    </span>' +
          '    <span class="right-item" ng-if="rakenne.muodostumisSaanto.koko.minimi">' +
          koonIlmaisu +
          '    </span>' +
          '  </div>' +
          '</div>' +
          '<div class="left">' +
          '  <span ng-class="{ \'pointer\': muokkaus }">' + generoiOtsikko() + '</span>' +
          '</div>';

        var kentta =
          '<div ng-if="rakenne._tutkinnonOsaViite" ' +
          '     ng-class="{ \'pointer\': muokkaus, \'huomio\': tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].$elevate || (apumuuttujat.haku && tutkinnonOsaViitteet[rakenne._tutkinnonOsaViite].$matched) }" class="bubble-osa">' +
            optiot +
          '</div>' +
          '<div ng-if="!rakenne._tutkinnonOsaViite" ng-class="{ \'pointer\': muokkaus }" class="bubble">' + optiot + '</div>' +
          '<div ng-model="rakenne" ng-show="rakenne.kuvaus && rakenne.kuvaus[lang].length > 0" class="kuvaus">' +
          '  <div class="kuvausteksti" ng-class="{ \'text-truncated\': !rakenne.$showKuvaus }">{{ rakenne.kuvaus | kaanna }}</div>' +
          '  <div class="avausnappi" ng-click="rakenne.$showKuvaus = !rakenne.$showKuvaus" ng-attr-title="{{rakenne.$showKuvaus && (\'Piilota ryhmän kuvaus\'|kaanna) || (\'Näytä ryhmän kuvaus\'|kaanna)}}">' +
          '  <div class="avausnappi-painike">&hellip;</div></div>' +
          '</div>' +
          '<div ng-model="rakenne" ng-show="muokkaus && rakenne.$virhe && !apumuuttujat.piilotaVirheet" class="virhe">' +
          '  <span>{{ tkaanna(rakenne.$virhe.selite) }}<span ng-show="rakenne.$virhe.selite.length > 0">. </span>{{ rakenne.$virhe.virhe | kaanna }}.</span>' +
          '</div>';

        var avaaKaikki = '<div class="pull-right">' +
                         '  <a ng-show="muokkaus && rakenne.$virheetMaara > 0" style="margin-right: 10px;" href="" ng-click="piilotaVirheet()" class="group-toggler">' +
                         '    <span ng-hide="apumuuttujat.piilotaVirheet" class="avaa-sulje"> {{ "piilota-virheet" | kaanna }}</span>' +
                         '    <span ng-show="apumuuttujat.piilotaVirheet" class="avaa-sulje"> {{ "nayta-virheet" | kaanna }}</span>' +
                         '  </a>' +
                         '  <a href="" ng-click="togglaaKuvaukset()" class="group-toggler">' +
                         '    <span><span icon-role="book"></span>{{ "nayta-kuvaukset" | kaanna }}</span>' +
                         '    ' +
                         '  </a>' +
                         '  <a href="" ng-click="togglaaPolut()" class="group-toggler">' +
                         '    <span class="avaa-sulje"><img src="images/expander.png" alt="">{{ "avaa-sulje-kaikki" | kaanna }}</span>' +
                         '  </a>' +
                         '</div>';

        var template = '' +
          // generoiKuvauksetAsetin() +
          '<div ng-if="!vanhempi">' +
          '  <div class="ylapainikkeet">' +
          '    <span class="rakenne-nimi">{{ apumuuttujat.peruste.nimi | kaanna }}' +
          '    <span ng-if="rakenne.muodostumisSaanto && rakenne.muodostumisSaanto.laajuus">' +
          '      <span ng-if="rakenne.$laajuus">{{ rakenne.$laajuus }} / </span>' +
          '      <span ng-if="rakenne.muodostumisSaanto.laajuus.minimi">' +
          '        {{ rakenne.muodostumisSaanto.laajuus.minimi }}' +
          '      </span>' +
          '      <span ng-if="rakenne.muodostumisSaanto.laajuus.maksimi && rakenne.muodostumisSaanto.laajuus.minimi !== rakenne.muodostumisSaanto.laajuus.maksimi">' +
          '        - {{ rakenne.muodostumisSaanto.laajuus.maksimi }}' +
          '      </span>' +
          '      {{ apumuuttujat.laajuusYksikko | kaanna }}' +
          '    </span></span>' +
          '    <a href="" ng-show="muokkaus" ng-click="ryhmaModaali(apumuuttujat.suoritustapa, rakenne, vanhempi)" kaanna>muokkaa-muodostumissääntöjä</button>' +
          '    <a ng-if="zoomaus" icon-role="back" class="back" href=""></a>' +
          avaaKaikki +
          '  </div>' +
          '  <div><div class="tree-yliviiva"></div></div>' +
          '  <div ng-show="muokkaus && rakenne.$virhe && !apumuuttujat.piilotaVirheet" class="isovirhe-otsikko">{{ tkaanna(rakenne.$virhe.selite) }}<span ng-show="rakenne.$virhe.selite.length > 0">. </span>{{ rakenne.$virhe.virhe | kaanna }}</div>' +
          '</div>' +
          '<div ng-if="vanhempi">' + kentta + '</div>' +
          '<div ng-if="rakenne.rooli !== \'määrittelemätön\'" class="collapser" ng-show="!rakenne.$collapsed">' +
          '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
          '    <li ng-repeat="osa in rakenne.osat" class="tree-list-item">' +
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
                                     PerusteTutkinnonosa, Notifikaatiot, PerusteenRakenne, Muodostumissaannot,
                                     Algoritmit, TreeDragAndDrop) {
    return {
      restrict: 'AE',
      transclude: true,
      terminal: true,
      templateUrl: 'views/partials/tree.html',
      scope: {
        rakenne: '=',
        voiLiikuttaa: '=',
        ajaKaikille: '=',
        muokkaus: '=',
        esitys: '=?'
      },
      controller: function($scope) {
        $scope.suljettuViimeksi = true;
        $scope.lisataanUuttaOsaa = false;
        $scope.uusiOsa = null;
        $scope.skratchpad = [];
        $scope.uniikit = [];
        $scope.kaytetytUniikit = {};
        $scope.kaikkiUniikit = [];
        $scope.topredicate = 'nimi.fi';
        $scope.tosarajaus = '';

        $scope.tutkinnonOsat = {
          perSivu: 8,
          rajaus: '',
          multiPage: false,
          sivu: 1
        };
      },
      link: function(scope) {
        scope.paivitaTekstiRajaus = function (value) {
          if (!_.isEmpty(value)) {
            PerusteenRakenne.kaikilleRakenteille(scope.rakenne.rakenne, function(item) {
              // 1. Find matches
              item.$collapsed = true;
              var osa = scope.rakenne.tutkinnonOsaViitteet[item._tutkinnonOsaViite];
              if (osa) {
                osa.$matched = Algoritmit.rajausVertailu(value, osa, 'nimi');
              }
            });
            PerusteenRakenne.kaikilleRakenteille(scope.rakenne.rakenne, function(item) {
              // 2. Uncollapse parents of matched
              var osa = scope.rakenne.tutkinnonOsaViitteet[item._tutkinnonOsaViite];
              if (osa && osa.$matched) {
                var parent = item.$parent;
                while (parent) {
                  if (parent.$parent) {
                    parent.$collapsed = false;
                  }
                  parent = parent.$parent;
                }
              }
            });
          } else {
            // Uncollapse all when search is cleared
            PerusteenRakenne.kaikilleRakenteille(scope.rakenne.rakenne, function(item) {
              item.$collapsed = false;
            });
          }
        };

        scope.paivitaRajaus = function(input) {
          input = input === undefined ? scope.tosarajaus : input;
          scope.tosarajaus = input;
          var filtered = !_.isEmpty(input);
          scope.uniikit = _.reject(scope.kaikkiUniikit, function(yksi) {
            var nimi = (Kaanna.kaanna(scope.rakenne.tutkinnonOsaViitteet[yksi._tutkinnonOsaViite].nimi) || '').toLowerCase();
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
            return (Kaanna.kaanna(scope.rakenne.tutkinnonOsaViitteet[osa._tutkinnonOsaViite].nimi) || '').toLowerCase();
          });
          scope.uniikit = scope.kaikkiUniikit;
          scope.paivitaRajaus();
          scope.kaytetytUniikit = PerusteenRakenne.puustaLoytyy(scope.rakenne.rakenne);
        }
        paivitaUniikit();

        // Drag & drop: Leikelauta <-> puu
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
          },
          update: TreeDragAndDrop.update
        };

        // Drag & drop: Tutkinnon osat <-> puu
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

        scope.$watch('skratchpad.length', function (value) {
          Muodostumissaannot.skratchpadNotEmpty(value > 0);
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
            sts = _(scope.rakenne.$peruste.suoritustavat).filter(function(st) { return st.laajuusYksikko; }).value();
            sts = _.zipObject(_.map(sts, 'suoritustapakoodi'), sts)[scope.rakenne.$suoritustapa];
          }

          scope.apumuuttujat = {
            suoritustapa: scope.rakenne.$suoritustapa,
            laajuusYksikko: sts ? sts.laajuusYksikko : null,
            vanhin: scope.rakenne,
            piilotaVirheet: true,
            peruste: scope.rakenne.$peruste
          };
        });

        scope.$watch('muokkaus', function() {
          scope.sortableOptions.disabled = !scope.muokkaus;
          scope.sortableOptionsUnique.disabled = !scope.muokkaus;
          if (!scope.muokkaus) {
            scope.skratchpad = [];
          }
        });

        scope.$watch('apumuuttujat.haku', function (value) {
          scope.paivitaTekstiRajaus(value);
        });
      }
    };
  })

  .config(function ($tooltipProvider) {
    $tooltipProvider.setTriggers({
        'mouseenter': 'mouseleave',
        'click': 'click',
        'focus': 'blur',
        'never': 'mouseleave',
        'show': 'hide'
    });
  })

  .service('TreeDragAndDrop', function (Notifikaatiot, $timeout) {
    var NODESELECTOR = '.tree-list-item';
    /*
     * Aito osaamisalaryhmä: ryhmä, joka on itse tyyppiä osaamisala
     * Osaamisalaryhmä: aito osaamisalaryhmä tai ryhmä, jonka mikä tahansa jälkeläinen on aito osaamisalaryhmä.
     * Osaamisalaryhmää ei voida asettaa puuhun jos mikä tahansa lisäämiskohdan edeltäjä on aito osaamisalaryhmä
     */
    function hasOsaamisala(item) {
      return !_.isEmpty(item.osaamisala);
    }

    function isOsaamisalaRyhma(item) {
      if (hasOsaamisala(item)) {
        return true;
      }
      return _.any(item.osat, function (child) {
        return isOsaamisalaRyhma(child);
      });
    }

    function parentsOrSelfHaveOsaamisala(node, item) {
      if (!item || !item.osa) {
        return false;
      }
      if (hasOsaamisala(item.osa)) {
        return true;
      }
      var parent = node.parent().closest(NODESELECTOR);
      return parentsOrSelfHaveOsaamisala(parent, parent ? parent.scope() : null);
    }

    this.update = function(e, ui) {
      var itemScope = ui.item.scope();
      var draggedHasOsaamisala = itemScope && itemScope.osa && isOsaamisalaRyhma(itemScope.osa);
      if (draggedHasOsaamisala) {
        var target = ui.item.sortable.droptarget;
        var listItem = target.closest(NODESELECTOR);
        var parentScope = listItem ? listItem.scope() : null;
        if (parentsOrSelfHaveOsaamisala(listItem, parentScope)) {
          var parent = listItem.find('.bubble').first();
          var el = angular.element('#osaamisala-varoitus');
          var pos = parent.offset();
          el.offset({top: pos.top, left: pos.left + 200});
          el.trigger('show');
          $timeout(function () {
           el.trigger('hide');
          }, 5000);
          ui.item.sortable.cancel();
        }
      }
    };
  });
