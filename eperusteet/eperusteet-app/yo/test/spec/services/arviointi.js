'use strict';

describe('Service: Arviointi', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Arviointi;
  beforeEach(inject(function (_Arviointi_) {
    Arviointi = _Arviointi_;
  }));

  it('should do something', function () {
    expect(!!Arviointi).toBe(true);
  });

});
