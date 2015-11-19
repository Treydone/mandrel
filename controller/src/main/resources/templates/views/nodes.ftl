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
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Nodes</li>
          </ol>
</#macro>

<#macro page_body>

          <div class="row">
	          <div class="col-md-12">
				<div class="box">
	                <div class="box-header">
	                  <h3 class="box-title">Nodes</h3>
	                </div><!-- /.box-header -->
	                <div class="box-body table-responsive no-padding">
	                  <table class="table table-hover">
	                    <tbody><tr>
	                      <th>URI</th>
	                      <th>Type</th>
	                      <th>Version</th>
	                      <th>Hostname</th>
	                      <th>FQDN</th>
	                      <th>Status</th>
	                      <th>Started</th>
	                      <th>Java</th>
	                      <th>Memory</th>
	                    </tr>
	                    <#list nodes?keys as key> 
							<#assign node = nodes?values[key_index]>
							<tr>
		                      <td><a href="/nodes/${node.id}">${node.id}</a></td>
		                      <td>${node.type!}</td>
		                      <td>${node.version}</td>
		                      <td>${node.infos.hostname}</td>
		                      <td>${node.infos.fqdn}</td>
		                      <td><span class="label label-success">OK</span></td>
		                      <td>${node.infos.jvmInfo.startTime}</td>
		                      <td>${node.infos.jvmInfo.vmName} <b>${node.infos.jvmInfo.version}</b> ${node.infos.jvmInfo.vmVersion} from ${node.infos.jvmInfo.vmVendor}</td>
		                      <td>${node.infos.jvmInfo.mem.heapInit.printableValue}/${node.infos.jvmInfo.mem.heapMax.printableValue} (${node.infos.jvmInfo.mem.nonHeapInit.printableValue}/${node.infos.jvmInfo.mem.nonHeapMax.printableValue})</td>
		                    </tr>
					    </#list>
	                  </tbody></table>
	                </div><!-- /.box-body -->
	              </div>
			</div>
		</div>
    
</#macro>

<@display_page/>