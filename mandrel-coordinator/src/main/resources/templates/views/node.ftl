<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Node</title>
</#macro>

<#macro content_header>
		<h1>
            Nodes
            <small>where the job is done</small> <span class="label label-success"> Ok</span>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/nodes"><i class="fa fa-laptop"></i> Nodes</a></li>
            <li class="active">${node.id}</li>
          </ol>
</#macro>

<#macro page_body>
	
	<div class="row">
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-signal"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Bandwidth</span>
	          <span class="info-box-number">?/s</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-download"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total size</span>
	          <span class="info-box-number">${printBytesSize(metrics.totalSizeTotal)}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-files-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total pages</span>
	          <span class="info-box-number">${metrics.nbPagesTotal}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-star-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Timeout</span>
	          <span class="info-box-number">0</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	  </div>
	  
	  <div class="row">
        <div class="col-md-6">
          <div class="box box-solid">
            <div class="box-header with-border">
              <i class="fa fa-text-width"></i>
              <h3 class="box-title">Configuration</h3>
            </div><!-- /.box-header -->
            <div class="box-body">
            	<dl class="dl-horizontal">
                    <dt>Id</dt>
                    <dd><a href="/nodes/${node.id}">${node.id}</a></dd>
                    <dt>Hostname</dt>
			        <dd>${node.infos.hostname}</dd>
			        <dt>Fqdn</dt>
			        <dd>${node.infos.fqdn}</dd>
			        <dt>Start time</dt>
			        <dd>${node.infos.jvmInfo.startTime}</dd>
			        <dt>JVM</dt>
			        <dd>${node.infos.jvmInfo.vmName} <b>${node.infos.jvmInfo.version}</b> ${node.infos.jvmInfo.vmVersion} from ${node.infos.jvmInfo.vmVendor}</dd>
			        <dt>Heap</dt>
			        <dd>${node.infos.jvmInfo.mem.heapInit.printableValue}/${node.infos.jvmInfo.mem.heapMax.printableValue} (${node.infos.jvmInfo.mem.nonHeapInit.printableValue}/${node.infos.jvmInfo.mem.nonHeapMax.printableValue})</li>
			        <dt>Network interfaces</dt>
			        <dd><ul><#list node.infos.interfaces as interface>
			         <li>${interface.name} (${interface.type}): ${interface.address}</li>
			         </#list></ul></dd>
                </dl>
            </div><!-- /.box-body -->
          </div><!-- /.box -->
        </div><!-- ./col -->
        <div class="col-md-6">
          <div class="box">
            <div class="box-header">
              <h3 class="box-title">Containers</h3>
            </div>
            <div class="box-body table-responsive no-padding">
              <table class="table table-hover">
                <tbody><tr>
                  <th>Job</th>
                  <th>Version</th>
                  <th>Status</th>
                  <th>Type</th>
                </tr>
                <#list containersByType?keys as key> 
					<#assign containers = containersByType?values[key_index]>
                	<#list containers as container> 
			         <tr>
                      <td><a href="/jobs/${container.jobId?c}">${container.jobId?c}</a></td>
                      <td>${container.version?c}</td>
                      <td>${container.status}</td>
                      <td>${key}</td>
                     </tr>
                    </#list>
			    </#list>
              </tbody></table>
            </div>
          </div>
        </div><!-- ./col -->
      </div>
		                    
</#macro>

<@display_page/>