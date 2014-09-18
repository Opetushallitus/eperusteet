// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function(config) {
  config.set({
    // base path, that will be used to resolve files and exclude
    basePath: '',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    // list of files / patterns to load in the browser
    files: [
      'app/bower_components/jquery/dist/jquery.js',
      'app/bower_components/jquery-ui/ui/core.js',
      'app/bower_components/jquery-ui/ui/widget.js',
      'app/bower_components/jquery-ui/ui/mouse.js',
      'app/bower_components/jquery-ui/ui/sortable.js',
      'app/bower_components/angular/angular.js',
      'app/bower_components/angular-route/angular-route.js',
      'app/bower_components/angular-sanitize/angular-sanitize.js',
      'app/bower_components/angular-resource/angular-resource.js',
      'app/bower_components/angular-animate/angular-animate.js',
      'app/bower_components/angular-mocks/angular-mocks.js',
      'app/bower_components/angular-translate/angular-translate.js',
      'app/bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js',
      'app/bower_components/angular-ui-utils/ui-utils.js',
      'app/bower_components/angular-ui-bootstrap-bower/ui-bootstrap.js',
      'app/bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls.js',
      'app/bower_components/angular-ui-utils/ui-utils.js',
      'app/bower_components/angular-ui-sortable/sortable.js',
      'app/bower_components/angular-elastic/elastic.js',
      'app/bower_components/angular-ui-router/release/angular-ui-router.js',
      'app/bower_components/angular-ui-tree/dist/angular-ui-tree.min.js',
      'app/bower_components/ckeditor/ckeditor.js',
      'app/bower_components/lodash/dist/lodash.js',
      'app/bower_components/momentjs/moment.js',
      'app/scripts/*.js',
      'app/scripts/**/*.js',
      'test/mock/**/*.js',
      'test/spec/**/*.js',
      'app/views/**/*.html'
    ],

    // list of files / patterns to exclude
    exclude: [],

    // web server port
    port: 9002,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,


    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['PhantomJS'],

    plugins: [
      'karma-jasmine',
      'karma-phantomjs-launcher',
      'karma-ng-html2js-preprocessor'
    ],

    preprocessors: {
      'app/**/*.html': ['ng-html2js']
    },

    ngHtml2JsPreprocessor: {
      stripPrefix: 'app/'
    },

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
