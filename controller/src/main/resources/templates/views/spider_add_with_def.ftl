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
            	<div class="box box-warning">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Spider definition</h3>
	                </div><!-- /.box-header -->
	                <form role="form" method="POST" action="/spiders/add">
	                <div class="box-body">
            			<div class="form-group">
	                      <label>JSON</label>
	                      <textarea id='output' class="form-control" rows="20" name="definition" id="definition"></textarea>
	                    </div>
                     </div><!-- /.box-body -->
                     <div class="box-footer">
                      <button type="submit" class="btn btn-info pull-right">Go</button>
                     </div><!-- /.box-footer -->
                    </form>
                </div><!-- /.box -->
             </div>
		</div>
</#macro>

<@display_page/>