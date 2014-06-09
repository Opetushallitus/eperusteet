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
/* global XLSX */

angular.module('eperusteApp')
  .service('ExcelService', function($q) {

    // Tutkintojen/perusteiden parsiminen
    var tutkintoMap = {
      // Lopullisen backendille lähetettävän perusteen rakenne
      parsittavatKentat: {
        1: 'nimi',
        2: 'tutkintokoodi',
        // 4: 'koulutusala',
        // 5: 'opintoalat',
        // 6: 'paivays',
      },
      // Perustutkintoon liittyvät tiedot
      perustutkinto: {
        // Parsittavan kentän ja solun suhde
        kentat: {
          1: 'A',
          2: 'B',
        },
        tekstikentat: [
          'D',
          'E',
          'F',
          'G',
          'H',
          'I',
          'J',
          'K',
          'L',
          'M',
          'N',
          'O',
          'P',
          'Q',
          'R',
          'S',
          'T',
          'U',
          'V',
          'W',
          'X',
          'Y',
          'Z',
          'AA',
          'AB',
          'AC',
          'AD',
          'AE',
          'AF',
          'AG',
          'AH',
        ]
      },
      ammattitutkinto: {
        kentat: {
          1: 'A',
          2: 'B',
        },
        // Tekstikappaleet/perusteen osat
        tekstikentat: [
          'D',
          'E',
          'F',
          'G',
          'H',
          'I',
          'J',
          'K',
          'L',
          'M',
          'N',
          'O',
          'P',
          'Q',
          'R',
          'S',
        ]
      }
    };
    // Kentät jotka pitää lokalisoida (laittaa oliossa fi:n alle)
    tutkintoMap.lokalisointi = [1];

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
      virheet: {
        1: 'Osaperusteen nimeä ei ole määritetty'
      },
      varoitukset: {
        2: 'Ammattitaitovaatimuksien kuvausta ei ole määritetty.',
        3: 'Opintoluokitusta ei ole määritetty.',
        4: 'Osaamisalaa ei ole määritetty.'
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
          arviointiasteikko: '1'
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
          arviointiasteikko: '2'
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

    function getOsaAnchors(data, tyyppi) {
      var height = sheetHeight(data);
      var anchors = [];
      for (var i = 2; i < height; i++) {
        var celldata = data[osatutkintoMap[tyyppi].kentat[1] + i];
        if (celldata && celldata.v) {
          anchors.push(i);
        }
      }
      return anchors;
    }

    function suodataTekstipala(teksti) {
      if (!teksti) {
        return '';
      } else if (!_.isString(teksti)) {
        return teksti;
      }

      var suodatettu = teksti;
      suodatettu = suodatettu.replace(/&.{0,5};/g, ' ');
      for (var i = 0; i < suodatettu.length; ++i) {
        var c = suodatettu[i];
        var ci = c.charCodeAt(0);
        if (ci < 32 && ci > 255) {
          suodatettu[i] = ' ';
        }
      }
      return suodatettu;
    }

    function fify(obj, ids, kentat) {
      var newobj = _.clone(obj);
      _.forEach(ids, function(id) {
        var field = [kentat[id]];
        var value = obj[field];
        newobj[field] = {};
        newobj[field].fi = suodataTekstipala(value);
      });
      return newobj;
    }

    function readPerusteet(data, tyyppi) {
      var height = sheetHeight(data);
      var kentat = tutkintoMap[tyyppi].kentat;
      var peruste = {};

      _.forEach(tutkintoMap.parsittavatKentat, function(value, key) {
        peruste[value] = [];
        for (var i = 2; i < height; ++i) {
          var solu = kentat[key] + i;
          var arvo = data[solu];
          if (arvo && arvo.v) {
            var suodatettu = suodataTekstipala(arvo.v);
            peruste[value] = suodatettu;
          }
        }
      });

      peruste = fify(peruste, tutkintoMap.lokalisointi, tutkintoMap.parsittavatKentat);

      peruste.tekstikentat = [];
      _.forEach(tutkintoMap[tyyppi].tekstikentat, function(col) {
        var otsikko = data[col + 1];

        if (otsikko && otsikko.v) {
          var tekstikentta = {
            nimi: {
              fi: suodataTekstipala(otsikko.v)
            },
            teksti: {
              fi: ''
            }
          };
          for (var i = 2; i < height; ++i) {
            var solu = data[col + i];
            if (solu && solu.v) {
              tekstikentta.teksti.fi += '<p>' + suodataTekstipala(solu.v) + '</p>';
            }
          }
          peruste.tekstikentat.push(tekstikentta);
        }
      });
      return peruste;
    }

    function readOsaperusteet(data, tyyppi) {
      var height = sheetHeight(data);
      var anchors = getOsaAnchors(data, tyyppi);
      var osaperusteet = [];
      var varoitukset = [];
      var virheet = [];
      var kentat = osatutkintoMap[tyyppi].kentat;
      var arviointiasteikko = osatutkintoMap[tyyppi].asetukset.arviointiasteikko;
      var tyydyttavat = [];
      var hyvat = [];
      var kiitettavat = [];

      _.each(anchors, function(anchor, index) {
        var osaperuste = {};
        _.forEach(_.pick(osatutkintoMap.parsittavatKentat, osatutkintoMap.info), function(value, key) {
          var solu = kentat[key] + anchor;
          var arvo = '';
          if (data[solu]) {
            var numero = parseInt(data[solu].v, 10);
            arvo = _.isNaN(numero) ? data[solu].v : numero;
          }
          osaperuste[value] = arvo;

          var virhe = osatutkintoMap.virheet[key];
          var varoitus = osatutkintoMap.varoitukset[key];
          if (!arvo || !value) {
            var nimi = suodataTekstipala(osaperuste.nimi);
            if (virhe) {
              virheet.push(rakennaVaroitus(solu, nimi, virhe, true));
            } else if (varoitus) {
              varoitukset.push(rakennaVaroitus(solu, nimi, varoitus));
            }
          }
        });

        osaperuste = fify(osaperuste, osatutkintoMap.lokalisointi, osatutkintoMap.parsittavatKentat);

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

          // Uuden kohdealueen lisäys ammattitaitovaatimukseen
          if (kohde && kohde.v) {
            if (!_.isEmpty(arvioinninKohdealue)) {
              var viimeinenKohde = _.last(arvioinninKohdealue.arvioinninKohteet);
              if (viimeinenKohde && viimeinenKohde._arviointiAsteikko === '2') {
                viimeinenKohde.osaamistasonKriteerit = [{
                    _osaamistaso: '2',
                    kriteerit: tyydyttavat
                }, {
                  _osaamistaso: '3',
                  kriteerit: hyvat
                }, {
                  _osaamistaso: '4',
                  kriteerit: kiitettavat
                }];
                tyydyttavat = [];
                hyvat = [];
                kiitettavat = [];
              }
              arvioinninKohdealue.arvioinninKohteet.push({
                  otsikko: {
                    fi: suodataTekstipala(kohde.v)
                  },
                  _arviointiAsteikko: arviointiasteikko,
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
            if (arviointiasteikko === '1') {
              var kriteeri = data[kentat[8] + j];

              // Uuden kriteerin lisääminen kohteeseen
              if (kriteeri && kriteeri.v) {
                if (_.isEmpty(okt)) {
                  okt.push({
                      _osaamistaso: '1',
                      kriteerit: []
                  });
                }
                _.last(okt).kriteerit.push({
                    fi: suodataTekstipala(kriteeri.v)
                });
              }
            } else {
              tyydyttavat.push({ fi: filtteroituKentta(9) });
              hyvat.push({ fi: filtteroituKentta(10) });
              kiitettavat.push({ fi: filtteroituKentta(11) });
            }
          } else {
            if (arviointiasteikko === '1') {
              varoitukset.push(rakennaVaroitus(kentat[8] + j, osaperuste.nimi, 'Arvioinnin kohdetta ei löytynyt'));
            } else if (arviointiasteikko === '2') {
              varoitukset.push(rakennaVaroitus(kentat[9] + j + ', ' + kentat[10] + j + ', ' + kentat[11] + j, osaperuste.nimi, 'Arvioinnin kohdetta ei löytynyt'));
            }
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
          deferred.resolve({
            peruste: readPerusteet(sheet, tyyppi),
            osatutkinnot: readOsaperusteet(sheet, tyyppi)
          });
        }
      }
      return deferred.promise;
    }

    function parseXLSXToOsaperuste(file, tyyppi) {
      return toJson(XLSX.read(file, { type: 'binary' }), tyyppi);
    }

    return {
      parseXLSXToOsaperuste: parseXLSXToOsaperuste
    };
  });
