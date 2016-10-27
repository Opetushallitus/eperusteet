declare var exports: any;

exports.config = {
    seleniumAddress: 'http://localhost:4444/wd/hub',
    specs: ['e2e/result.js'],
    jasmineNodeOpts: {
        showColors: true
    }
};
