'use strict';
/* global _ */
/* global XLSX */

function tableHeaderMap() {
  var theaders = {};
  var count = 0;
  _.each(_.range(26), function(i) {
    theaders[count++] = String.fromCharCode(65 + i);
  });
  _.each(_.range(26), function(i) {
    _.each(_.range(26), function(j) {
      theaders[count++] = String.fromCharCode(65 + i) + String.fromCharCode(65 + j);
    });
  });
  return theaders;
}

angular.module('eperusteApp')
  .service('ExcelService', function($q, $http, SERVICE_LOC) {
    // FIXME: Voisi olla backendissä
    var osaperusteHeaders = {
      A1: 'Tutkinnon nimi',
      B1: 'Tutkintokoodi',
      C1: 'Diaarinumero',
      D1: 'Näyttötutkintojen järjestäminen',
      E1: 'Näyttötutkinnon suorittaminen',
      F1: 'Näyttötutkinnon perusteet',
      G1: 'Henkilökohtaistaminen näyttötutkinnossa',
      H1: 'Ammattitaidon arviointi näyttötutkinnossa',
      I1: 'Arvioijat',
      J1: 'Arvioinnin oikaisu',
      K1: 'Todistukset',
      L1: 'Näyttötutkintoon valmistava koulutus',
      M1: 'Tutkintokohtaiset terveydentilavaatimukset ammatti- ja erikoisammattitutkinnoissa',
      N1: 'Yleistä tutkinnon muodostumisesta',
      O1: 'Tutkinnossa osoitettava osaaminen/tutkinnon suorittaneen osaaminen',
      P1: 'Laissa säädetyt ammattipätevyydet',
      Q1: 'Ammattitutkinnon tiivis kuvaus',
      R1: 'Huomioitavaa tutkinnon suorittamisesta',
      S1: 'Tutkinnon osa muusta perus-, ammatti- tai erikoisammattitutkinnosta',
      T1: 'Tutkinnon osan luvun numero perusteasiakirjassa',
      U1: 'Tutkinnon osan opintoluokituskoodi',
      V1: 'Tutkinnon osan nimi',
      W1: 'Pakollinen / valinnainen',
      X1: 'Osaamisala',
      Y1: 'Ammattitaitovaatimukset (tiivistelmä/kuvaus ammattitaitovaatimuksista)',
      Z1: 'Erillispätevyys',
      AA1: 'Ammattitaitovaatimus',
      AB1: 'Arvioinnin kohde',
      AC1: 'Arviointikriteerit',
      AD1: 'Hyväksytyn suorituksen määritys',
      AE1: 'Hylätyn suorituksen määritys',
      AF1: 'Ammattitaidon osoittamistavat'
    };

    // Takes data to validate as first argument and n-amount of validatorfunctions
    // after that. The validatorfunctions need to take data and next -functions as
    // arguments and are required to return next() with optional error as an argument.
    function validate() {
      if (_.size(arguments) < 2 || !_.all(_.rest(arguments), _.isFunction)) { return false; }

      var data = _.first(arguments);
      var validators = _.rest(arguments);
      var errors = [];

      function next(err) {
        if (err) { errors.push(err); }

        if (_.isEmpty(validators)) {
          return errors;
        } else {
          var nextValidator = _.first(validators);
          validators = _.rest(validators);
          return nextValidator(data, next);
        }
      }
      return next();
    }

    function constructWarning(cellnro, name, warning, severe) {
      severe = severe || false;
      return {
        cell: cellnro,
        name: name,
        warning: warning,
        severe: severe
      };
    }

    function constructError(cellnro, expected, actual) {
      return {
        cell: cellnro,
        expected: expected,
        actual: actual
      };
    }

    function cleanString(str) {
      return str.trim().toLowerCase();
    }

    // Checks if headers look the same
    function validateHeaders(headers) {
      return function(data, next) {
        _.each(_.range(32), function(i) {
          var cellnro = headers[i] + 1;
          var expected = osaperusteHeaders[headers[i] + 1];
          var actual = data[cellnro].v;
          if (cleanString(expected) !== cleanString(actual)) {
            return next(constructError(cellnro, expected, actual));
          }
        });
        return next();
      };
    }

    function validateRows(data, next) {
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

    function uusiOsaperuste(nimi, opintoluokitus, osaamisala, ammattitaitovaatimukset) {
      var osaperuste = {};

      osaperuste.nimi = {};
      osaperuste.osaamisala = {};
      osaperuste.ammattitaitovaatimukset = {};
      osaperuste.opintoluokitus = opintoluokitus;

      osaperuste.nimi.fi = suodataTekstipala(nimi);
      osaperuste.osaamisala.fi = suodataTekstipala(osaamisala);
      osaperuste.ammattitaitovaatimukset.fi = suodataTekstipala(ammattitaitovaatimukset);

      return osaperuste;
    }

    function readOsaperusteet(data) {
      var height = sheetHeight(data);
      var anchors = getOsaAnchors(data);
      var osaperusteet = [];
      var varoitukset = [];
      var virheet = [];

      _.each(anchors, function(anchor, index) {
        var osaperuste = uusiOsaperuste(data['V' + anchor].v, data['U' + anchor].v, data['X' + anchor].v, data['Y' + anchor].v);

        // FIXME: lokalisoi
        _.forEach(osaperuste, function(value, key) {
          var warning = false;
          if (!value.fi && key === 'nimi') { warning = constructWarning('V' + anchor, '-', 'Osaperusteen nimeä ei ole määritetty.', true); }
          else if (!value.fi && key === 'osaamisala') { warning = constructWarning('X' + anchor, osaperuste.nimi, 'Osaamisalaa ei ole määritetty.'); }
          else if (!value.fi && key === 'ammattitaitovaatimukset') { warning = constructWarning('Y' + anchor, osaperuste.nimi, 'Ammattitaitovaatimuksien kuvausta ei ole määritetty.'); }
          else if (!value && key === 'opintoluokitus') { warning = constructWarning('U' + anchor, osaperuste.nimi, 'Opintoluokitusta ei ole määritetty.'); }
          if (warning !== false) {
            varoitukset.push(warning);
          }
        });

        osaperuste.ammattitaidonOsoittamistavat = '';
        osaperuste.arviointi = {};
        osaperuste.arviointi.lisatiedot = '';
        osaperuste.arviointi.arvioinninKohdealueet = [];

        var nextAnchor = index < anchors.length - 1 ? anchors[index + 1] : height;
        var arvioinninKohdealue = {};

        _.each(_.range(anchor, nextAnchor), function(j) {
          // Osaperusteiden kerääminen
          var cell = data['AF' + j];
          if (cell && cell.v) {
            osaperuste.ammattitaidonOsoittamistavat += '<p>' + suodataTekstipala(cell.v) + '</p>';
          }

          // ArvioinninKohdealueiden lisääminen
          cell = data['AA' + j];
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
          var kohde = data['AB' + j];
          var kriteeri = data['AC' + j];

          // Uuden kohteen lisäys ammattitaitovaatimukseen
          if (kohde && kohde.v) {
            if (!_.isEmpty(arvioinninKohdealue)) {
              arvioinninKohdealue.arvioinninKohteet.push({
                  otsikko: {
                    fi: suodataTekstipala(kohde.v)
                  },
                  arviointiAsteikko: 'arviointiasteikko_1',
                  osaamistasonKriteerit: []
              });
            } else {
              varoitukset.push(constructWarning('AC' + j, '', 'Arvioinnin kohdealuetta ei löytynyt'));
            }
          }

          // Uuden kriteerin lisääminen kohteeseen
          if (kriteeri && kriteeri.v) {
            if (!_.isEmpty(arvioinninKohdealue.arvioinninKohteet)) {
              var okt = _.last(arvioinninKohdealue.arvioinninKohteet).osaamistasonKriteerit;
              okt.push({
                  osaamistaso: 'osaamistaso_1',
                  kriteerit: []
              });
              _.last(okt).kriteerit.push({
                  fi: suodataTekstipala(kriteeri.v)
              });
            } else {
              console.log('virhe');
              varoitukset.push(constructWarning('AC' + j, osaperuste.nimi, 'Arvioinnin kohdetta ei löytynyt'));
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
    function toJson(parsedxlsx) {
      var deferred = $q.defer();
      if (_.isEmpty(parsedxlsx.SheetNames)) {
        deferred.reject(1);
      } else {
        var name = parsedxlsx.SheetNames[0];
        var sheet = parsedxlsx.Sheets[name];

        var err = validate(sheet, validateHeaders(tableHeaderMap()), validateRows);
        if (err.length > 0) {
          deferred.reject(err);
        } else {
          deferred.resolve(readOsaperusteet(sheet));
        }
      }
      return deferred.promise;
    }

    function parseXLSXToOsaperuste(file) {
      return toJson(XLSX.read(file, { type: 'binary' }));
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
