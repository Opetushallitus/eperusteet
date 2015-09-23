'use strict';

describe('Service: Kayttajaprofiilit', function() {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Kayttajaprofiilit;
  beforeEach(inject(function(_Kayttajaprofiilit_) {
    Kayttajaprofiilit = _Kayttajaprofiilit_;
  }));

  it('should do something', function() {
    expect(!!Kayttajaprofiilit).toBe(true);
  });

});
