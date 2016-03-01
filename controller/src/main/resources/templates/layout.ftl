<#include "./functions.ftl">

<#macro common_page_head>
  <meta charset="utf-8">
</#macro>

<#macro page_head>
  <@common_page_head/>
  <title>Mandrel | Dashboard</title>
</#macro>

<#macro page_body>

</#macro>

<#macro content_header>
		<h1>
            Blank page
            <small>it all starts here</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="#">Blank</a></li>
            <li class="active">Blank page</li>
          </ol>
</#macro>

<#macro js>

</#macro>

<#macro css>

</#macro>

<#macro aftercss>

</#macro>

<#macro display_page>
<!DOCTYPE html>
<html>
  <head>
    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    
    <link href="/public/bundles/common.min.css" rel="stylesheet" type="text/css" />
    
    <@css/>
    <link href="/public/css/AdminLTE.min.css" rel="stylesheet" type="text/css" />
    <link href="/public/css/_all-skins.min.css" rel="stylesheet" type="text/css" />
    
    <!-- <link href="/public/css/blue.css" rel="stylesheet" type="text/css" />
    <link href="/public/css/morris.css" rel="stylesheet" type="text/css" />
    <link href="/webjars/bootstrap-3-datepicker/1.4.0/dist/css/bootstrap-datepicker.min.css" rel="stylesheet" type="text/css" />
    <link href="/webjars/bootstrap-daterangepicker/1.3.22/daterangepicker-bs3.css" rel="stylesheet" type="text/css" /> -->

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <@page_head/>
  </head>
  <body class="sidebar-mini skin-black-light">
  
    <div class="modal" id="add-spider">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">Add a new spider</h4>
          </div>
          <div class="modal-body">
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
                <a href="/webjars/swagger-ui/2.1.8-M1/index.html?url=/api-docs"><button type="button" class="btn btn-outline"><i class="fa fa-plus"></i> Create one!</button></a>
              </div>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
          
          
    <!-- Site wrapper -->
    <div class="wrapper">

      <header class="main-header">
        <!-- Logo -->
        <a href="../../index2.html" class="logo">
          <!-- mini logo for sidebar mini 50x50 pixels -->
          <span class="logo-mini"><i class="fa fa-skyatlas"></i></span>
          <!-- logo for regular state and mobile devices -->
          <span class="logo-lg"><b><i class="fa fa-skyatlas"></i> Mandrel</b></span>
        </a>
        <!-- Header Navbar: style can be found in header.less -->
        <nav class="navbar navbar-static-top" role="navigation">
          <!-- Sidebar toggle button-->
          <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
	      <div id="mainbar" style="margin-left: 47px;"></div>
        </nav>
      </header>

      <!-- =============================================== -->

      <!-- Left side column. contains the sidebar -->
      <aside class="main-sidebar">
        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">
          <!-- Sidebar user panel -->
          <div class="user-panel">
            <div class="pull-left info">
              <p>Cluster: Mandrel</p>
              <a href="#"><i class="fa fa-circle text-success"></i> Online</a>
            </div>
          </div>
          <!-- sidebar menu: : style can be found in sidebar.less -->
          <ul class="sidebar-menu">
            <li class="header">MAIN NAVIGATION</li>
            <li>
              <a href="/">
                <i class="fa fa-dashboard"></i> <span>Home</span>
              </a>
            </li>
            <li>
              <a href="/nodes">
                <i class="fa fa-laptop"></i> <span>Nodes</span>
              </a>
            </li>
            <li>
              <a href="/spiders">
                <i class="fa fa-tasks"></i> <span>Spiders</span>
              </a>
            </li>
            <li>
              <a href="/data">
                <i class="fa fa-database"></i> <span>Data</span>
              </a>
            </li>
            <li>
              <a href="/metrics">
                <i class="fa fa fa-pie-chart"></i> <span>Metrics</span>
              </a>
            </li>
            <li>
              <a href="/console">
                <i class="fa fa-terminal"></i> <span>Console</span>
              </a>
            </li>
            <li>
              <a href="/webjars/swagger-ui/2.1.8-M1/index.html?url=/api-docs">
                <i class="fa fa-cloud"></i> <span>API</span>
              </a>
            </li>
          </ul>
        </section>
        <!-- /.sidebar -->
      </aside>

      <!-- =============================================== -->

      <!-- Content Wrapper. Contains page content -->
      <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
        	<@content_header/>
        </section>

        <!-- Main content -->
        <section class="content">

		<@page_body/>

        </section><!-- /.content -->
      </div><!-- /.content-wrapper -->

      <footer class="main-footer">
        <div class="pull-right hidden-xs">
          <b>Version</b> ${infoSettings.version}
        </div>
        <strong><a href="http://github.com/Treydone/mandrel">Mandrel</a></strong> (from ${infoSettings.commit.originUrl}/${infoSettings.commit.branch} - ${infoSettings.commit.id} at ${infoSettings.commit.time})
      </footer>

    </div><!-- ./wrapper -->
    
    <script src="/public/bundles/common.min.js" type="text/javascript"></script>
    
    <script>
    var throughput = [<#if (throughput)??><#list throughput as item>${item.value}<#sep>,</#sep></#list></#if>];
    	$('#mainbar').sparkline(throughput, { 
    			type: 'bar',
    			barColor: '#000',
    			height: '50',
    			width: '800px',
    		 });
    </script>
    <@js/>
    <script src="/public/js/app.min.js" type="text/javascript"></script>
  </body>
</html>
</#macro>