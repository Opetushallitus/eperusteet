'use strict';
/*global CKEDITOR,$*/

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
        console.log('creating editor...');
        console.log('editing enable: ' + editingEnabled);
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

        console.log('attaching events');

        $rootScope.$on('$translateChangeSuccess', function() {
          console.log('kieli vaihtui');
          placeholderText = getPlaceholder();
          ctrl.$render();
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
        
        editor.on('focus', function() {
          console.log('focus');
          if(editingEnabled) {
            element.removeClass('has-placeholder');
            console.log('set toolbar -> show');
            $('#toolbar').show();
            if(editor.getData() === placeholderText) {
              editor.setData('');
            }
          }
//          var h = $('#ck-toolbar-top').height();
//          console.log(h);
//          $('body').css('padding-top', h);
          //element.attr('contenteditable','false');
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
            if(!data) {
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
            if(angular.isString(ctrl.$viewValue) && ctrl.$viewValue.length === 0 && placeholderText) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
            } else {
              editor.setData(ctrl.$viewValue);
            }
          }
        };

//        getPlaceholderPromise().then(function(resolvedPlaceholder) {
//          placeholderText = resolvedPlaceholder;
//          
//          // load init value from DOM
//          ctrl.$render();
//        });
        
        placeholderText = getPlaceholder();
        ctrl.$render();
        
        //element.click(ev);
        
      }
    };
  });
