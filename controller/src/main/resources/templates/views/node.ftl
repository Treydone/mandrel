<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Node</title>
</#macro>

<#macro content_header>
		<h1>
            Nodes
            <small>where the job is done</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/nodes"><i class="fa fa-laptop"></i> Nodes</a></li>
            <li class="active">${node.uri}</li>
          </ol>
</#macro>

<#macro page_body>
	
	<div class="row">
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-signal"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Bandwidth</span>
	          <span class="info-box-number">${printBytesSize(metrics.totalSize / (clusterTime - now * 1000))}/s</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-green"><i class="fa fa-download"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total size</span>
	          <span class="info-box-number">${printBytesSize(metrics.totalSize)}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-yellow"><i class="fa fa-files-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total pages</span>
	          <span class="info-box-number">${metrics.nbPages}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-red"><i class="fa fa-star-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Timeout</span>
	          <span class="info-box-number">${metrics.connectTimeout + metrics.readTimeout}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	  </div>
	  
	  <div class="row">
        <div class="col-md-6">
          <div class="box box-solid">
            <div class="box-header with-border">
              <i class="fa fa-text-width"></i>
              <h3 class="box-title">Headlines</h3>
            </div><!-- /.box-header -->
            <div class="box-body">
	            <ul>
			      <li><a href="/nodes/${node.uri}">${node.uri}</a></li>
			      <li>${node.infos.hostname}</li>
			      <li>${node.infos.fqdn}</li>
			      <li><span class="label label-success">OK</span></li>
			      <li>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</li>
			      <li>${node.infos.jvmInfo.startTime}</li>
			      <li>${node.infos.jvmInfo.vmName} <b>${node.infos.jvmInfo.version}</b> ${node.infos.jvmInfo.vmVersion} from ${node.infos.jvmInfo.vmVendor}</li>
			      <li>${node.infos.jvmInfo.mem.heapInit.printableValue}/${node.infos.jvmInfo.mem.heapMax.printableValue} (${node.infos.jvmInfo.mem.nonHeapInit.printableValue}/${node.infos.jvmInfo.mem.nonHeapMax.printableValue})</li>
			    </ul>
            </div><!-- /.box-body -->
          </div><!-- /.box -->
        </div><!-- ./col -->
        <div class="col-md-6">
          <div class="box box-solid">
            <div class="box-header with-border">
              <i class="fa fa-text-width"></i>
              <h3 class="box-title">Text Emphasis</h3>
            </div><!-- /.box-header -->
            <div class="box-body">
            	${networkSettings.group.name}
            	${networkSettings.group.password}
              <p class="lead">Lead to emphasize importance</p>
              <p class="text-green">Text green to emphasize success</p>
              <p class="text-aqua">Text aqua to emphasize info</p>
              <p class="text-light-blue">Text light blue to emphasize info (2)</p>
              <p class="text-red">Text red to emphasize danger</p>
              <p class="text-yellow">Text yellow to emphasize warning</p>
              <p class="text-muted">Text muted to emphasize general</p>
            </div><!-- /.box-body -->
          </div><!-- /.box -->
        </div><!-- ./col -->
      </div>
		                    
</#macro>

<@display_page/>