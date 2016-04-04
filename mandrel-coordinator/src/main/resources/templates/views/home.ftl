<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Home</title>
</#macro>

<#macro content_header>
		<h1>
            Home
            <small>it all starts here</small>
          </h1>
          <ol class="breadcrumb">
            <li class="active"><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
          </ol>
</#macro>

<#macro js>
	  <script src="/webjars/sockjs-client/0.3.4-1/sockjs.min.js"></script>
      <script src="/webjars/stomp-websocket/2.3.1-1/stomp.min.js"></script>
      <script>
		var path = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1);
		var sock = new SockJS(path + '../sockjs');
		var stompClient = Stomp.over(sock);

		stompClient.connect({}, function(frame) {
			stompClient.subscribe("/topic/global", function(msg) {
				var timeline = document.getElementById('timeline');
				var element = timeline.getElementsByTagName('li')[0];
				element.insertAdjacentHTML('afterend', msg.body);
			});
		});		
		</script>
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
	          <span class="info-box-text">Jobs completed</span>
	          <span class="info-box-number">0</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	  </div>
	  
	<div class="row">
		<div class="col-md-6">
			<div class="box">
                <div class="box-header">
                  <h3 class="box-title">Nodes</h3>
                </div><!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                  <table class="table table-hover">
                    <tbody><tr>
                      <th>ID</th>
                      <th>Type</th>
                      <th>Hostname</th>
                      <th>FQDN</th>
                      <th>Status</th>
                      <th>Memory</th>
                    </tr>
                    <#list nodes?keys as key> 
						<#assign node = nodes?values[key_index]>
						<tr>
	                      <td><a href="/nodes/${node.id}">${node.id}</a></td>
	                      <td>${node.type!}</td>
	                      <td>${node.infos.hostname}</td>
	                      <td>${node.infos.fqdn}</td>
	                      <td><span class="label label-success">OK</span></td>
	                      <td>${node.infos.jvmInfo.mem.heapInit.printableValue}/${node.infos.jvmInfo.mem.heapMax.printableValue} (${node.infos.jvmInfo.mem.nonHeapInit.printableValue}/${node.infos.jvmInfo.mem.nonHeapMax.printableValue})</td>
	                    </tr>
				    </#list>
                  </tbody></table>
                </div><!-- /.box-body -->
                <div class="box-footer clearfix">
                  <a class="btn btn-sm btn-default btn-flat pull-right" href="/nodes">View All</a>
                </div>
              </div>
		</div>
		<div class="col-md-6">
			<div class="box">
                <div class="box-header">
                  <h3 class="box-title">Jobs</h3>
                </div><!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                  <table class="table table-hover">
                    <tbody><tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Added</th>
                      <th>Started</th>
                      <th>Status</th>
                      <th>Description</th>
                    </tr>
                    <#list jobs as job>
                		<tr>
	                      <td><a href="/jobs/${job.id?c}">#${job.id?c}</a></td>
	                      <td><a href="/jobs/${job.id?c}">${job.name}</a></td>
	                      <td>${job.created}</td>
	                      <td>${(job.started)!"not started"}</td>
	                      <#switch job.status>
								<#case "created">
									<#assign label = "primary">
									<#break>
								<#case "started">
									<#assign label = "info">
									<#break>
								<#case "paused">
									<#assign label = "warning">
									<#break>
								<#case "ended">
									<#assign label = "success">
		 							<#break>
		 						<#case "killed">
		 							<#assign label = "danger">
		 							<#break>
								<#default>
									<#assign label = "warning">
									<#break>
							</#switch>
	                      <td><span class="label label-${label}">${job.status}</span></td>
	                      <td>Sources:
	                      <#if job.sources??>
	                      <#list job.sources as source>
	                      	${source.name()} <#sep>/</#sep>
	                      </#list>
	                      </#if>
	                      </td>
	                    </tr>
				    </#list>
                  </tbody>
                  </table>
                </div>
                <#if jobs?size < 1>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>There is no running job!</h4>
                    <p>It is time to work now, create a new job.</p>
                    <button type="button" class="btn btn-outline" data-toggle="modal" data-target="#add-job"><i class="fa fa-plus"></i> Create one!</button>
                  </div>
                </div><!-- /.box-body -->
                </#if>
                <div class="box-footer clearfix">
                  <a class="btn btn-sm btn-default btn-flat pull-right" href="/jobs">View All</a>
                </div>
              </div>
		</div>
	</div>
          
      <div class="row">
        <div class="col-md-12">
          <ul class="timeline" id="timeline">
			  <#list events?keys as key>
			  	<li class="time-label" id="time-label-${key}">
                  <span class="bg-gray">
                   ${key}
                  </span>
                </li>
				<#assign eventsForData = events?values[key_index]>
				<#list eventsForData as event>
					<#include "event.ftl">
	          	</#list>
			  </#list>
            <li id="timeline-end">
              <i class="fa fa-clock-o bg-gray"></i>
            </li>
          </ul>
        </div><!-- /.col -->
      </div>
      
</#macro>

<@display_page/>