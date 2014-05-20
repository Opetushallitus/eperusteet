'use strict';

describe('Service: Muokkaustila', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Muokkaustila;
  beforeEach(inject(function (_Muokkaustila_) {
    Muokkaustila = _Muokkaustila_;
  }));

  it('should do something', function () {
    expect(!!Muokkaustila).toBe(true);
  });

});
