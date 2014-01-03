'use strict';

describe('Service: YleinenData', function() {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var YleinenData;
  beforeEach(inject(function(_YleinenData_) {
    YleinenData = _YleinenData_;
  }));

  it('should do something', function() {
    expect(!!YleinenData).toBe(true);
  });

});
