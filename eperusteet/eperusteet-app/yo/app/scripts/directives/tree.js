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
          console.log('tallennettu id:', puuId);
        }
      },
      puhdista: function() {
        puuId = -1;
        puu = {};
      }
    };
  })
  .directive('tree', function($compile, TutkinnonOsanTuonti) {
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

    function generoiOptiot() {
      return '<span ng-show="naytaTyokalut">' +
        '<a href="" ng-click="ylos(rakenne, vanhempi)"><span class="glyphicon glyphicon-chevron-up"></a>' +
        '<a href="" ng-click="alas(rakenne, vanhempi)"><span class="glyphicon glyphicon-chevron-down"></a>' +
        '<a href="" ng-click="muokkaa(rakenne, vanhempi)"><span class="glyphicon glyphicon-pencil"></a>' +
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

    function kaikilleRakenteille(osat, f) {
      if (!osat || !f) {
        return;
      }
      _.forEach(osat, function(r) {
        r = f(r);
        if (r.osat) {
          kaikilleRakenteille(r.osat);
        }
      });
    }

    return {
      restrict: 'AE',
      transclude: false,
      terminal: true,
      scope: {
        rakenne: '=',
        vanhempi: '='
      },
      link: function(scope, el, attrs) {
        scope.lisaaUusi = 0;

        scope.tuoTutkinnonOsa = TutkinnonOsanTuonti.modaali(function(osa) {
          console.log(osa);
          var uusi = {};
          uusi.otsikko = osa.nimi;
          uusi.kuvaus = { fi: '' };
          uusi._perusteenOsa = osa.id;
          uusi.saannot = {
            maara: 10,
            yksikko: 'ov',
            tyyppi: 'laajuus',
            pakollinen: false
          };
          scope.rakenne.osat.push(uusi);
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
        kentta += generoiOptiot();
        kentta = '<div ng-mouseenter="naytaTyokalut = true" ng-mouseleave="naytaTyokalut = false">' + kentta + '</div>';

        var template = '';
        if (scope.rakenne && _.isArray(scope.rakenne.osat)) {
          template =
            '<div class="panel panel-default">' +
            '  <div class="panel-heading">' +
            kentta +
            '  </div>' +
            '  <div ng-show="!rakenne.collapsed" class="panel-body">';

          if (scope.rakenne.kuvaus) {
            template += '<div>' + scope.rakenne.kuvaus.fi + '</div><br>';
          }

          template +=
            '    <div ng-repeat="osa in rakenne.osat"><tree rakenne="osa" vanhempi="rakenne"></tree></div>' +
            '<div class="dropdown">' +
            '  <a ng-hide="lisaaUusi" href="" class="dropdown-toggle">Lisää...</a>' +
            '  <ul class="dropdown-menu">' +
            '    <li><a href="" ng-click="lisaaUusi = 1">Uusi tutkinnon osa</a></li>' +
            '    <li><a href="" ng-click="tuoTutkinnonOsa()">Hae tutkinnon osa</a></li>' +
            '    <li><a href="" ng-click="lisaaUusi = 2">Ryhmä</a></li>' +
            '  </ul>' +
            '</div>' +
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
  });
