<!DOCTYPE html>

<html>
	<head>
		<title>Spring Boot log viewer</title>
		
		<script src="webjars/sockjs-client/0.3.4-1/sockjs.min.js"></script>
        <script src="webjars/stomp-websocket/2.3.1-1/stomp.min.js"></script>
		
	</head>
	<body>
		<noscript><h2>Enable Java script and reload this page to run Websocket Demo</h2></noscript>
		<h1>Spring Boot log viewer</h1>
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
	</body>
</html> 