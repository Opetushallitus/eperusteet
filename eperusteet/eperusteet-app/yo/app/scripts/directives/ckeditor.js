'use strict';
/*global CKEDITOR,$,_*/

angular.module('eperusteApp')
  .directive('ckeditor', function($q, $filter, $rootScope) {
    CKEDITOR.disableAutoInline = true;

    return {
      restrict: 'A',
      require: 'ngModel',
      scope: {
        editorPlaceholder: '@?'
      },
      link: function(scope, element, attrs, ctrl) {
        var placeholderText = null;
        var editingEnabled = (element.attr('editing-enabled') || 'true') === 'true';
        if(editingEnabled) {
          element.addClass('edit-mode');
        }
        element.attr('contenteditable', 'true');
        
        function getPlaceholder() {
          if(scope.editorPlaceholder) {
            return $filter('translate')(scope.editorPlaceholder);
          } else {
            return '';
          }
        }
        
        console.log('CKEDITOR!!');
        var editor = CKEDITOR.instances[attrs.id];
        if (editor) {
          console.log('editor exist');
          return;
        }
        
        editor = CKEDITOR.inline(element[0], {
          toolbar: 'Basic',
          removePlugins: 'resize,elementspath',
          extraPlugins: 'divarea,sharedspace',
          extraAllowedContent: '*[contenteditable]',
          language: 'fi',
          'entities_latin': false,
          sharedSpaces: {
            top: 'ck-toolbar-top'
          },
          readOnly: !editingEnabled
        });

        $rootScope.$on('$translateChangeSuccess', function() {
          console.log('translate change finished');
          placeholderText = getPlaceholder();
          ctrl.$render();
        });
        
        scope.$on('enableEditing', function() {
          editingEnabled = true;
          editor.setReadOnly(!editingEnabled);
          element.addClass('edit-mode');
        });
        
        scope.$on('disableEditing', function() {
          editingEnabled = false;
          editor.setReadOnly(!editingEnabled);
          element.removeClass('edit-mode');
        });
        
        editor.on('focus', function() {
          console.log('focus');
          if(editingEnabled) {
            element.removeClass('has-placeholder');
            $('#toolbar').show();
            if(_.isEmpty(ctrl.$viewValue)) {
              editor.setData('');
            }
          }
        });

        editor.on('change', function() {
          console.log('pasteState');
        });

        editor.on('instanceReady', function() {
          console.log('ready');
        });

        editor.on('blur', function() {
          console.log('blur');
          //element.attr('contenteditable','false');
          if (editor.checkDirty()) {
            var data = editor.getData();
            scope.$apply(function() {
              ctrl.$setViewValue(data);
              scope.$broadcast('edited');
            });
            if(_.isEmpty(data)) {
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
          if (editor) {
            if(_.isString(ctrl.$viewValue) && _.isEmpty(ctrl.$viewValue) && placeholderText) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
            } else {
              element.removeClass('has-placeholder');
              editor.setData(ctrl.$viewValue);
            }
          }
        };
        
        placeholderText = getPlaceholder();
        ctrl.$render();
        
        //element.click(ev);
        
      }
    };
  });
