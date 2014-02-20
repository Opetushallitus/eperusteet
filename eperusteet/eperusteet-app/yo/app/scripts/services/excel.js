'use strict';
/* global _ */
/* global XLSX */

angular.module('eperusteApp')
  .service('ExcelService', function($q, $http, SERVICE_LOC) {
    // FIXME: Voisi olla backendissä

    var osatutkintoMap = {
      parsittavatKentat: {
        1: 'nimi',
        2: 'ammattitaitovaatimukset',
        3: 'opintoluokitus',
        4: 'osaamisala',
        5: 'osoittamistavat',
        6: 'kohdealueet',
        7: 'kohteet',
        8: 'arviointikriteerit',
        9: 'tyydyttava',
        10: 'hyvä',
        11: 'kiitettävä',
        12: 'erillispätevyys',
      },
      info: [1, 2, 3, 4],
      lokalisointi: [1, 2, 4],
      ammattitutkinto: {
        kentat: {
          1: 'V',
          2: 'Y',
          3: 'U',
          4: 'X',
          5: 'AF',
          6: 'AA',
          7: 'AB',
          8: 'AC',
          12: 'Z',
        },
        asetukset: {
          arviointiasteikko: 1
        },
        otsikot: {
          AA1: 'Ammattitaitovaatimus',
          AB1: 'Arvioinnin kohde',
          AC1: 'Arviointikriteerit',
          AF1: 'Ammattitaidon osoittamistavat',
          U1: 'Tutkinnon osan opintoluokituskoodi',
          V1: 'Tutkinnon osan nimi',
          X1: 'Osaamisala',
          Y1: 'Ammattitaitovaatimukset (tiivistelmä/kuvaus ammattitaitovaatimuksista)',
          Z1: 'Erillispätevyys',
        }
      },
      perustutkinto: {
        asetukset: {
          arviointiasteikko: 3
        },
        kentat: {
          1: 'AK',
          2: 'AU',
          3: 'AJ',
          4: 'AP',
          5: 'BA',
          6: 'AV',
          7: 'AW',
          9: 'AX',
          10: 'AY',
          11: 'AZ',
        },
        otsikot: {
          AJ1: 'Tutkinnon osan opintoluokituskoodi',
          AK1: 'Tutkinnon osan nimi',
          AR1: 'Tutkintonimike',
          AS1: 'Tutkintonimikekoodi',
          AU1: 'Ammattitaitovaatimus / tavoite',
          AV1: 'Arvioinnin kohdealue',
          AW1: 'Arvioinnin kohde',
          AX1: 'Tyydyttävä T1 ',
          AY1: 'Hyvä H2',
          AZ1: 'Kiitettävä K3',
          BA1: 'Ammattitaidon osoittamistavat',
        }
      }
    };

    function validoi() {
      if (_.size(arguments) < 2 || !_.all(_.rest(arguments), _.isFunction)) { return false; }

      var data = _.first(arguments);
      var validaattorit = _.rest(arguments);
      var virheet = [];

      function next(err) {
        if (err) { virheet.push(err); }
        if (_.isEmpty(validaattorit)) {
          return virheet;
        } else {
          var seuraavaValidaattori = _.first(validaattorit);
          validaattorit = _.rest(validaattorit);
          return seuraavaValidaattori(data, next);
        }
      }
      return next();
    }

    function rakennaVaroitus(cellnro, name, warning, severe) {
      severe = severe || false;
      return {
        cell: cellnro,
        name: name,
        warning: warning,
        severe: severe
      };
    }

    function rakennaVirhe(cellnro, expected, actual) {
      return {
        cell: cellnro,
        expected: expected,
        actual: actual
      };
    }

    function puhdistaString(str) {
      return str.trim().toLowerCase();
    }

    // Checks if headers look the same
    function validoiOtsikot(data, tyyppi) {
      return function(data, next) {
        _.forEach(osatutkintoMap[tyyppi].otsikot, function(value, key) {
          var expected = value;
          var actual = data[key] ? data[key].v : '';
          if (puhdistaString(expected) !== puhdistaString(actual)) {
            return next(rakennaVirhe(key, expected, actual));
          }
        });
        return next();
      };
    }

    function validoiRivit(data, next) {
      return next();
    }

    function sheetHeight(sheet) {
      return sheet['!ref'].replace(/^.+:/, '').replace(/[^0-9]/g, '');
    }

    function getOsaAnchors(data) {
      var height = sheetHeight(data);
      var anchors = [];
      for (var i = 2; i < height; i++) {
        var celldata = data['T' + i];
        if (celldata && celldata.v) {
          anchors.push(i);
        }
      }
      return anchors;
    }

    function suodataTekstipala(teksti) {
      if (!teksti) {
        return '';
      }
      var suodatettu = teksti;
      suodatettu = suodatettu.replace(/&.{0,5};/g, ' ');
      suodatettu = suodatettu.replace('•', '');
      return suodatettu;
    }

    function fify(obj, ids) {
      _.forEach(ids, function(id) {
        var field = [osatutkintoMap.parsittavatKentat[id]];
        var value = obj[field];
        obj[field] = {};
        obj[field].fi = suodataTekstipala(value);
      });
      return obj;
    }

    function readOsaperusteet(data, tyyppi) {
      var height = sheetHeight(data);
      var anchors = getOsaAnchors(data);
      var osaperusteet = [];
      var varoitukset = [];
      var virheet = [];
      var kentat = osatutkintoMap[tyyppi].kentat;
      var arviointiasteikko = osatutkintoMap[tyyppi].asetukset.arviointiasteikko;

      _.each(anchors, function(anchor, index) {
        var osaperuste = {};
        _.forEach(_.pick(osatutkintoMap.parsittavatKentat, osatutkintoMap.info), function(value, key) {
          var solu = kentat[key] + anchor;
          var arvo = parseInt(data[solu].v);
          osaperuste[value] = _.isNaN(arvo) ? data[solu].v : arvo;

          var warning = false;
          if (!value && key === 'nimi') {
            warning = rakennaVaroitus(solu, '-', 'Osaperusteen nimeä ei ole määritetty.', true);
          } else if (!value && key === 'osaamisala') {
            warning = rakennaVaroitus(solu, osaperuste.nimi, 'Osaamisalaa ei ole määritetty.');
          } else if (!value && key === 'ammattitaitovaatimukset') {
            warning = rakennaVaroitus(solu, osaperuste.nimi, 'Ammattitaitovaatimuksien kuvausta ei ole määritetty.');
          } else if (!value && key === 'opintoluokitus') {
            warning = rakennaVaroitus(solu, osaperuste.nimi, 'Opintoluokitusta ei ole määritetty.');
          }
          if (warning !== false) {
            varoitukset.push(warning);
          }
        });

        osaperuste = fify(osaperuste, osatutkintoMap.lokalisointi);

        osaperuste.ammattitaidonOsoittamistavat = {};
        osaperuste.ammattitaidonOsoittamistavat.fi = '';
        osaperuste.arviointi = {};
        osaperuste.arviointi.lisatiedot = {};
        osaperuste.arviointi.lisatiedot.fi = '';
        osaperuste.arviointi.arvioinninKohdealueet = [];

        var nextAnchor = index < anchors.length - 1 ? anchors[index + 1] : height;
        var arvioinninKohdealue = {};

        _.each(_.range(anchor, nextAnchor), function(j) {
          // Osoittamistapojen kerääminen
          var cell = data[kentat[5] + j];
          if (cell && cell.v) {
            osaperuste.ammattitaidonOsoittamistavat.fi += '<p>' + suodataTekstipala(cell.v) + '</p>';
          }

          // ArvioinninKohdealueiden lisääminen
          cell = data[kentat[6] + j];
          if (cell && cell.v) {
            if (!_.isEmpty(arvioinninKohdealue)) {
              osaperuste.arviointi.arvioinninKohdealueet.push(_.clone(arvioinninKohdealue));
            }
            arvioinninKohdealue = {};
            arvioinninKohdealue.arvioinninKohteet = [];
            arvioinninKohdealue.otsikko = {
              fi: suodataTekstipala(cell.v)
            };
          }

          // Uuden ammattitaitovaatimuksen lisääminen
          var kohde = data[kentat[7] + j];

          // Uuden kohteen lisäys ammattitaitovaatimukseen
          if (kohde && kohde.v) {
            if (!_.isEmpty(arvioinninKohdealue)) {
              var viimeinenKohde = _.last(arvioinninKohdealue.arvioinninKohteet);
              if (viimeinenKohde && viimeinenKohde.arviointiAsteikko === 3) {
                var tyydyttavat = [];
                var hyvat = [];
                var kiitettavat = [];
                _.forEach(viimeinenKohde.osaamistasonKriteerit, function(obj) {
                  var key = parseInt(_.keys(obj)[0]);
                  var value = _.values(obj)[0];
                  if (key === 1) { tyydyttavat.push(value); }
                  else if (key === 2) { hyvat.push(value); }
                  else if (key === 3) { kiitettavat.push(value); }
                });
                viimeinenKohde.osaamistasonKriteerit = [{
                  osaamistaso: 1,
                  kriteerit: tyydyttavat
                }, {
                  osaamistaso: 2,
                  kriteerit: hyvat
                }, {
                  osaamistaso: 3,
                  kriteerit: kiitettavat
                }];
              }
              arvioinninKohdealue.arvioinninKohteet.push({
                  otsikko: {
                    fi: suodataTekstipala(kohde.v)
                  },
                  arviointiAsteikko: arviointiasteikko,
                  osaamistasonKriteerit: []
              });
            } else {
              varoitukset.push(rakennaVaroitus(kentat[8] + j, '', 'Arvioinnin kohdealuetta ei löytynyt'));
            }
          }

          function filtteroituKentta(id) {
            var cell = data[kentat[id] + j];
            return cell ? suodataTekstipala(cell.v) : '';
          }

          // Kriteereiden parsiminen kohteille
          if (!_.isEmpty(arvioinninKohdealue.arvioinninKohteet)) {
            var okt = _.last(arvioinninKohdealue.arvioinninKohteet).osaamistasonKriteerit;
            if (arviointiasteikko === 1) {
              var kriteeri = data[kentat[8] + j];

              // Uuden kriteerin lisääminen kohteeseen
              if (kriteeri && kriteeri.v) {
                if (_.isEmpty(okt)) {
                  okt.push({
                      osaamistaso: 1,
                      kriteerit: []
                  });
                }
                _.last(okt).kriteerit.push({
                    fi: suodataTekstipala(kriteeri.v)
                });
              }
            } else {
              okt.push({ 1: { fi: filtteroituKentta(9) } });
              okt.push({ 2: { fi: filtteroituKentta(10) } });
              okt.push({ 3: { fi: filtteroituKentta(11) } });
            }
          } else {
            varoitukset.push(rakennaVaroitus(kentat[8] + j, osaperuste.nimi, 'Arvioinnin kohdetta ei löytynyt'));
          }
        });

        osaperusteet.push(_.clone(osaperuste));
      });

      return {
        osaperusteet: osaperusteet,
        varoitukset: varoitukset,
        virheet: virheet
      };
    }

    // Konvertoi parsitun XLSX-tiedoston perusteen osiksi.
    // $q:n notifyä käytetään valmistumisen päivittämiseen.
    // Palauttaa lupauksen.
    function toJson(parsedxlsx, tyyppi) {
      if (tyyppi !== 'perustutkinto') {
        tyyppi = 'ammattitutkinto';
      }

      var deferred = $q.defer();
      if (_.isEmpty(parsedxlsx.SheetNames)) {
        deferred.reject(1);
      } else {
        var name = parsedxlsx.SheetNames[0];
        var sheet = parsedxlsx.Sheets[name];
        var err = validoi(sheet, validoiOtsikot(sheet, tyyppi), validoiRivit);

        if (err.length > 0) {
          deferred.reject(err);
        } else {
          deferred.resolve(readOsaperusteet(sheet, tyyppi));
        }
      }
      return deferred.promise;
    }

    function parseXLSXToOsaperuste(file, tyyppi) {
      return toJson(XLSX.read(file, { type: 'binary' }), tyyppi);
    }

    function saveOsaperuste(osaperuste) {
      return $http.post(SERVICE_LOC + '/perusteenosat',
        _.clone(osaperuste),
        { params: { tyyppi: 'perusteen-osat-tutkinnon-osa' } });
    }

    return {
      parseXLSXToOsaperuste: parseXLSXToOsaperuste,
      saveOsaperuste: saveOsaperuste
    };
  });
