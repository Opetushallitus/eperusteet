CKEDITOR.dialog.add( 'abbrDialog', function( editor ) {
    return {
        title: 'Abbreviation Properties',
        minWidth: 400,
        minHeight: 200,
        contents: [
            {
                id: 'tab-basic',
                label: 'Basic Settings',
                elements: [
                    {
                        type: 'text',
                        id: 'abbr',
                        label: 'Abbreviation',
                        validate: CKEDITOR.dialog.validate.notEmpty( "Abbreviation field cannot be empty." )
                    },
                    {
                        type: 'select',
                        id: 'title',
                        label: 'Explanation',
                        items: [['--valitse--', 0]],
                        onShow: function () {
                          console.log("onShow");
                          this.clear();
                          this.add('label1', 'value1');
                          var self = this;
                          var uniqueId = 1;
                          $.ajax({
                            type: 'GET',
                            url: 'http://localhost:9000/eperusteet-service/api/perusteenosat/' + editor.config.customData,
                            dataType: 'json',
                            success: function (data) {
                              self.clear();
                              $.each(data, function (key) {
                                self.add(key, ++uniqueId);
                              })
                            }
                          });
                        }
                        //validate: CKEDITOR.dialog.validate.notEmpty( "Explanation field cannot be empty." )
                    }
                ]
            },
        ],
        onOk: function() {
            var dialog = this;

            var abbr = editor.document.createElement( 'abbr' );
            console.log("valittu arvolla", dialog.getValueOf('tab-basic', 'title'));
            //abbr.setAttribute( 'title', dialog.getValueOf( 'tab-basic', 'title' ) );
            //abbr.setText( dialog.getValueOf( 'tab-basic', 'abbr' ) );

            /*var id = dialog.getValueOf( 'tab-adv', 'id' );
            if ( id )
                abbr.setAttribute( 'id', id );*/

            editor.insertElement( abbr );
        }
    };
});
