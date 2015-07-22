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
            <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Home</li>
          </ol>
</#macro>

<#macro page_body>

	<div class="row">
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-signal"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Bandwidth</span>
	          <span class="info-box-number">1,410 kb/s</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-green"><i class="fa fa-download"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total size</span>
	          <span class="info-box-number">410 Mb</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-yellow"><i class="fa fa-files-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total pages</span>
	          <span class="info-box-number">13,648</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-red"><i class="fa fa-star-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Spiders completed</span>
	          <span class="info-box-number">93,139</span>
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
                      <th>UUID</th>
                      <th>Hostname</th>
                      <th>FQDN</th>
                      <th>Status</th>
                      <th>Reason</th>
                    </tr>
                    <#assign keys = nodes?keys>
					<#list keys as key>
						<#assign node = nodes[key]>
						<tr>
	                      <td><a href="/nodes/${node.uuid}">${node.uuid}</a></td>
	                      <td>${node.infos.hostname}</td>
	                      <td>${node.infos.fqdn}</td>
	                      <td><span class="label label-success">OK</span></td>
	                      <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
	                    </tr>
				    </#list>
                  </tbody></table>
                </div><!-- /.box-body -->
              </div>
		</div>
		<div class="col-md-6">
			<div class="box">
                <div class="box-header">
                  <h3 class="box-title">Spiders</h3>
                </div><!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                  <table class="table table-hover">
                    <tbody><tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Date</th>
                      <th>Status</th>
                      <th>Reason</th>
                    </tr>
                    <#list spiders as spider>
                		<tr>
	                      <td><a href="/spider/${spider.id}">${spider.id}</a></td>
	                      <td>${spider.status}</td>
	                      <td>11-7-2014</td>
	                      <td><span class="label label-success">Approved</span></td>
	                      <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
	                    </tr>
				    </#list>
                  </tbody></table>
                </div>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>I am a success callout!</h4>
                    <p>This is a green callout.</p>
                  </div>
                </div><!-- /.box-body -->
              </div>
		</div>
	</div>
          
      <div class="row">
        <div class="col-md-12">
          <ul class="timeline">
			  <#list events?keys as key>
			  	<li class="time-label">
                  <span class="bg-gray">
                   ${key}
                  </span>
                </li>
					<#assign eventsForData = events?values[key_index]>
					<#list eventsForData as event>
						<li>
						<#assign icon = "fa-rotate-left">
						<#assign color = "bg-green">
						<#assign text = "">
						<#assign footer = "">
						<#assign data = "">
						<#switch event.type>
							<#case "NODE_STARTED">
								<#assign icon = "fa-laptop">
								<#assign color = "bg-green">
								<#assign text = '<a href="/nodes/${event.nodeId}">${event.nodeId}</a> successfully joined the cluster'>
								<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" href="/nodes/${event.nodeId}">View node</a>'>
								<#break>
							<#case "NODE_STOPPED">
								<#assign icon = "fa-laptop">
								<#assign color = "bg-red">
								<#assign text = '<a href="/nodes/${event.nodeId}">${event.nodeId}</a> successfully joined the cluster'>
								<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" href="/nodes/${event.nodeId}">View node</a>'>
								<#break>
							<#case "SPIDER_NEW">
								<#assign icon = "fa-tasks">
	 							<#break>
	 						<#case "SPIDER_STARTED">
	 							<#assign icon = "fa-tasks">
	 							<#break>
	 						<#case "SPIDER_ENDED">
	 							<#assign icon = "fa-tasks">
	 							<#break>
	 						<#case "SPIDER_CANCELLED">
	 							<#assign icon = "fa-tasks">
	 							<#break>
							<#default>
								<#break>
						</#switch>
						  <i class="fa ${icon} ${color}"></i>
		                  <div class="timeline-item">
		                    <span class="time"><i class="fa fa-clock-o"></i> ${event.time}</span>
		                    <h3 class="timeline-header">${text}</h3>
		                    <div class="timeline-body">
		                      ${data}
		                    </div>
		                    <div class="timeline-footer">
		                      ${footer}
		                    </div>
		                  </div>
						</li>
		          	</#list>
			  </#list>
            <li>
              <i class="fa fa-clock-o bg-gray"></i>
            </li>
          </ul>
        </div><!-- /.col -->
      </div>

</#macro>

<@display_page/>