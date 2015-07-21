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
              <!-- The time line -->
              <ul class="timeline">
                <!-- timeline time label -->
                <li class="time-label">
                  <span class="bg-gray">
                    10 Feb. 2014
                  </span>
                </li>
                <!-- /.timeline-label -->
                <!-- timeline item -->
                <li>
                  <i class="fa fa-tasks bg-red"></i>
                  <div class="timeline-item">
                    <span class="time"><i class="fa fa-clock-o"></i> 12:05</span>
                    <h3 class="timeline-header no-border"><a href="#">IMDB - test</a> has been cancelled</h3>
                    <div class="timeline-body">
                      Etsy doostang zoodles disqus groupon greplin oooj voxy zoodles,
                      weebly ning heekya handango imeem plugg dopplr jibjab, movity
                      jajah plickers sifteo edmodo ifttt zimbra. Babblely odeo kaboodle
                      quora plaxo ideeli hulu weebly balihoo...
                    </div>
                    <div class="timeline-footer">
                      <a class="btn btn-primary btn-xs">Read more</a>
                      <a class="btn btn-danger btn-xs">Delete</a>
                    </div>
                  </div>
                </li>
                <!-- END timeline item -->
                <!-- timeline item -->
                <li>
                  <i class="fa fa-tasks bg-aqua"></i>
                  <div class="timeline-item">
                    <span class="time"><i class="fa fa-clock-o"></i> 5 mins ago</span>
                    <h3 class="timeline-header no-border"><a href="#">IMDB - test</a> added to spiders</h3>
                  </div>
                </li>
                <!-- END timeline item -->
                <!-- timeline item -->
                <li>
                  <i class="fa fa-laptop bg-green"></i>
                  <div class="timeline-item">
                    <span class="time"><i class="fa fa-clock-o"></i> 27 mins ago</span>
                    <h3 class="timeline-header"><a href="#">Jay White</a> successfully joined the cluster</h3>
                    <div class="timeline-body">
                      Take me to your leader!
                      Switzerland is small and neutral!
                      We are more like Germany, ambitious and misunderstood!
                    </div>
                    <div class="timeline-footer">
                      <a class="btn btn-warning btn-flat btn-xs">View comment</a>
                    </div>
                  </div>
                </li>
                <!-- END timeline item -->
                <!-- timeline time label -->
                <li class="time-label">
                  <span class="bg-gray">
                    3 Jan. 2014
                  </span>
                </li>
                <!-- /.timeline-label -->
                <!-- timeline item -->
                <li>
                  <i class="fa fa-laptop bg-green"></i>
                  <div class="timeline-item">
                    <span class="time"><i class="fa fa-clock-o"></i> 27 mins ago</span>
                    <h3 class="timeline-header"><a href="#">Jay White</a> successfully joined the cluster</h3>
                    <div class="timeline-body">
                      Take me to your leader!
                      Switzerland is small and neutral!
                      We are more like Germany, ambitious and misunderstood!
                    </div>
                    <div class="timeline-footer">
                      <a class="btn btn-warning btn-flat btn-xs">View comment</a>
                    </div>
                  </div>
                </li>
                <!-- END timeline item -->
                <li>
                  <i class="fa fa-clock-o bg-gray"></i>
                </li>
              </ul>
            </div><!-- /.col -->
          </div>

</#macro>

<@display_page/>