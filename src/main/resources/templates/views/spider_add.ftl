<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>New spider</title>
  
  <script src="/public/js/jsoneditor.js"></script>
	<script>
		JSONEditor.defaults.theme = 'bootstrap3';
	    JSONEditor.defaults.iconlib = 'fontawesome4';
	    //JSONEditor.defaults.options.disable_properties = true;
	    //JSONEditor.defaults.options.disable_edit_json = true;
	    //JSONEditor.defaults.options.no_additional_properties = false;
	    //JSONEditor.defaults.options.required_by_default = true;
	</script>    
</#macro>

<#macro content_header>
		<h1>
            New spider
            <small>a new one please!</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/spiders"><i class="fa fa-tasks"></i> Spiders</a></li>
            <li class="active">Create</li>
          </ol>
</#macro>

<#macro js>
	<script>
		var $output = document.getElementById('output');
		
      // This is the starting value for the editor
      // We will use this to seed the initial editor 
      // and to provide a "Restore to Default" button.
      var starting_value = [
        {
          name: "John Smith",
          age: 35,
          gender: "male",
          location: {
            city: "San Francisco",
            state: "California",
            citystate: ""
          },
          pets: [
            {
              name: "Spot",
              type: "dog",
              fixed: true
            },
            {
              name: "Whiskers",
              type: "cat",
              fixed: false
            }
          ]
        }
      ];
      
      // Initialize the editor
      var editor = new JSONEditor(document.getElementById('editor_holder'),{
        // Enable fetching schemas via ajax
        ajax: true,
        
        // The schema for the editor
        schema: {
          $ref: "/public/schemas/spider.json",
          format: "grid"
        },
        
        // Seed the form with a starting value
        //startval: starting_value,
      });
      
      // Hook up the submit button to log to the console
      document.getElementById('submit').addEventListener('click',function() {
        // Get the value from the editor
        console.log(editor.getValue());
      });
      
      // Hook up the Restore to Default button
      document.getElementById('restore').addEventListener('click',function() {
        editor.setValue(starting_value);
      });
      
      // Hook up the enable/disable button
      document.getElementById('enable_disable').addEventListener('click',function() {
        // Enable form
        if(!editor.isEnabled()) {
          editor.enable();
        }
        // Disable form
        else {
          editor.disable();
        }
      });
      
      // Hook up the validation indicator to update its 
      // status whenever the editor changes
      editor.on('change',function() {
      
      	var json = editor.getValue();

        $output.value = JSON.stringify(json,null,5);
            
            
        // Get an array of errors from the validator
        var errors = editor.validate();
        
        var indicator = document.getElementById('valid_indicator');
        
        // Not valid
        if(errors.length) {
          indicator.style.color = 'red';
          indicator.textContent = "not valid";
        }
        // Valid
        else {
          indicator.style.color = 'green';
          indicator.textContent = "valid";
        }
      });
    </script>
</#macro>

<#macro page_body>
		<div class="row">
			<div class="col-xs-12">
				<span id="valid_indicator"></span>
				<button id='submit'>Submit (console.log)</button>
			    <button id='restore'>Restore to Default</button>
			    <button id='enable_disable'>Disable/Enable Form</button>
			</div>
			
		</div>
		<div class="row">		
			<div class="col-md-7">
	            <div class="box box-info">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Horizontal Form</h3>
	                </div><!-- /.box-header -->
	                  <div class="box-body" id="editor_holder">
	                  </div>
	              </div>
            </div>
            <div class="col-md-5">
            	<div class="box box-warning">
	                <div class="box-header with-border">
	                  <h3 class="box-title">General Elements</h3>
	                </div><!-- /.box-header -->
	                <div class="box-body">
	                  <form role="form">
            			<div class="form-group">
	                      <label>Textarea Disabled</label>
	                      <textarea id='output' class="form-control" rows="50" disabled></textarea>
	                    </div>
	                  </form>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
            </div>
		</div>
</#macro>

<@display_page/>