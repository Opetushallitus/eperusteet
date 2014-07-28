'use strict';

describe('Service: PerusteenOsat', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var PerusteenOsat;
  beforeEach(inject(function (_PerusteenOsat_) {
    PerusteenOsat = _PerusteenOsat_;
  }));

  it('should do something', function () {
    expect(!!PerusteenOsat).toBe(true);
  });

});
