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
  .directive('tree', function($compile, $state, TutkinnonOsanTuonti) {
    function swap(container, from, to) {
      if (container && from >= 0 && to >= 0 && from < _.size(container) && to < _.size(container)) {
        var temp = container[to];
        container[to] = container[from];
        container[from] = temp;
      }
    }

    function generoiPiilotusPainike() {
      return '<a ng-show="!rakenne._perusteenOsa" href="" ng-click="rakenne.collapsed = !rakenne.collapsed">' +
        '<span ng-hide="rakenne.collapsed" class="glyphicon glyphicon-collapse-down"></span>' +
        '<span ng-show="rakenne.collapsed" class="glyphicon glyphicon-collapse-up"></span>' +
        '</a> ';
    }

    function generoiOtsikko(rakenne) {
      var otsikko = '';

      if (rakenne.otsikko) {
        otsikko = rakenne.otsikko.fi;

        if (rakenne.saannot) {
          if (rakenne.saannot.maara && rakenne.saannot.yksikko) {
            otsikko += ', ' + rakenne.saannot.maara + rakenne.saannot.yksikko;
          }
        }
      } else if (rakenne.saannot) {
        switch (rakenne.saannot.tyyppi) {
          case 'laajuus':
            otsikko = 'Valitse seuraavista vähintään ' + rakenne.saannot.maara + rakenne.saannot.yksikko + ' edestä';
            break;
          case 'maara':
            otsikko = 'Valitse seuraavista ' + rakenne.saannot.maara;
            break;
          default:
            break;
        }
      }
      return '<span>' + otsikko + ' </span>';
    }

    function generoiOptiot(rakenne) {
      var url = '';
      if (rakenne._perusteenOsa) {
        url = $state.href('muokkaus.vanha', { perusteenOsanId: rakenne._perusteenOsa, perusteenOsanTyyppi: 'tutkinnonosa' });
      }
      return '<span ng-show="naytaTyokalut">' +
        '<a href="" ng-click="ylos(rakenne, vanhempi)"><span class="glyphicon glyphicon-chevron-up"></a>' +
        '<a href="" ng-click="alas(rakenne, vanhempi)"><span class="glyphicon glyphicon-chevron-down"></a>' +
        '<a ng-if="!rakenne._perusteenOsa" href="" ng-click="muokkaa(rakenne, vanhempi)"><span class="glyphicon glyphicon-pencil"></a>' +
        '<a ng-if="rakenne._perusteenOsa" ng-href="' + url + '"><span class="glyphicon glyphicon-pencil"></a>' +
        '<a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></a>' +
        '</span>';
    }

    function generoiUusiValitsin() {
      var ryhma =
        '<div ng-show="lisaaUusi === 2" class="panel panel-primary">' +
        '<div class="panel-heading"><span>Lisää uusi ryhmä</span></div>' +
        '  <div class="panel-body">' +
        '    <div class="form-group">' +
        '      <label class="col-sm-3">Nimi</label>' +
        '      <div class="col-sm-9">' +
        '        <input ng-model="uusi.nimi" type="text" class="form-control"></input>' +
        '      </div>' +
        '    </div>' +
        '    <div class="form-group">' +
        '      <label class="col-sm-3">Kuvaus</label>' +
        '      <div class="col-sm-9">' +
        '        <input ng-model="uusi.kuvaus" type="text" class="form-control"></input>' +
        '      </div>' +
        '    </div>' +
        '    <div class="form-group">' +
        '      <label class="col-sm-3">Pakollisuus</label>' +
        '      <div class="col-sm-9">' +
        '        <input ng-model="uusi.pakollinen" type="checkbox" class="form-control"></input>' +
        '      </div>' +
        '    </div>' +
        '    <div class="form-group">' +
        '      <div class="clearfix"></div>' +
        '      <label class="col-sm-3">Laajuus</label>' +
        '      <div class="col-sm-9">' +
        '        <input ng-model="uusi.laajuus" type="number" class="form-control"></input>' +
        '      </div>' +
        '    </div>' +
        '  </div>' +
        '  <div class="panel-footer">' +
        '    <button class="btn btn-primary btn-xs" ng-click="lisaaNode(uusi, lisaaUusi); lisaaUusi = 0">Lisää</button>' +
        '    <button class="btn btn-danger btn-xs" ng-click="lisaaUusi = 0">Peru</button>' +
        '  </div>' +
        '</div>';

      // var tutkinnonosa =
      //   '<div ng-show="lisaaUusi === 1" class="panel panel-default">' +
      //   '<div class="panel-heading"><span>Lisää uusi tutkinnon osa</span></div>' +
      //   '  <div class="panel-body">' +
      //   '    <div class="form-group">' +
      //   '      <label class="col-sm-3">Nimi</label>' +
      //   '      <div class="col-sm-9">' +
      //   '        <input ng-model="uusi.nimi" type="text" class="form-control"></input>' +
      //   '      </div>' +
      //   '    </div>' +
      //   '    <div class="form-group">' +
      //   '      <label class="col-sm-3">Kuvaus</label>' +
      //   '      <div class="col-sm-9">' +
      //   '        <input ng-model="uusi.kuvaus" type="text" class="form-control"></input>' +
      //   '      </div>' +
      //   '    </div>' +
      //   '    <div class="form-group">' +
      //   '      <label class="col-sm-3">Pakollisuus</label>' +
      //   '      <div class="col-sm-9">' +
      //   '        <input ng-model="uusi.pakollinen" type="checkbox" class="form-control"></input>' +
      //   '      </div>' +
      //   '    </div>' +
      //   '    <div class="form-group">' +
      //   '      <div class="clearfix"></div>' +
      //   '      <label class="col-sm-3">Laajuus</label>' +
      //   '      <div class="col-sm-9">' +
      //   '        <input ng-model="uusi.laajuus" type="number" class="form-control"></input>' +
      //   '      </div>' +
      //   '    </div>' +
      //   '  </div>' +
      //   '  <div class="panel-footer">' +
      //   '    <button class="btn btn-primary btn-xs" ng-click="lisaaNode(uusi); lisaaUusi = 0">Lisää</button>' +
      //   '    <button class="btn btn-danger btn-xs" ng-click="lisaaUusi = 0">Peru</button>' +
      //   '  </div>' +
      //   '</div>';

      return ryhma;
    }

    return {
      restrict: 'AE',
      transclude: false,
      terminal: true,
      scope: {
        rakenne: '=',
        uusiTutkinnonOsa: '=',
        vanhempi: '='
      },
      link: function(scope, el) {
        scope.lisaaUusi = 0;
        scope.lisataanUuttaPerusteenOsaa = false;

        function liitaUusiTutkinnonOsa(osa) {
          scope.rakenne.osat.push({
            otsikko: { fi: 'Uusi' },
            kuvaus: { fi: '' },
            _perusteenOsa: 1337,
            saannot: {
              maara: 10,
              yksikko: 'ov',
              tyyppi: 'laajuus',
              pakollinen: false
            }
          });
        }

        scope.tuoTutkinnonOsa = TutkinnonOsanTuonti.modaali(function(osa) {
          scope.rakenne.osat.push({
            otsikko: osa.nimi,
            kuvaus: { fi: '' },
            _perusteenOsa: osa.id,
            saannot: {
              maara: 10,
              yksikko: 'ov',
              tyyppi: 'laajuus',
              pakollinen: false
            }
          });
        });

        scope.poista = function(i, a) { _.remove(a.osat, i); };
        scope.alas = function(i, a) {
          if (!a) {
            return;
          }
          var index = a.osat.indexOf(i);
          swap(a.osat, index, index + 1);
        };
        scope.ylos = function(i, a) {
          if (!a) {
            return;
          }
          var index = a.osat.indexOf(i);
          swap(a.osat, index, index - 1);
        };

        scope.sortableOptions = {
          placeholder: 'group-placeholder',
          connectWith: '.tree-group',
          delay: 400,
          tolerance: 'pointer',
          cursorAt: { top : 2, left: 2 }
        };

        scope.liitaUusiTutkinnonOsa = liitaUusiTutkinnonOsa;

        scope.lisaaNode = function(node, tyyppi) {
          if (tyyppi === 0) {
            return;
          }

          var uusi = {};

          if (tyyppi === 1) {
            uusi.otsikko = { fi: node.nimi };
            uusi.kuvaus = { fi: node.kuvaus };
            uusi._perusteenOsa = 1;
            uusi.saannot = {
              maara: node.laajuus,
              yksikko: 'ov',
              tyyppi: 'laajuus',
              pakollinen: node.pakollinen
            };
          } else if (tyyppi === 2) {
            uusi.otsikko = { fi: node.nimi };
            uusi.kuvaus = { fi: node.kuvaus };
            uusi.saannot = {
              maara: node.laajuus,
              yksikko: 'ov',
              tyyppi: 'laajuus',
              pakollinen: node.pakollinen
            };
            uusi.osat = [];
          }
          scope.rakenne.osat.push(uusi);
        };

        var kentta = generoiPiilotusPainike();
        kentta += generoiOtsikko(scope.rakenne);
        kentta += generoiOptiot(scope.rakenne);
        kentta = '<div ng-mouseenter="naytaTyokalut = true" ng-mouseleave="naytaTyokalut = false">' + kentta + '</div>';

        var template = '';

        if (scope.rakenne && _.isArray(scope.rakenne.osat)) {
          template =
            '<div>' +
            '<div grab-cursor>' +
            kentta +
            '</div>' +
            '<div ng-show="!rakenne.collapsed">';

          if (scope.rakenne.kuvaus) {
            template += '<div>' + scope.rakenne.kuvaus.fi + '</div>';
          }

          template +=
            '<ul ng-if="true" ui-sortable="sortableOptions" class="tree-group" ng-model="rakenne.osat">' +
            '  <li class="item" ng-repeat="osa in rakenne.osat">' +
            '    <tree rakenne="osa" vanhempi="rakenne" uusi-tutkinnon-osa="uusiTutkinnonOsa"></tree>' +
            '  </li>' +
            '</ul>' +
            '<ul>' +
            '  <li class="dropdown"><a ng-hide="lisaaUusi" href="" class="dropdown-toggle"><span class="glyphicon glyphicon-plus"></span> Lisää</a>' +
            '  <ul class="dropdown-menu">' +
            '    <li><a href="" ng-click="uusiTutkinnonOsa(liitaUusiTutkinnonOsa)">Uusi tutkinnon osa</a></li>' +
            '    <li><a href="" ng-click="tuoTutkinnonOsa()">Hae tutkinnon osa</a></li>' +
            '    <li><a href="" ng-click="lisaaUusi = 2">Ryhmä</a></li>' +
            '  </ul>' +
            '  </li>' +
            '</ul>' +
            '' +
            generoiUusiValitsin() +
            '  </div>' +
            '</div>';
        } else {
          template = kentta;
        }

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  })
  .directive('treeWrapper', function(Editointikontrollit) {
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
      },
      link: function(scope) {
        scope.lisataanUuttaOsaa = false;
        scope.uusiOsa = null;
        scope.suljePolut = function() {
          scope.rakenne.collapsed = true;
          kaikilleRakenteille(scope.rakenne.osat, function(osa) {
            osa.collapsed = true;
          });
        };

        Editointikontrollit.registerAdditionalSaveCallback(function() {
          scope.lisataanUuttaOsaa = false;
        });

        scope.uusiTutkinnonOsa = function(cb) {
          scope.lisataanUuttaOsaa = true;
          cb();
        };
      }
    };
  });
