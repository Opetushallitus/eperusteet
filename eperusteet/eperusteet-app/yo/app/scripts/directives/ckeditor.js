'use strict';
/*global CKEDITOR,$*/

angular.module('eperusteApp')
  .directive('ckeditor', function() {
    CKEDITOR.disableAutoInline = true;

    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        element.attr('contenteditable', 'true');

        console.log('CKEDIOR!!');
        var editor = CKEDITOR.instances[attrs.id];
        if (editor) {
          console.log('editor exist');
          return;
        }
        console.log('creating editor...');

        editor = CKEDITOR.inline(element[0], {
          toolbar: 'Basic',
          removePlugins: 'resize,elementspath',
          extraPlugins: 'divarea,sharedspace',
          extraAllowedContent: '*[contenteditable]',
          language: 'fi',
          'entities_latin': false,
          sharedSpaces: {
            top: 'ck-toolbar-top'
          }
        });

        console.log('attaching events');

        editor.on('focus', function() {
          console.log('focus');
          if(element.attr('contenteditable') === 'true') {
            console.log('set toolbar -> show');
            $('#toolbar').show();
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
          }
          $('#toolbar').hide();
          $('body').css('padding-top', 0);
        });

        // model -> view

        ctrl.$render = function() {
          console.log('render');
          console.log(editor);
          if (editor) {
            editor.setData(ctrl.$viewValue);
          }
        };

        // load init value from DOM
        ctrl.$render();
        //element.click(ev);

      }
    };
  });
