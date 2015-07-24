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
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
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
                    <#list spiders as spider>
                		<tr>
	                      <td><a href="/spiders/${spider.id}">${spider.id}</a></td>
	                      <td>${spider.name}</td>
	                      <td>11-7-2014</td>
	                      <#switch spider.state>
								<#case "NEW">
									<#assign label = "primary">
									<#break>
								<#case "STARTED">
									<#assign label = "info">
									<#break>
								<#case "ENDED">
									<#assign label = "success">
		 							<#break>
		 						<#case "CANCELLED">
		 							<#assign label = "danger">
		 							<#break>
								<#default>
									<#assign label = "warning">
									<#break>
							</#switch>
	                      <td><span class="label label-${label}">${spider.state}</span></td>
	                      <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
	                    </tr>
				    </#list>
                  </tbody></table>
                </div>
                <#if spiders?size < 1>
                <div class="box-body">
                  <div class="callout callout-success">
                    <h4>There is not running spider!</h4>
                    <p>It is time to work now, create a new spider.</p>
                    <a href="/spiders/add"><button type="button" class="btn btn-outline"><i class="fa fa-plus"></i> Create one!</button></a>
                  </div>
                </div><!-- /.box-body -->
                </#if>
              </div>
		</div>
	</div>
</#macro>

<@display_page/>