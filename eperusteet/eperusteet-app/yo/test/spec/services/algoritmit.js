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

describe('Service: Algoritmit', function() {
  var $q,
      service,
      obj,
      puu;

  beforeEach(module('eperusteApp'));
  beforeEach(inject(function(Algoritmit, _$q_) {
    service = Algoritmit;
    $q = _$q_;

    obj = {
      something: {
        complitely: 'different'
      }
    };

    puu = {
      sisalto: '0',
      lapset: [{
        sisalto: '1',
        lapset: []
      }, {
        sisalto: '2',
        lapset: [{
          sisalto: '3',
        }, {
          sisalto: '4',
          lapset: [{
            sisalto: '5',
          }]
        }]
      }]
    };
  }));

  describe('access', function() {
    it('can access nested objects', function() {
      expect(service.access(obj, 'something', 'complitely')).toBe('different');
      expect(service.access(obj, 'something', 'complitely', 'different')).toBe(undefined);
    });
  });

  describe('rajausVertailu', function() {
    it('compares strings incasesensitively', function() {
      var obj = {
        fi: 'Testiolio'
      };

      expect(service.rajausVertailu('testi', obj, 'fi')).toBe(true);
      expect(service.rajausVertailu('TeSTi', obj, 'fi')).toBe(true);
      expect(service.rajausVertailu('testit', obj, 'fi')).toBe(false);
      expect(service.rajausVertailu('rkKij', 'testimerkkijono')).toBe(true);
    });
  });

  describe('mapLapsisolmut', function() {
    it('traverses a tree and returns altered nodes', function() {
      var result = service.mapLapsisolmut(puu, 'lapset', function(solmu) {
        solmu.sisalto = 'etuliite-' + solmu.sisalto;
        return solmu;
      });

      expect(puu.lapset[0].sisalto).toBe('1');
      expect(result[0].sisalto).toBe('etuliite-1');
      expect(result[1].sisalto).toBe('etuliite-2');
      expect(result[1].lapset[0].sisalto).toBe('etuliite-3');
      expect(result[1].lapset[1].lapset[0].sisalto).toBe('etuliite-5');
    });
  });

  describe('kaikilleLapsisolmuille', function() {
    it('can alter the state of all nodes of a tree', function() {
      service.kaikilleLapsisolmuille(puu, 'lapset', function(solmu) {
        solmu.sisalto = 'etuliite-' + solmu.sisalto;
      });

      expect(puu.lapset[0].sisalto).toBe('etuliite-1');
      expect(puu.lapset[1].sisalto).toBe('etuliite-2');
      expect(puu.lapset[1].lapset[0].sisalto).toBe('etuliite-3');
      expect(puu.lapset[1].lapset[1].lapset[0].sisalto).toBe('etuliite-5');
    });
  });

  describe('perusteenSuoritustavanYksikko', function() {
    it('palauttaa perusteen suoritustavan yksikön', function() {
      var yksikko = service.perusteenSuoritustavanYksikko({
        suoritustavat: [{
          suoritustapakoodi: 'naytto'
        }, {
          suoritustapakoodi: 'ops',
          laajuusYksikko: 'osp'
        }]
      }, 'ops');

      expect(yksikko).toBe('osp');
    });
  });

});
