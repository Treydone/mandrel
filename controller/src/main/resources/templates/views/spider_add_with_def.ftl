<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>New spider</title>
  
  <link href="/webjars/jsoneditor/5.0.1/dist/jsoneditor.min.css" rel="stylesheet" type="text/css">
  
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
<script src="/webjars/jsoneditor/5.0.1/dist/jsoneditor.min.js"></script>
<script>
    // create the editor
      var container = document.getElementById("definition");
    
      var options = {
	    mode: 'code',
	    modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
	    onError: function (err) {
	      alert(err.toString());
	    }
	  };
	  
	  var json = ${baseValue};
	  json.stores = ${storesValue};
	  json.frontier = ${frontierValue};
	  json.extractors = ${extractionValue};
	  json.politeness = ${politenessValue};
	  json.client = ${advancedValue};
	  
      var editor = new JSONEditor(container, options, json);
      
	  document.getElementById('submit').addEventListener('click',function() {
	     var form = document.createElement("form");
		form.setAttribute("method", "post");
		form.setAttribute("action", "/spiders/add/definition");
		
		var hiddenField = document.createElement("input");              
		hiddenField.setAttribute("type", "hidden");
        hiddenField.setAttribute("name", "definition");
        hiddenField.setAttribute("value", JSON.stringify(editor.get()));
		form.appendChild(hiddenField);
		document.body.appendChild(form);
		
		form.submit();
      });

</script>
</#macro>

<#macro page_body>
		<div class="row">		
            <div class="col-md-12">
            	<div class="box box-warning">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Spider definition</h3>
	                </div><!-- /.box-header -->
	             
	             <div class="box-body">
	             <#if errors??>
                  <div class="alert alert-warning">
		            <h4><i class="icon fa fa-warning"></i> Alert!</h4>
		            <ul>
	                  <#list errors as error>
		            	<li>${error}</li>
			          </#list>
		            </ul>
		          </div>
		          </div>
		          
		          </#if>
	                 <div class="form-control" name="definition" id="definition" style="height: 800px;"></div>
                     <div class="box-footer">
                      <button id="submit" type="submit" class="btn btn-info pull-right">Go</button>
                     </div><!-- /.box-footer -->
                </div><!-- /.box -->
             </div>
		</div>
</#macro>

<@display_page/>