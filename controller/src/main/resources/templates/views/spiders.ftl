<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Spiders</title>
</#macro>

<#macro content_header>
		<h1>
            Spiders
            <small>how the job is done</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-tasks"></i> Home</a></li>
            <li class="active">Spiders</li>
          </ol>
</#macro>

<#macro page_body>
	<div class="row">
		<div class="col-md-12">
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
                    <#list spiders.content as spider>
                		<tr>
	                      <td><a href="/spiders/${spider.id?c}">${spider.id?c}</a></td>
	                      <td><a href="/spiders/${spider.id?c}">${spider.name}</a></td>
	                      <td>${spider.created}</td>
	                      <#switch spider.status>
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
	                      <td><span class="label label-${label}">${spider.status}</span></td>
	                      <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
	                    </tr>
				    </#list>
                  </tbody></table>
                </div>
                <#if spiders.totalElements < 1>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>There is not running spider!</h4>
                    <p>It is time to work now, create a new spider.</p>
                    <button type="button" class="btn btn-outline" data-toggle="modal" data-target="#add-spider"><i class="fa fa-plus"></i> Create one!</button>
                  </div>
                </div><!-- /.box-body -->
                </#if>
                <#if spiders.totalElements gt 0>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>Add spider!</h4>
                    <p>Create a new spider.</p>
                    <button type="button" class="btn btn-outline" data-toggle="modal" data-target="#add-spider"><i class="fa fa-plus"></i> Create one!</button>
                  </div>
                </div><!-- /.box-body -->
                </#if>
              </div>
		</div>
	</div>
</#macro>

<@display_page/>