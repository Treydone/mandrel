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
                      <th>Spider</th>
                      <th>Added</th>
                      <th>State</th>
                      <th>Extractor</th>
                    </tr>
                    <#list spiders as spider>
                    	<#list spider.extractors.pages as extractor>
                		<tr>
	                      <td><a href="/spiders/${spider.id}">${spider.name}</a></td>
	                      <td>${spider.added}</td>
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
	                      <td><a href="/spiders/${spider.id}/data/${extractor.name}">${extractor.name}</a></td>
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