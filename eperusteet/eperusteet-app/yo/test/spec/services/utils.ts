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

describe('Service: Utils', function() {
  var service;
  var fn;
  beforeEach(module('eperusteApp'));
  beforeEach(inject(function(Utils) {
    service = Utils;
    fn = service.presaveStrip;
  }));

  describe('presaveStrip', function() {
    it('does nothing for non-objects or non-arrays', function() {
      expect(fn(undefined)).toBe(undefined);
      expect(fn('string')).toBe('string');
      expect(fn(98)).toBe(98);
    });

    it('strips objects in arrays', function() {
      var arr = [
        {a: 1, b: 2, $extra: 'foo'},
        {c: 3, $d: 'a'}
      ];
      expect(fn(arr)).toEqual([{a: 1, b: 2},{c: 3}]);
    });

    it('strips deep objects', function() {
      var obj = {
        arr: [
          {a: 1, b: 2, $extra: 'foo'},
          {c: 3, $d: 'a'}
        ],
        nimi: {$joku: true, fi: 'asdf'},
        $extra: false,
        foo: {
          bar : {
            $remove: {
              thiswhole: 'thing',
              $doesnt: 'matter'
            },
            dontremove: 5678
          }
        }
      };
      expect(fn(obj)).toEqual({
        arr: [{a: 1, b: 2},{c: 3}],
        nimi: {fi: 'asdf'},
        foo: {bar: {dontremove: 5678}}
      });
    });
  });

});
