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
  .directive('tree', function($compile, $state, $modal) {
    function validoiRyhma(rakenne) {
      if (!rakenne) { return; }

      delete rakenne.$virhe;

      if (rakenne.muodostumisSaanto) {
        if (rakenne.muodostumisSaanto.laajuus) {
          var msl = rakenne.muodostumisSaanto.laajuus;
          if (msl.minimi) {
            var summa = _(rakenne.osat)
              .map(function(osa) { return osa.$laajuus; })
              .reduce(function(sum, newval) { return sum + newval; });
            if (summa < msl.minimi) {
              rakenne.$virhe = 'muodostumis-rakenne-validointi-1';
            }
          }
        }

        if (rakenne.muodostumisSaanto.koko) {
          var msk = rakenne.muodostumisSaanto.koko;
          if (_.size(rakenne.osat) < msk.minimi) {
            rakenne.$virhe = 'muodostumis-rakenne-validointi-2';
          }
        }

        var tosat = _(rakenne.osat).filter(function(osa) { return osa._tutkinnonOsa; }).value();
        if (_.size(tosat) !== _(tosat).uniq('_tutkinnonOsa').size()) {
            rakenne.$virhe = 'muodostumis-rakenne-validointi-3';
        }
      }
    }

    function laskeLaajuudet(rakenne, tutkinnonOsat, root) {
      root = root || true;

      if (!rakenne) { return; }

      _.forEach(rakenne.osat, function(osa) { laskeLaajuudet(osa, tutkinnonOsat, false); });
      rakenne.$laajuus = 0;

      if (rakenne._tutkinnonOsa) {
        rakenne.$laajuus = tutkinnonOsat[rakenne._tutkinnonOsa].laajuus;
      } else if (rakenne.osat) {
        if (!root && rakenne.muodostumisSaanto && rakenne.muodostumisSaanto.laajuus && rakenne.muodostumisSaanto.laajuus.maksimi) {
          rakenne.$laajuus = rakenne.muodostumisSaanto.laajuus.maksimi;
        } else if (rakenne.osat.length > 0 && rakenne.muodostumisSaanto && rakenne.muodostumisSaanto.koko && rakenne.muodostumisSaanto.koko.maksimi) {
          var set = [];
          for (var i = 0; i < rakenne.muodostumisSaanto.koko.maksimi; ++i) {
            var newindex = 0;
            for (var j = 0; j < rakenne.osat.length; ++j) {
              if (rakenne.osat[j].$laajuus > rakenne.osat[newindex].$laajuus && set.indexOf(j) === -1) {
                newindex = j;
              }
            }
            set.push(newindex);
          }
          _.forEach(set, function(s) { rakenne.$laajuus += rakenne.osat[s].$laajuus; });
        } else { _.forEach(rakenne.osat, function(osa) { rakenne.$laajuus += osa.$laajuus || 0; }); }
      } else {
        console.log('jokin meni pieleen');
      }
    }

    function generoiOtsikko() {
      var tosa = 'tutkinnonOsat[rakenne._tutkinnonOsa]';
      return '' +
        '<span ng-if="rakenne._tutkinnonOsa">{{ ' + tosa + '.nimi | kaanna }}, <b>{{' + tosa + '.laajuus || 0 }}</b>ov</span>' +
        '<span class="pull-right" ng-if="rakenne._tutkinnonOsa"><a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></a></span>' +
        '<span ng-if="!rakenne._tutkinnonOsa && rakenne.nimi"><b>{{ rakenne.nimi | kaanna }}</b></span>';
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
        'muokkaus': '='
      },
      link: function(scope, el) {
        scope.lisaaUusi = 0;
        scope.lisataanUuttaPerusteenOsaa = false;
        scope.scratchpad = [];
        scope.roskakori = [];

        // function liitaUusiTutkinnonOsa() {
        //   scope.rakenne.osat.push({
        //     otsikko: { fi: 'Uusi' },
        //     kuvaus: { fi: '' },
        //     _tutkinnonOsa: 1337,
        //     muodostumisSaanto: {
        //       maara: 10,
        //       yksikko: 'ov',
        //       tyyppi: 'laajuus',
        //       pakollinen: false
        //     }
        //   });
        // }

        scope.poista = function(i, a) { _.remove(a.osat, i); };

        scope.ryhmaModaali = function(ryhma, vanhempi) {
          ryhma = ryhma || {};
          $modal.open({
            templateUrl: 'views/modals/ryhmaModal.html',
            controller: 'MuodostumisryhmaModalCtrl',
            resolve: {
              ryhma: function() { return ryhma; },
              vanhempi: function() { return vanhempi; },
            }
          }).result.then(function(uusiryhma) {
            if (!scope.vanhempi) {
              scope.rakenne = uusiryhma;
            } else {
              var indeksi = scope.vanhempi.osat.indexOf(ryhma);
              if (!uusiryhma) { _.remove(scope.vanhempi.osat, ryhma); }
              else if (indeksi !== -1) { scope.vanhempi.osat[indeksi] = uusiryhma; }
            }
          });
        };

        scope.$watch('rakenne', function(uusirakenne) {
          laskeLaajuudet(scope.rakenne, scope.tutkinnonOsat);
          validoiRyhma(uusirakenne, scope.tutkinnonOsat);
        }, true);

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

        // scope.liitaUusiTutkinnonOsa = liitaUusiTutkinnonOsa;

        var optiot = '' +
          '<span ng-if="!rakenne._tutkinnonOsa" class="colorbox">' +
          '  <a href="" ng-click="rakenne.$collapsed = !rakenne.$collapsed">' +
          '    <span ng-hide="rakenne.$collapsed" class="glyphicon glyphicon-chevron-up"></span>' +
          '    <span ng-show="rakenne.$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
          '  </a>' +
          '</span>' +
          '<div class="left">' +
          '  <span class="tree-item">' + generoiOtsikko() + '</span>' +
          '</div>' +
          '<div class="right">' +
          '  <div ng-if="!rakenne._tutkinnonOsa && muokkaus" class="right-item">' +
          '    <a href="" ng-click="ryhmaModaali(rakenne, vanhempi)"><span class="glyphicon glyphicon-pencil"></span></a>' +
          '  </div>' +
          '  <div class="pull-right" ng-if="!rakenne._tutkinnonOsa && muokkaus">' +
          '    <span class="right-item" ng-if="!rakenne.muodostumisSaanto.laajuus"><b>{{ rakenne.$laajuus }}ov</b></span>' +
          '    <span class="right-item" ng-if="rakenne.muodostumisSaanto && rakenne.muodostumisSaanto.laajuus"><b>{{ rakenne.$laajuus }}/{{ rakenne.muodostumisSaanto.laajuus.minimi }}ov</b></span>' +
          '    <span class="right-item" ng-if="rakenne.muodostumisSaanto && rakenne.muodostumisSaanto.koko"><b>{{ rakenne.osat.length }}/{{ rakenne.muodostumisSaanto.koko.minimi }}kpl</b></span>' +
          '  </div>' +
          '</div>';

        var kentta = '<div ng-if="rakenne._tutkinnonOsa" class="bubble-osa">' + optiot + '</div>';
        kentta += '<div ng-if="!rakenne._tutkinnonOsa" class="bubble">' + optiot + '</div>';
        kentta += '<div ng-model="rakenne" ng-show="muokkaus && rakenne.$virhe" class="virhe"><span>{{ rakenne.$virhe | translate }}</span></div>';

        var template = '';

        template =
          '<div>' +
          kentta +
          '</div>';

        template =
          '<div ng-if="!vanhempi">' +
          '  <div class="otsikko">' +
          '    <h4><a href="" ng-click="ryhmaModaali(rakenne, vanhempi)">{{ rakenne.nimi || \'perusteella-ei-nimeä\' | kaanna }}</a>, {{ rakenne.$laajuus }} / {{ rakenne.muodostumisSaanto.laajuus.minimi || 0 }}ov</h4>' +
          '    <div ng-if="rakenne.$virhe" class="isovirhe">{{ rakenne.$virhe | kaanna }}</div>' +
          '  </div>' +
          '</div>' +
          '<div ng-if="vanhempi">' + kentta + '</div>' +
          '<div ng-show="!rakenne.$collapsed">' +
          '  <ul ng-if="rakenne.osat !== undefined" ui-sortable="sortableOptions" id="tree-sortable" class="tree-group" ng-model="rakenne.osat">' +
          '    <li ng-repeat="osa in rakenne.osat">' +
          '      <tree muokkaus="muokkaus" rakenne="osa" vanhempi="rakenne" tutkinnon-osat="tutkinnonOsat" uusi-tutkinnon-osa="uusiTutkinnonOsa" ng-init="notfirst = true"></tree>' +
          '    </li>' +
          '  </ul>' +
          '</div>';

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  })
  .directive('treeWrapper', function($modal, Editointikontrollit, TutkinnonOsanTuonti, Kaanna) {
    function kaikilleRakenteille(rakenne, f) {
      if (!rakenne || !f) { return; }
      _.forEach(rakenne, function(r) {
        kaikilleRakenteille(r.osat, f);
        f(r);
      });
    }

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
        scope.tosarajaus = '';

        function paivitaUniikit() {
          scope.uniikit = [];
          scope.uniikit = _.map(scope.rakenne.tutkinnonOsat, function(osa) {
            return {  _tutkinnonOsa: osa._tutkinnonOsa };
          });
        }
        paivitaUniikit();

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

        scope.sortableOptionsUnique = {
          placeholder: 'placeholder',
          connectWith: '.tree-group',
          disabled: !scope.muokkaus,
          delay: 100,
          //tolerance: 'pointer',
          //cursorAt: { top : 2, left: 2 },
          cursor: 'move',
          stop: function() { paivitaUniikit(); },
          start: function(e, ui) {
            ui.placeholder.html('<div class="group-placeholder"></div>');
          }
        };

        scope.ryhmaModaali = function(ryhma, vanhempi) {
          $modal.open({
            templateUrl: 'views/modals/ryhmaModal.html',
            controller: 'MuodostumisryhmaModalCtrl',
            resolve: {
              ryhma: function() { return ryhma; },
              vanhempi: function() { return vanhempi; },
            }
          }).result.then(function(uusiryhma) {
            if (uusiryhma) {
              if (ryhma === undefined) { scope.skratchpad.push(uusiryhma); }
              else { ryhma = uusiryhma; }
            } else {
              _.remove(scope.skratchpad, ryhma);
            }
          });
        };

        scope.paivitaRajaus = function(rajaus) { scope.tosarajaus = rajaus; };
        scope.rajaaTutkinnonOsia = function(haku) { return Kaanna.kaanna(haku.nimi).indexOf(scope.tosarajaus) !== -1; };

        scope.suljePolut = function() {
          scope.rakenne.rakenne.$collapsed = scope.suljettuViimeksi;
          kaikilleRakenteille(scope.rakenne.rakenne.osat, function(osa) {
            osa.$collapsed = scope.suljettuViimeksi;
          });
          scope.suljettuViimeksi = !scope.suljettuViimeksi;
        };

        scope.tuoTutkinnonosa = TutkinnonOsanTuonti.modaali('ops', function(osat) {
          _.forEach(osat, function(osa) { scope.skratchpad.push(osa); });
          paivitaUniikit();
        });

        Editointikontrollit.registerAdditionalSaveCallback(function() { scope.lisataanUuttaOsaa = false; });

        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };
      }
    };
  })
  .controller('MuodostumisryhmaModalCtrl', function($scope, $modalInstance, ryhma, vanhempi) {
    $scope.vanhempi = vanhempi;

    $scope.ms = {
      laajuus: ryhma && ryhma.muodostumisSaanto && ryhma.muodostumisSaanto.laajuus,
      koko: ryhma && ryhma.muodostumisSaanto && ryhma.muodostumisSaanto.koko,
    };

    $scope.ryhma = ryhma ? angular.copy(ryhma) : {};
    if (!$scope.ryhma.muodostumisSaanto) { $scope.ryhma.muodostumisSaanto = {}; }
    if (!$scope.ryhma.nimi) { $scope.ryhma.nimi = {}; }
    if (!$scope.ryhma.kuvaus) { $scope.ryhma.kuvaus = {}; }

    $scope.ok = function(uusiryhma) {
      if (uusiryhma) {
        if (uusiryhma.osat === undefined) { uusiryhma.osat = []; }
        if (!$scope.ms.laajus) { uusiryhma = _.omit(uusiryhma, 'muodostumisSaanto.laajuus'); }
        if (!$scope.ms.koko) { uusiryhma = _.omit(uusiryhma, 'muodostumisSaanto.koko'); }
      }
      $modalInstance.close(uusiryhma);
    };
    $scope.peruuta = function() { $modalInstance.dismiss(); };
  });
