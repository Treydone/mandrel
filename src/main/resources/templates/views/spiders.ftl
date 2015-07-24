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

	<#list spiders as spider>
    	<tr>
        	<td><a href="/spider/${spider.id}"></a>${spider.id}</td> <td>${spider.name}</td> <td>${spider.status}</td> 
		</tr>
    </#list>

</#macro>

<@display_page/>