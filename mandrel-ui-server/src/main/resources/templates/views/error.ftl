<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Error</title>
  <style>
  	.error-page {
	  width: 1200px;
	  margin: 20px auto 0 auto;
	}
  </style>
</#macro>

<#macro content_header>
		<h1>
            Error!
            <small>ouch...</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active"><a href="/"><i class="fa fa-dashboard"></i> 500</a></li>
          </ol>
</#macro>

<#macro page_body>	
		
		<div class="error-page">
			<#assign color="red">
            <h2 class="headline text-${color}">500</h2>
            <div class="error-content">
              <h3><i class="fa fa-warning text-${color}"></i> Oops! Something went wrong.</h3>
              <p>
                We will work on fixing that right away.
                Meanwhile, you may <a href="../../index.html">return to dashboard</a> or try using the search form.
              </p>
              <dl>
                    <dt>Timestamp</dt>
                    <dd>${timestamp?datetime}</dd>
                    <dt>Path</dt>
                    <dd>${path!""}</dd>
                    <dt>Status</dt>
                    <dd>${status}</dd>
                    <dt>Errors</dt>
                    <dd>${error!""}<br />${errors!""}</dd>
                    <dt>Exception</dt>
                    <dd>${exception!""}</dd>
                    <dt>Message</dt>
                    <dd>${message}</dd>
                    <dt>Trace</dt>
                    <dd>${trace!""}</dd>
              	</dl>
            </div>
          </div>
</#macro>

<@display_page/>