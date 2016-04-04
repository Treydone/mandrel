<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Data</title>
</#macro>

<#macro css>
  <link href="/public/css/dataTables.bootstrap.css" rel="stylesheet" type="text/css"/>
</#macro>

<#macro content_header>
		<h1>
            Data
            <small>YOUR data</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/jobs"><i class="fa fa-tasks"></i> Jobs</a></li>
            <li><a href="/jobs/${job.id?c}"><i class="fa fa-database"></i> ${job.name}</a></li>
            <li class="active">Data</li>
          </ol>
</#macro>

<#macro js>
	<script src="/webjars/datatables/1.10.7/js/jquery.dataTables.js"></script>
	<script src="/public/js/dataTables.bootstrap.js" type="text/javascript"></script>
	<script>
		$(document).ready(function() {
		    $('#datatable').dataTable( {
		        "processing": true,
		        "serverSide": true,
		        "lengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
		        "scrollX": true,
		        "searching": false,
		        "ajax": {
		            "url": "/jobs/${job.id?c}/data/${extractor.getName()}",
		            "type": "POST"
		        },
		        "columns": [
		        	<#list extractor.fields as field>
                  	{ 
                  		"data": "${field.name}[, ]",
                  		"orderable": false,
                  		"render": function ( data, type, full, meta ) {
					      return type === 'display' && data.length > 40 ? '<span title="'+data+'">'+data.substr( 0, 38 )+'...</span>' : data;
					    }
                  	}<#sep>,</#sep>
                  	</#list> 
		        ]
		    } );
		} );		
	</script>
</#macro>

<#macro page_body>
		<div class="row">
            <div class="col-xs-12">
              <div class="box">
                <div class="box-header">
                  <h3 class="box-title">Table</h3>
                </div><!-- /.box-header -->
                <div class="box-body">
                  <table id="datatable" class="table table-bordered table-striped">
                    <thead>
                      <tr>
                      	<#list extractor.fields as field>
                      	<th>${field.name}</th>
                      	</#list> 
                      </tr>
                    </thead>
                    <tbody>
                    </tbody>
                    <tfoot>
                      <tr>
                        <#list extractor.fields as field>
                      	<th>${field.name}</th>
                      	</#list> 
                      </tr>
                      </tr>
                    </tfoot>
                  </table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
            </div><!-- /.col -->
          </div><!-- /.row -->		                    
</#macro>

<@display_page/>