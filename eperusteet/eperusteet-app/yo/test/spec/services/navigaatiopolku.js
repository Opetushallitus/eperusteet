'use strict';

describe('Service: Navigaatiopolku', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Navigaatiopolku;
  beforeEach(inject(function (_Navigaatiopolku_) {
    Navigaatiopolku = _Navigaatiopolku_;
  }));

  it('should do something', function () {
    expect(!!Navigaatiopolku).toBe(true);
  });

});
