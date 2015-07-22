						<#if event??>
						<li>
						<#assign icon = "fa-rotate-left">
						<#assign color = "bg-green">
						<#assign title = event.title!"">
						<#assign footer = "">
						<#assign text = event.text!"">
						<#if event.type??>
							<#switch event.type>
								<#case "NODE_STARTED">
									<#assign icon = "fa-laptop">
									<#assign color = "bg-green">
									<#assign title = '<a href="/nodes/${event.nodeId}">${event.nodeId}</a> successfully joined the cluster'>
									<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" href="/nodes/${event.nodeId}">View node</a>'>
									<#break>
								<#case "NODE_STOPPED">
									<#assign icon = "fa-laptop">
									<#assign color = "bg-red">
									<#assign title = '<a href="/nodes/${event.nodeId}">${event.nodeId}</a> successfully joined the cluster'>
									<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" href="/nodes/${event.nodeId}">View node</a>'>
									<#break>
								<#case "SPIDER_NEW">
									<#assign icon = "fa-tasks">
		 							<#break>
		 						<#case "SPIDER_STARTED">
		 							<#assign icon = "fa-tasks">
		 							<#break>
		 						<#case "SPIDER_ENDED">
		 							<#assign icon = "fa-tasks">
		 							<#break>
		 						<#case "SPIDER_CANCELLED">
		 							<#assign icon = "fa-tasks">
		 							<#break>
								<#default>
									<#break>
							</#switch>
						</#if>
						  <i class="fa ${icon} ${color}"></i>
		                  <div class="timeline-item">
		                    <span class="time"><i class="fa fa-clock-o"></i> ${event.time}</span>
		                    <h3 class="timeline-header">${title}</h3>
		                    <div class="timeline-body">
		                      ${text}
		                    </div>
		                    <div class="timeline-footer">
		                      ${footer}
		                    </div>
		                  </div>
						</li>
						</#if>