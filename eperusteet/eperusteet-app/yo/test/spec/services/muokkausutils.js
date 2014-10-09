'use strict';

describe('Service: MuokkausUtils', function() {
  var service, data;

  beforeEach(module('eperusteApp'));
  beforeEach(inject(function(MuokkausUtils) {
    service = MuokkausUtils;
    data = {
      taso1: {
        taso2: {
          taso3: {
            avain: 'arvo'
          }
        },
        taso2array: [
          {nimi: 'item1'},
          {nimi: 'item2'},
        ]
      }
    };
  }));

  it('has kertoo onko polku olemassa', function() {
    expect(service.nestedHas(data, 'taso1.taso2.taso3', '.')).toBe(true);
    expect(service.nestedHas(data, 'taso1.taso2.taso4', '.')).toBe(false);
    expect(service.nestedHas(data, 'taso1.taso2array[0]', '.')).toBe(true);
    expect(service.nestedHas(data, 'taso1.taso2array[3]', '.')).toBe(false);
  });

  it('get saa datan syvästä hierarkiasta', function() {
    expect(service.nestedGet(data, 'taso1.taso2.taso3.avain', '.')).toBe('arvo');
  });

  it('set asettaa datan syvään hierarkiaan', function() {
    service.nestedSet(data, 'taso1.taso2.taso3.avain', '.', 'uusiarvo');
    expect(service.nestedGet(data, 'taso1.taso2.taso3.avain', '.')).toBe('uusiarvo');
  });

  it('set asettaa täysin uuden objektin', function() {
    service.nestedSet(data, 'taso1.taso2.uusitaso.nimi', '.', 'uusiarvo');
    expect(service.nestedGet(data, 'taso1.taso2.uusitaso.nimi', '.')).toBe('uusiarvo');
  });

  it('get saa datan taulukosta', function() {
    expect(service.nestedGet(data, 'taso1.taso2array[0].nimi', '.')).toBe('item1');
    expect(service.nestedGet(data, 'taso1.taso2array[1].nimi', '.')).toBe('item2');
  });
});
