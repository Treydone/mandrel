<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Jobs</title>
</#macro>

<#macro content_header>
		<h1>
            Jobs
            <small>how the job is done</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-tasks"></i> Home</a></li>
            <li class="active">Jobs</li>
          </ol>
</#macro>

<#macro page_body>
	<div class="row">
		<div class="col-md-12">
			<div class="box">
                <div class="box-header">
                  <h3 class="box-title">Jobs</h3>
                </div><!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                  <table class="table table-hover">
                    <tbody><tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Date</th>
                      <th>Status</th>
                      <th>Description</th>
                    </tr>
                    <#list jobs.content as job>
                		<tr>
	                      <td><a href="/jobs/${job.id?c}">${job.id?c}</a></td>
	                      <td><a href="/jobs/${job.id?c}">${job.name}</a></td>
	                      <td>${job.created}</td>
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
                  </tbody></table>
                </div>
                <#if jobs.total < 1>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>There is not running job!</h4>
                    <p>It is time to work now, create a new job.</p>
                    <button type="button" class="btn btn-outline" data-toggle="modal" data-target="#add-job"><i class="fa fa-plus"></i> Create one!</button>
                  </div>
                </div><!-- /.box-body -->
                </#if>
                <#if jobs.total gt 0>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>Add job!</h4>
                    <p>Create a new job.</p>
                    <button type="button" class="btn btn-outline" data-toggle="modal" data-target="#add-job"><i class="fa fa-plus"></i> Create one!</button>
                  </div>
                </div><!-- /.box-body -->
                </#if>
              </div>
		</div>
	</div>
</#macro>

<@display_page/>