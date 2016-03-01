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

<#macro page_body>
		<div class="row">		
            <div class="col-md-12">
            	<div class="box">
	                <div class="box-body">
	                  <div class="callout callout-success">
	                    <h4>Create a spider with the form</h4>
	                    <p>The easiest method to create a new spider.</p>
	                    <a href="/spiders/add/form"><button type="button" class="btn btn-outline"><i class="fa fa-plus"></i> Create one!</button></a>
	                  </div>
	                </div><!-- /.box-body -->
	                <div class="box-body">
	                  <div class="callout callout-info">
	                    <h4>Create a spider with a JSON definition</h4>
	                    <p>For fine tuning and advanced users.</p>
	                    <a href="/spiders/add/definition"><button type="button" class="btn btn-outline"><i class="fa fa-plus"></i> Create one!</button></a>
	                  </div>
	                </div><!-- /.box-body -->
	                <div class="box-body">
	                  <div class="callout callout-warning">
	                    <h4>Create a spider with the API</h4>
	                    <p>Automation is key, have a look on the api</p>
	                    <a href="/webjars/swagger-ui/2.0.24/index.html?url=/api-docs"><button type="button" class="btn btn-outline"><i class="fa fa-plus"></i> Create one!</button></a>
	                  </div>
	                </div><!-- /.box-body -->
                </div><!-- /.box -->
             </div>
		</div>
</#macro>

<@display_page/>