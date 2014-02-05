'use strict';

describe('Service: Navigaatiopolku', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Navigaatiopolku;
  beforeEach(inject(function (_navigaatiopolku_) {
    Navigaatiopolku = _navigaatiopolku_;
  }));

  it('should do something', function () {
    expect(!!Navigaatiopolku).toBe(true);
  });

});
