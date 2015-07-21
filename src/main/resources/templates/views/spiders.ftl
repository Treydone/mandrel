<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Spiders</title>
</#macro>

<#macro page_body>

	<#list spiders as spider>
    	<tr>
        	<td><a href="/spider/${spider.id}"></a>${spider.id}</td> <td>${spider.name}</td> <td>${spider.status}</td> 
		</tr>
    </#list>

</#macro>

<@display_page/>