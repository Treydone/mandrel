<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Nodes</title>
</#macro>

<#macro content_header>
		<h1>
            Nodes
            <small>where the job is done</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Home</li>
          </ol>
</#macro>

<#macro page_body>

		<div class="row">
            <div class="col-lg-3 col-xs-6">
              <!-- small box -->
              <div class="small-box bg-aqua">
                <div class="inner">
                  <h3>150</h3>
                  <p>Installed</p>
                </div>
                <div class="icon">
                  <i class="ion ion-bag"></i>
                </div>
              </div>
            </div><!-- ./col -->
            <div class="col-lg-3 col-xs-6">
              <!-- small box -->
              <div class="small-box bg-green">
                <div class="inner">
                  <h3>53<sup style="font-size: 20px">%</sup></h3>
                  <p>Running</p>
                </div>
                <div class="icon">
                  <i class="ion ion-stats-bars"></i>
                </div>
              </div>
            </div><!-- ./col -->
            <div class="col-lg-3 col-xs-6">
              <!-- small box -->
              <div class="small-box bg-yellow">
                <div class="inner">
                  <h3>44</h3>
                  <p>Starting</p>
                </div>
                <div class="icon">
                  <i class="ion ion-person-add"></i>
                </div>
              </div>
            </div><!-- ./col -->
            <div class="col-lg-3 col-xs-6">
              <!-- small box -->
              <div class="small-box bg-red">
                <div class="inner">
                  <h3>65</h3>
                  <p>In errors</p>
                </div>
                <div class="icon">
                  <i class="ion ion-pie-graph"></i>
                </div>
              </div>
            </div><!-- ./col -->
          </div>
          
          <div class="row">
	          <div class="col-md-12">
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
	                      <th>Started</th>
	                      <th>Java</th>
	                      <th>Memory</th>
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
		                      <td>${node.infos.jvmInfo.startTime}</td>
		                      <td>${node.infos.jvmInfo.vmName} <b>${node.infos.jvmInfo.version}</b> ${node.infos.jvmInfo.vmVersion} from ${node.infos.jvmInfo.vmVendor}</td>
		                      <td>${node.infos.jvmInfo.mem.heapInit}/${node.infos.jvmInfo.mem.heapMax} (${node.infos.jvmInfo.mem.nonHeapInit}/${node.infos.jvmInfo.mem.nonHeapMax})</td>
		                    </tr>
					    </#list>
	                  </tbody></table>
	                </div><!-- /.box-body -->
	              </div>
			</div>
		</div>
    
</#macro>

<@display_page/>