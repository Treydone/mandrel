<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>New spider</title>
  
  <script src="/public/js/jsoneditor.js"></script>
  <script src="/public/js/jsoneditor-theme.js"></script>
	<script>
		JSONEditor.defaults.theme = 'adminlte';
	    JSONEditor.defaults.iconlib = 'bootstrap3';
	    JSONEditor.defaults.options.disable_properties = true;
	    JSONEditor.defaults.options.disable_edit_json = true;
	    //JSONEditor.defaults.options.no_additional_properties = false;
	    JSONEditor.defaults.options.required_by_default = true;
	    JSONEditor.defaults.options.keep_oneof_values = false;
	    JSONEditor.defaults.options.disable_collapse = true;
	    
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
	
      var prepare = function(baseValue, baseEditorId, ref, outputId, validationId) {
      	  var $output = document.getElementById(outputId);
	      var $output_errors = document.getElementById(outputId + "_errors");
	      var $output_errors_nb = document.getElementById(outputId + "_errors_nb");
	      var starting_value = baseValue;
	      var editor = new JSONEditor(document.getElementById(baseEditorId),{
	        ajax: true,
	        schema: {
	          $ref: ref
	        },
	        startval: starting_value,
	      });
	      editor.on('change',function() {
	      	var json = editor.getValue();
	        $output.value = JSON.stringify(json,null,5);
	        var errors = editor.validate();
	        $output_errors.getElementsByTagName("ul")[0].innerHTML = '';
	        // Not valid
	        if(errors.length) {
	          console.log(errors);
	          $output_errors.className = $output_errors.className.replace(/\bhidden\b/,'');
	          errors.forEach(function (error, i) {
	          	$output_errors.getElementsByTagName("ul")[0].innerHTML += "<li>" + errors[i].message + "(" + errors[i].path + ")" + "</li>";
	          });
	          $output_errors_nb.className = $output_errors_nb.className.replace(/\bhidden\b/,'');
	          $output_errors_nb.setAttribute("data-original-title",errors.length + " errors");
	          $output_errors_nb.innerHTML=errors.length;
	        }
	        // Valid
	        else {
	          if($output_errors.className.indexOf('hidden') == -1) {
  	            $output_errors.className += 'hidden';
	          }
	          if($output_errors_nb.className.indexOf('hidden') == -1) {
  	            $output_errors_nb.className += 'hidden';
	          }
	        }
	      });
      }
      
      prepare(${baseValue}, 'base_editor_holder', "/public/schemas/base.json", 'output_base', 'valid_indicator_base');
      prepare(${storesValue}, 'stores_editor_holder', "/public/schemas/stores.json", 'output_stores', 'valid_indicator_stores');
      prepare(${frontierValue}, 'frontier_editor_holder', "/public/schemas/frontier.json", 'output_frontier', 'valid_indicator_frontier');
      prepare(${extractionValue}, 'extraction_editor_holder', "/public/schemas/extraction.json", 'output_extraction', 'valid_indicator_extraction');
      prepare(${politenessValue}, 'politeness_editor_holder', "/public/schemas/politeness.json", 'output_politeness', 'valid_indicator_politeness');
      prepare(${advancedValue}, 'advanced_editor_holder', "/public/schemas/advanced.json", 'output_advanced', 'valid_indicator_advanced');
      
    </script>
</#macro>

<#macro page_body>
		<div class="row">
			<div class="col-md-7">
			
	          <!-- Custom Tabs -->
              <div class="nav-tabs-custom">
                <ul class="nav nav-tabs">
                  <li class="active"><a href="#tab_1" data-toggle="tab">Base <span id="output_base_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
                  <li><a href="#tab_2" data-toggle="tab">Stores <span id="output_stores_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
                  <li><a href="#tab_3" data-toggle="tab">Frontier <span id="output_frontier_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
                  <li><a href="#tab_4" data-toggle="tab">Extraction <span id="output_extraction_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
                  <li><a href="#tab_5" data-toggle="tab">Politeness <span id="output_politeness_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
                  <li><a href="#tab_6" data-toggle="tab">Advanced <span id="output_advanced_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
                  <li class="pull-right"><a class="text-muted"><i class="fa fa-gear"></i></a></li>
                </ul>
                <div class="tab-content">
                
                  <div class="tab-pane active" id="tab_1">
                  <div class="alert alert-warning hidden" id="output_base_errors">
		            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
		            <ul></ul>
		          </div>
                    <div class="box-body" id="base_editor_holder"></div>
                  </div><!-- /.tab-pane -->
                  
                  <div class="tab-pane" id="tab_2">
                  	 <div class="alert alert-warning hidden" id="output_stores_errors">
			            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
			            <ul></ul>
			          </div>
					<div class="box-body" id="stores_editor_holder"></div>
                  </div><!-- /.tab-pane -->
                  
                  <div class="tab-pane" id="tab_3">
                  	 <div class="alert alert-warning hidden" id="output_frontier_errors">
			            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
			            <ul></ul>
			          </div>
					<div class="box-body" id="frontier_editor_holder"></div>
                  </div><!-- /.tab-pane -->
                  
                  <div class="tab-pane" id="tab_4">
                  	<div class="alert alert-warning hidden" id="output_extraction_errors">
			            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
			            <ul></ul>
			          </div>
					<div class="box-body" id="extraction_editor_holder"></div>
                  </div><!-- /.tab-pane -->
                  
                  <div class="tab-pane" id="tab_5">
                  	<div class="alert alert-warning hidden" id="output_politeness_errors_nb">
			            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
			            <ul></ul>
			          </div>
					<div class="box-body" id="politeness_editor_holder"></div>
                  </div><!-- /.tab-pane -->
                                    
                  <div class="tab-pane" id="tab_6">
                  	<div class="alert alert-warning hidden" id="output_advanced_errors">
			            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
			            <ul></ul>
			          </div>
					<div class="box-body" id="advanced_editor_holder"></div>
                  </div><!-- /.tab-pane -->
                  
                </div><!-- /.tab-content -->
              </div><!-- nav-tabs-custom -->
            </div>
            <div class="col-md-5">
            	<div class="box box-warning">
	                <div class="box-header with-border">
	                  <h3 class="box-title">JSON</h3>
	                </div><!-- /.box-header -->
	                <div class="box-body">
	                  <form role="form">
            			<div class="form-group">
	                      <label>Definition</label>
	                      <textarea id='output_base' class="form-control" rows="20" disabled></textarea>
	                    </div>
	                    <div class="form-group">
	                      <label>Definition</label>
	                      <textarea id='output_stores' class="form-control" rows="20" disabled></textarea>
	                    </div>
	                    <div class="form-group">
	                      <label>Definition</label>
	                      <textarea id='output_frontier' class="form-control" rows="20" disabled></textarea>
	                    </div>
	                    <div class="form-group">
	                      <label>Definition</label>
	                      <textarea id='output_extraction' class="form-control" rows="20" disabled></textarea>
	                    </div>
	                    <div class="form-group">
	                      <label>Definition</label>
	                      <textarea id='output_advanced' class="form-control" rows="20" disabled></textarea>
	                    </div>
	                  </form>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
            </div>
		</div>
</#macro>

<@display_page/>