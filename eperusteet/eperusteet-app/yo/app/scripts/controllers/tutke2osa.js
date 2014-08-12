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
  .config(function($stateProvider) {
    $stateProvider
      .state('root.tutke2proto', {
        url: '/tutke2proto',
        templateUrl: 'views/tutke2osa.html',
        controller: 'Tutke2OsaController'
      });
  })
  .controller('Tutke2OsaController', function($scope, Tutke2ProtoTiedot) {
    $scope.activeosaamistavoite = null;

    $scope.hasChoices = function (osaAlue) {
      return _.isObject(osaAlue.vaihtoehdot) && _.keys(osaAlue.vaihtoehdot).length > 1;
    };

    $scope.show = function (osaAlue, model, key) {
      osaAlue.$shown = true;
      _.each(osaAlue.vaihtoehdot[osaAlue.$vaihtoehto || 0].osaamistavoitteet, function (item) {
        var osaamistavoiteShown = _.isUndefined(model) ? true : item === model;
        item.$shown = osaamistavoiteShown;
        _.each(item, function (value, innerkey) {
            item[innerkey].$shown = osaamistavoiteShown && _.isUndefined(key);
        });
      });
      if (!_.isUndefined(key)) {
        model[key].$shown = true;
      }
    };

    $scope.close = function (osaAlue) {
      osaAlue.$shown = false;
      _.each(osaAlue.vaihtoehdot, function (value) {
        _.each(value.osaamistavoitteet, function (item) {
          item.$shown = false;
          _.each(item, function (value, innerkey) {
              delete item[innerkey].$shown;
          });
        });
      });
    };

    $scope.model = {
      tutkinnonosa: Tutke2ProtoTiedot.OSA2
    };

    $scope.changeOsa = function () {
      if ($scope.model.tutkinnonosa.nimi.fi === 'Matemaattis-luonnontieteellinen osaaminen') {
        $scope.model.tutkinnonosa = Tutke2ProtoTiedot.OSA2;
      } else {
        $scope.model.tutkinnonosa = Tutke2ProtoTiedot.OSA1;
      }
    };
  })

  .service('Tutke2ProtoTiedot', function () {
    this.OSA1 = {
      nimi: {fi: 'Matemaattis-luonnontieteellinen osaaminen'},
      laajuus: {
        0: {
          pakollinen: 6,
          valinnainen: 3,
          yhteensa: 9
        }
      },
      osaAlueet: [
        {
          nimi: {fi: 'Matematiikka'},
          vaihtoehdot: {
            0: {
              osaamistavoitteet: [
            {
              pakollisuus: 'pakollinen',
              laajuus: 3,
              tavoitteet: {fi: 'Matematiikka pakollinen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Matematiikka pakollinen, tunnustaminen teksti'},
              arviointi: {}
            },
            {
              pakollisuus: 'valinnainen',
              laajuus: 3,
              tavoitteet: {fi: 'Matematiikka valinnainen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Matematiikka valinnainen, tunnustaminen teksti'},
              arviointi: {}
            }
          ]
            }
          }
        },
        {
          nimi: {fi: 'Fysiikka ja kemia'},
          vaihtoehdot: {
            0: {
              osaamistavoitteet: [
            {
              pakollisuus: 'pakollinen',
              laajuus: 2,
              tavoitteet: {fi: 'Opiskelija * osaa soveltaa oman alan kannalta keskeisiä fysiikan käsitteitä, ilmiöitä ja lainalaisuuksia * osaa ottaa työssään huomioon oman alan kannalta keskeisiä kemian ilmiöitä ja aineiden erityisominaisuuksia.'},
              tunnustaminen: {fi: 'Osaamisen tunnustamisessa lukion kurssit Fysiikka luonnontieteenä (FY1) ja Ihmisen ja elinympäristön kemia (KE1) vastaavat tavoitteiltaan Fysiikka ja kemia –osa-alueen pakollisia osaamistavoitteita.'},
              arviointi: {}
            },
            {
              pakollisuus: 'valinnainen',
              laajuus: 3,
              tavoitteet: {fi: 'Opiskelija * osaa soveltaa oman alan kannalta keskeisiä mekaniikan, lämpöopin ja sähköopin peruskäsitteitä ja ilmiöitä * osaa valmistaa alalla tarvittavia aineseoksia * osaa säilyttää, käyttää ja hävittää omalla alalla tarvittavia aineita * osaa tehdä havaintoja ja mittauksia oman alansa kannalta keskeisistä fysikaalisista ja kemiallisista ilmiöistä * osaa kerätä, käsitellä ja analysoida tekemiään havaintoja ja mittauksia * osaa arvioida saamiensa mittaustulosten luotettavuutta, tarkkuutta ja mielekkyyttä.'},
              tunnustaminen: {fi: 'Osaamisen tunnustamisessa lukion kurssit Fysiikka luonnontieteenä (FY1) ja Ihmisen ja elinympäristön kemia (KE1) vastaavat tavoitteiltaan Fysiikka ja kemia –osa-alueen valinnaisia osaamistavoitteita.'},
              arviointi: {}
            }
          ]
            }
          }
        },
        {
          nimi: {fi: 'Tieto- ja viestintätekniikka sekä sen hyödyntäminen'},
          vaihtoehdot: {
            0: {
              osaamistavoitteet: [
            {
              pakollisuus: 'pakollinen',
              laajuus: 1,
              tavoitteet: {fi: 'Tieto- ja viestintätekniikka sekä sen hyödyntäminen pakollinen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Tieto- ja viestintätekniikka sekä sen hyödyntäminen pakollinen, tunnustaminen teksti'},
              arviointi: {}
            },
            {
              pakollisuus: 'valinnainen',
              laajuus: 3,
              tavoitteet: {fi: 'Tieto- ja viestintätekniikka sekä sen hyödyntäminen valinnainen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Tieto- ja viestintätekniikka sekä sen hyödyntäminen valinnainen, tunnustaminen teksti'},
              arviointi: {}
            }
          ]
            }
          }
        },
      ]
    };
    this.OSA2 = {
      nimi: {fi: 'Viestintä- ja vuorovaikutusosaaminen'},
      laajuus: {
        0: {
          pakollinen: 8,
          valinnainen: 3,
          yhteensa: 11
        },
        1: {
          pakollinen: 9,
          valinnainen: 2,
          yhteensa: 11
        }
      },
      osaAlueet: [
        {
          nimi: {fi: 'Äidinkieli'},
          $vaihtoehto: 0,
          vaihtoehdot: {
            0: {
              nimi: {'fi': 'suomi'},
              osaamistavoitteet: [
                {
                  pakollisuus: 'pakollinen',
                  laajuus: 5,
                  tavoitteet: {fi: 'Äidinkieli suomi pakollinen, tavoitteet teksti'},
                  tunnustaminen: {fi: 'Äidinkieli suomi pakollinen, tunnustaminen teksti'},
                  arviointi: {}
                },
                {
                  pakollisuus: 'valinnainen',
                  laajuus: 3,
                  tavoitteet: {fi: 'Äidinkieli suomi valinnainen, tavoitteet teksti'},
                  tunnustaminen: {fi: 'Äidinkieli suomi valinnainen, tunnustaminen teksti'},
                  arviointi: {}
                }
              ]
            },
            1: {
              nimi: {'fi': 'ruotsi'},
              osaamistavoitteet: [
                {
                  pakollisuus: 'pakollinen',
                  laajuus: 5,
                  tavoitteet: {fi: 'Äidinkieli ruotsi pakollinen, tavoitteet teksti'},
                  tunnustaminen: {fi: 'Äidinkieli ruotsi pakollinen, tunnustaminen teksti'},
                  arviointi: {}
                },
                {
                  pakollisuus: 'valinnainen',
                  laajuus: 3,
                  tavoitteet: {fi: 'Äidinkieli ruotsi valinnainen, tavoitteet teksti'},
                  tunnustaminen: {fi: 'Äidinkieli ruotsi valinnainen, tunnustaminen teksti'},
                  arviointi: {}
                }
              ]
            }
          }
        },
        {
          nimi: {fi: 'Toinen kotimainen kieli'},
          $vaihtoehto: 0,
          vaihtoehdot: {
           0: {
             nimi: {'fi': 'ruotsi'},
             osaamistavoitteet: [
               {
               pakollisuus: 'pakollinen',
               laajuus: 1,
               tavoitteet: {fi: 'Toinen kotimainen kieli pakollinen, tavoitteet teksti'},
               tunnustaminen: {fi: 'Toinen kotimainen kieli pakollinen, tunnustaminen teksti'},
               arviointi: {}
               },
               {
               pakollisuus: 'valinnainen',
               laajuus: 3,
               tavoitteet: {fi: 'Toinen kotimainen kieli valinnainen, tavoitteet teksti'},
               tunnustaminen: {fi: 'Toinen kotimainen kieli valinnainen, tunnustaminen teksti'},
               arviointi: {}
               }
            ]
           },
           1: {
             nimi: {'fi': 'suomi'},
             osaamistavoitteet: [
            {
              pakollisuus: 'pakollinen',
              laajuus: 2,
              tavoitteet: {fi: 'Toinen kotimainen kieli pakollinen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Toinen kotimainen kieli pakollinen, tunnustaminen teksti'},
              arviointi: {}
            },
            {
              pakollisuus: 'valinnainen',
              laajuus: 2,
              tavoitteet: {fi: 'Toinen kotimainen kieli valinnainen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Toinen kotimainen kieli valinnainen, tunnustaminen teksti'},
              arviointi: {}
            }
          ]
           }
          }
        },
        {
          nimi: {fi: 'Vieraat kielet'},
          vaihtoehdot: {
            0: {
              osaamistavoitteet: [
            {
              pakollisuus: 'pakollinen',
              laajuus: 2,
              tavoitteet: {fi: 'Vieraat kielet pakollinen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Vieraat kielet pakollinen, tunnustaminen teksti'},
              arviointi: {}
            },
            {
              pakollisuus: 'valinnainen',
              laajuus: 3,
              tavoitteet: {fi: 'Vieraat kielet valinnainen, tavoitteet teksti'},
              tunnustaminen: {fi: 'Vieraat kielet valinnainen, tunnustaminen teksti'},
              arviointi: {}
            }
          ]
            }
          }

        },
      ]
    };
  });
