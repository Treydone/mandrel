<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Console</title>
</#macro>

<#macro content_header>
		<h1>
            Console
            <small>because shit happened</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="#"><i class="fa fa-dashboard"></i> Console</a></li>
            <li class="active">Console</li>
          </ol>
</#macro>

<#macro page_body>
	
		<script src="/webjars/sockjs-client/0.3.4-1/sockjs.min.js"></script>
      	<script src="/webjars/stomp-websocket/2.3.1-1/stomp.min.js"></script>

		<p id="info"></p>
	  	<p id="tail"></p>
	  	
		<script>
		var path = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1);
		var sock = new SockJS(path + '../sockjs');
		var stompClient = Stomp.over(sock);

		stompClient.connect({}, function(frame) {
			stompClient.subscribe("/topic/tail", function(msg) {
				var tail = document.getElementById('tail');
            	var p = document.createElement('p');
            	p.appendChild(document.createTextNode(msg.body));
            	tail.appendChild(p);
			});
		});		
		</script>
				                    
</#macro>

<@display_page/>
