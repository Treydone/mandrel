<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Data</title>
</#macro>

<#macro content_header>
		<h1>
            Data
            <small>YOUR data</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Data</li>
          </ol>
</#macro>

<#macro page_body>
	<div class="row">
		<div class="col-md-12">
			<div class="box">
                <div class="box-header">
                  <h3 class="box-title">Extractors</h3>
                </div><!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                  <table class="table table-hover">
                    <tbody><tr>
                      <th>Job</th>
                      <th>Added</th>
                      <th>State</th>
                      <th>Extractor</th>
                    </tr>
                    <#list jobs.content as job>
                    	<#list job.extractors.data as extractor>
                		<tr>
	                      <td><a href="/jobs/${job.id?c}">${job.name}</a></td>
	                      <td>${job.created}</td>
	                      <#switch job.status>
								<#case "CREATED">
									<#assign label = "primary">
									<#break>
								<#case "STARTED">
									<#assign label = "info">
									<#break>
								<#case "ENDED">
									<#assign label = "success">
		 							<#break>
		 						<#case "KILLED">
		 							<#assign label = "danger">
		 							<#break>
								<#default>
									<#assign label = "warning">
									<#break>
							</#switch>
	                      <td><span class="label label-${label}">${job.status}</span></td>
	                      <td><a href="/jobs/${job.id?c}/data/${extractor.getName()}">${extractor.getName()}</a></td>
	                    </tr>
	                    </#list>
				    </#list>
                  </tbody></table>
                </div>
              </div>
		</div>
	</div>
</#macro>

<@display_page/>