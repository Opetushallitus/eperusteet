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

describe('Service: Muodostumissaannot', function() {
  var $q,
      service,
      algo,
      viitteet,
      rakenne;

  beforeEach(module('eperusteApp'));
  beforeEach(inject(function(Muodostumissaannot, Algoritmit, _$q_) {
    service = Muodostumissaannot;
    algo = Algoritmit;
    $q = _$q_;

    rakenne = {
      rooli: 'määritelty',
      nimi: 'ROOT',
      muodostumisSaanto: {
        laajuus: {
          minimi: 140,
          maksimi: 140,
          yksikko: null
        },
      },
      osaamisala: null,
      osat: [{
        nimi: 'X',
        rooli: 'määrittelemätön',
        muodostumisSaanto: {
          laajuus: {
            minimi: 20,
            maksimi: 30,
            yksikko: null
          },
        },
        osaamisala: null,
        osat: [],
      }, {
        pakollinen: false,
        _tutkinnonOsaViite: '9228'
      }, {
        nimi: 'A',
        rooli: 'määritelty',
        muodostumisSaanto: {
          laajuus: {
            minimi: 50,
            maksimi: 60,
            yksikko: null
          },
        },
        osaamisala: null,
        osat: [{ _tutkinnonOsaViite: '9975' }, {
          _tutkinnonOsaViite: '11299'
        }, {
          _tutkinnonOsaViite: '8690'
        }, {
          _tutkinnonOsaViite: '8204'
        }, {
          _tutkinnonOsaViite: '9502'
        }],
      }, {
        nimi: 'B',
        rooli: 'määritelty',
        muodostumisSaanto: {
          laajuus: {
            minimi: 30,
            maksimi: 35,
            yksikko: null
          },
        },
        osaamisala: null,
        osat: [{ _tutkinnonOsaViite: '10596' },
          { _tutkinnonOsaViite: '8018' },
          { _tutkinnonOsaViite: '10195' }],
      }],
    };

    viitteet = {
      8018: {
        id: 8018,
        laajuus: 20,
        tyyppi: 'normaali',
        _tutkinnonOsa: '8019'
      },
      8204: {
        id: 8204,
        laajuus: 20,
        tyyppi: 'normaali',
        _tutkinnonOsa: '8205'
      },
      8444: {
        id: 8444,
        laajuus: 20,
        tyyppi: 'normaali',
        _tutkinnonOsa: '8445'
      },
      8690: {
        id: 8690,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '8691'
      },
      8934: {
        id: 8934,
        laajuus: 20,
        tyyppi: 'normaali',
        _tutkinnonOsa: '8935'
      },
      9228: {
        id: 9228,
        laajuus: 20,
        tyyppi: 'normaali',
        _tutkinnonOsa: '9229'
      },
      9502: {
        id: 9502,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '9503'
      },
      9723: {
        id: 9723,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '9724'
      },
      9975: {
        id: 9975,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '9976'
      },
      10195: {
        id: 10195,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '10196'
      },
      10419: {
        id: 10419,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '10420'
      },
      10596: {
        id: 10596,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '10597'
      },
      10792: {
        id: 10792,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '10793'
      },
      10947: {
        id: 10947,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '10948'
      },
      11113: {
        id: 11113,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '11114'
      },
      11299: {
        id: 11299,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '11300'
      },
      11445: {
        id: 11445,
        laajuus: 10,
        tyyppi: 'normaali',
        _tutkinnonOsa: '11446'
      }
    };

  }));

  describe('Muodostumissäännöt', function() {

    it('pystyy laskemaan laajuudet ryhmille', function() {
      function testaa(osa, laajuus, vaadittu) {
        expect(osa.$laajuus).toBe(laajuus);
        expect(osa.$vaadittuLaajuus).toBe(vaadittu);
      }

      service.laskeLaajuudet(rakenne, viitteet);
      testaa(rakenne, 145, 140);

      algo.kaikilleLapsisolmuille(rakenne, 'osat', function(osa) {
        if (osa.osat) {
          if (osa.nimi === 'A') { testaa(osa, 60, 60); }
          else if (osa.nimi === 'B') { testaa(osa, 40, 35); }
          else if (osa.nimi === 'X') { testaa(osa, 30, 30); }
        }
      });
    });

  });

});
