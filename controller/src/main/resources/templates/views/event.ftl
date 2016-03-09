						<#if event??>
						<li>
						<#assign icon = "fa-rotate-left">
						<#assign color = "bg-green">
						<#assign title = event.title!"">
						<#assign footer = "">
						<#assign text = event.text!"">
						<#if event.type??>
							<#switch event.type>
								<#case "NODE">
									<#if (event.node??) && (event.node.type??)>
										<#switch event.node.type>
											<#case "NODE_STARTED">
												<#assign icon = "fa-laptop">
												<#assign color = "bg-green">
												<#assign title = '<a href="/nodes/${event.node.nodeId}">${event.node.nodeId}</a> successfully joined the cluster'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" href="/nodes/${event.node.nodeId}">View node</a>'>
												<#break>
											<#case "NODE_STOPPED">
												<#assign icon = "fa-laptop">
												<#assign color = "bg-red">
												<#assign title = '<a href="/nodes/${event.node.nodeId}">${event.node.nodeId}</a> just leaved the cluster'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" href="/nodes/${event.node.nodeId}">View node</a>'>
												<#break>
											<#default>
												<#break>
										</#switch>
									</#if>
								<#case "SPIDER">
									<#if (event.spider??) && (event.spider.type??)>
										<#switch event.spider.type>
											<#case "SPIDER_CREATED">
					 							<#assign icon = "fa-tasks">
					 							<#assign color = "bg-blue">
					 							<#assign title = '<a href="/spiders/${event.spider.spiderId?c}">${event.spider.spiderName}</a> has been added to the spiders'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" "/spiders/${event.spider.spiderId}">View spider</a>'>
					 							<#break>
					 						<#case "SPIDER_STARTED">
					 							<#assign icon = "fa-tasks">
					 							<#assign title = '<a href="/spiders/${event.spider.spiderId?c}">${event.spider.spiderName}</a> has been started'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" "/spiders/${event.spider.spiderId}">View spider</a>'>
					 							<#break>
					 						<#case "SPIDER_PAUSED">
					 							<#assign icon = "fa-tasks">
					 							<#assign color = "bg-orange">
					 							<#assign title = '<a href="/spiders/${event.spider.spiderId?c}">${event.spider.spiderName}</a> has been paused'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" "/spiders/${event.spider.spiderId}">View spider</a>'>
					 							<#break>
					 						<#case "SPIDER_ENDED">
					 							<#assign icon = "fa-tasks">
					 							<#assign title = '<a href="/spiders/${event.spider.spiderId?c}">${event.spider.spiderName}</a> just ended!'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" "/spiders/${event.spider.spiderId}">View spider</a>'>
					 							<#break>
					 						<#case "SPIDER_KILLED">
					 							<#assign icon = "fa-tasks">
					 							<#assign color = "bg-red">
					 							<#assign title = '<a href="/spiders/${event.spider.spiderId?c}">${event.spider.spiderName}</a> has been killed'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" "/spiders/${event.spider.spiderId}">View spider</a>'>
					 							<#break>
					 						<#case "SPIDER_DELETED">
					 							<#assign icon = "fa-tasks">
					 							<#assign color = "bg-orange">
					 							<#assign title = '<a href="/spiders/${event.spider.spiderId?c}">${event.spider.spiderName}</a> has been deleted'>
												<#assign footer = '<a class="btn btn-warning btn-flat btn-xs" "/spiders/${event.spider.spiderId}">View spider</a>'>
					 							<#break>
					 						<#default>
												<#break>
										</#switch>
									</#if>
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