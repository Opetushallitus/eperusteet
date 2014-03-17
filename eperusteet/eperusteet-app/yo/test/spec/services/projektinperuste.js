'use strict';

describe('Service: Projektinperuste', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Projektinperuste;
  beforeEach(inject(function (_Projektinperuste_) {
    Projektinperuste = _Projektinperuste_;
  }));

  it('should do something', function () {
    expect(!!Projektinperuste).toBe(true);
  });

});
