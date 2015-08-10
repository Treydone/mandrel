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
            <li><a href="/spiders"><i class="fa fa-tasks"></i> Spiders</a></li>
            <li><a href="/spiders/${spider.id}"><i class="fa fa-database"></i> ${spider.name}</a></li>
            <li class="active">Data</li>
          </ol>
</#macro>

<#macro js>
	<script src="/webjars/datatables/1.10.7/js/jquery.dataTables.min.js"></script>
	<script src="/public/js/dataTables.bootstrap.min.js" type="text/javascript"></script>
	<script>
		$(document).ready(function() {
		    $('#datatable').dataTable( {
		        "processing": true,
		        "serverSide": true,
		        "lengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
		        "scrollX": true,
		        "searching": false,
		        "ajax": {
		            "url": "/spiders/${spider.id}/data/${extractor}",
		            "type": "POST"
		        },
		        "columns": [
		            { "data": "first_name" },
		            { "data": "last_name" },
		            { "data": "position" },
		            { "data": "office" },
		            { "data": "salary" }
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
                  <h3 class="box-title">Data Table With Full Features</h3>
                </div><!-- /.box-header -->
                <div class="box-body">
                  <table id="datatable" class="table table-bordered table-striped">
                    <thead>
                      <tr>
                        <th>Rendering engine</th>
                        <th>Browser</th>
                        <th>Platform(s)</th>
                        <th>Engine version</th>
                        <th>CSS grade</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>Trident</td>
                        <td>Internet Explorer 4.0</td>
                        <td>Win 95+</td>
                        <td> 4</td>
                        <td>X</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr>
                        <th>Rendering engine</th>
                        <th>Browser</th>
                        <th>Platform(s)</th>
                        <th>Engine version</th>
                        <th>CSS grade</th>
                      </tr>
                    </tfoot>
                  </table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
            </div><!-- /.col -->
          </div><!-- /.row -->		                    
</#macro>

<@display_page/>