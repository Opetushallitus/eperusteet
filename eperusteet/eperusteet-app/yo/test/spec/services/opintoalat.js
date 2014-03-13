'use strict';

describe('Service: Opintoalat', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Opintoalat;
  beforeEach(inject(function (_Opintoalat_) {
    Opintoalat = _Opintoalat_;
  }));

  it('should do something', function () {
    expect(!!Opintoalat).toBe(true);
  });

});
