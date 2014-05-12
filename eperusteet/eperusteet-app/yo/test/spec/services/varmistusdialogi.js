'use strict';

describe('Service: Varmistusdialogi', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Varmistusdialogi;
  beforeEach(inject(function (_Varmistusdialogi_) {
    Varmistusdialogi = _Varmistusdialogi_;
  }));

  it('should do something', function () {
    expect(!!Varmistusdialogi).toBe(true);
  });

});
