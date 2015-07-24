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
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Console</li>
          </ol>
</#macro>

<#macro page_body>
		<div class="row">
            <div class="col-xs-12">
              <div class="box">
                <div class="box-header">
                  <h3 class="box-title">Entries</h3>
                </div><!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                  <table class="table table-hover">
                    <tbody><tr id="tail">
                      <th>Date</th>
                      <th>Level</th>
                      <th>Thread</th>
                      <th>Logger</th>
                      <th>Message</th>
                    </tr>
                  </tbody></table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
            </div>
          </div>	
		<script src="/webjars/sockjs-client/0.3.4-1/sockjs.min.js"></script>
      	<script src="/webjars/stomp-websocket/2.3.1-1/stomp.min.js"></script>

		<script>
		var path = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1);
		var sock = new SockJS(path + '../sockjs');
		var stompClient = Stomp.over(sock);

		stompClient.connect({}, function(frame) {
			stompClient.subscribe("/topic/tail", function(msg) {
				var tail = document.getElementById('tail');
				tail.insertAdjacentHTML('afterend', msg.body);
			});
		});		
		</script>
				                    
</#macro>

<@display_page/>
