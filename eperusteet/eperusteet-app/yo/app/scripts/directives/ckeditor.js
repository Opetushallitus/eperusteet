'use strict';
/*global CKEDITOR,$*/

angular.module('eperusteApp')
  .run(function() {
    CKEDITOR.disableAutoInline = true;
  })
  .directive('ckeditor', function($translate, $q) {
    return {
      restrict: 'A',
      require: 'ngModel',
      scope: {},
      link: function(scope, element, attrs, ctrl) {
        var placeholderText = null;
        var editingEnabled = (element.attr('editing-enabled') || 'true') === 'true';
        element.attr('contenteditable', 'true');
        function getPlaceholderPromise() {
          var deferred = $q.defer();
          if (element.attr('editor-placeholder')) {
            console.log('Placeholder: ' + element.attr('editor-placeholder'));
            $translate(element.attr('editor-placeholder')).then(function(value) {
              deferred.resolve(value);
            },
              function() {
                deferred.resolve(element.attr('editor-placeholder'));
              });
          } else {
            deferred.resolve();
          }
          return deferred.promise;
        }

        console.log('CKEDITOR!!');
        var editor = CKEDITOR.instances[attrs.id];
        if (editor) {
          console.log('editor exist');
          return;
        }
        console.log('creating editor...');
        console.log('editing enable: ' + editingEnabled);
        editor = CKEDITOR.inline(element[0], {
          toolbar: 'Basic',
          removePlugins: 'resize,elementspath,scayt,wsc',
          extraPlugins: 'divarea,sharedspace',
          extraAllowedContent: '*[contenteditable]',
          language: 'fi',
          'entities_latin': false,
          sharedSpaces: {
            top: 'ck-toolbar-top'
          },
          readOnly: !editingEnabled
        });

        console.log('attaching events');

        scope.$on('kieliVaihtui', function() {
          getPlaceholderPromise().then(function(resolvedPlaceholder) {
            placeholderText = resolvedPlaceholder;
            ctrl.$render();
          });
        });

        scope.$on('enableEditing', function() {
          editingEnabled = true;
          editor.setReadOnly(!editingEnabled);
          console.log('enable editing');
        });

        scope.$on('disableEditing', function() {
          editingEnabled = false;
          editor.setReadOnly(!editingEnabled);
          console.log('disable editing');
        });

        scope.$on('$destroy', function() {
          if (editor) {
            editor.destroy(false);
          }
        });

        editor.on('focus', function() {
          console.log('focus');
          if (editingEnabled) {
            element.removeClass('has-placeholder');
            console.log('set toolbar -> show');
            $('#toolbar').show();
            if (editor.getData() === placeholderText) {
              editor.setData('');
            }
          }
        });

        //editor.on('change', function() {
        //});

        //editor.on('instanceReady', function() {
        //});

        editor.on('blur', function() {
          console.log('blur');
          //element.attr('contenteditable','false');
          if (editor.checkDirty()) {
            var data = editor.getData();
            scope.$apply(function() {
              ctrl.$setViewValue(data);
              scope.$broadcast('edited');
            });
            if (!data) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
            }
          }
          $('#toolbar').hide();
          $('body').css('padding-top', 0);
        });

        // model -> view

        ctrl.$render = function() {
          console.log('render: ' + ctrl.$viewValue);
          console.log(editor);
          if (editor) {
            if (angular.isString(ctrl.$viewValue) && ctrl.$viewValue.length === 0 && placeholderText) {
              console.log('render placeholder to editor');
              console.log(placeholderText);
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
            } else {
              editor.setData(ctrl.$viewValue);
            }
          }
          console.log(editor);
        };

        getPlaceholderPromise().then(function(resolvedPlaceholder) {
          placeholderText = resolvedPlaceholder;
          // load init value from DOM
          ctrl.$render();
        });

      }
    };
  });
