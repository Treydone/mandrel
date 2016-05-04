<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Data</title>
</#macro>

<#macro content_header>
		<h1>
            Metrics
            <small>with love</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Metrics</li>
          </ol>
</#macro>

<#macro page_body>
<div class="row">
		            <div class="col-md-6">
			            <div class="box box-default">
			                <div class="box-header with-border">
			                  <h3 class="box-title">Http Responses Codes</h3>
			                </div><!-- /.box-header -->
			                <div class="box-body">
			                  <div class="row">
			                    <div class="col-md-8">
			                      <div class="chart-responsive">
			                        <canvas id="pagesByStatusChart" height="150"></canvas>
			                      </div><!-- ./chart-responsive -->
			                    </div><!-- /.col -->
			                    <div class="col-md-4">
			                      <ul class="chart-legend clearfix">
			                        <li><i class="fa fa-circle-o text-aqua"></i> 1xx</li>
			                        <li><i class="fa fa-circle-o text-green"></i> 2xx</li>
			                        <li><i class="fa fa-circle-o text-blue"></i> 3xx</li>
			                        <li><i class="fa fa-circle-o text-yellow"></i> 4xx</li>
			                        <li><i class="fa fa-circle-o text-red"></i> 5xx</li>
			                        <li><i class="fa fa-circle-o text-gray"></i> Unknown</li>
			                      </ul>
			                    </div><!-- /.col -->
			                  </div><!-- /.row -->
			                </div><!-- /.box-body -->
			              </div><!-- /.box -->
			            </div>
			            <div class="col-md-6">
			              <div class="box box-default">
			                <div class="box-header with-border">
			                  <h3 class="box-title">Pages by host</h3>
			                </div><!-- /.box-header -->
			                <div class="box-body">
			                  <div class="row">
			                    <div class="col-md-8">
			                      <div class="chart-responsive">
			                        <canvas id="pagesByHostChart" height="150"></canvas>
			                      </div><!-- ./chart-responsive -->
			                    </div><!-- /.col -->
			                    <div class="col-md-4">
			                      <ul class="chart-legend clearfix">
			                        <#if (metrics.hosts)??>
				                    <#list metrics.hosts?keys as key>
			                        <li><i class="fa fa-circle-o text-red"></i> ${key}</li>
			                        </#list>
			                        </#if>
			                      </ul>
			                    </div><!-- /.col -->
			                  </div><!-- /.row -->
			                </div><!-- /.box-body -->
			              </div><!-- /.box -->
		          		</div>
		          </div>
		          
<div class="row">
            <!-- Left col -->
            <section class="col-lg-7 connectedSortable ui-sortable">
            
            
              <!-- solid graph -->
              <div class="box box-solid bg-teal-gradient">
                <div class="box-header">
                  <i class="fa fa-th"></i>
                  <h3 class="box-title">Total size</h3>
                </div>
                <div class="box-body border-radius-none">
                  <div class="chart" id="line-chart" style="height: 250px;"></div>
                </div><!-- /.box-body -->
                <div class="box-footer no-border">
                  <div class="row">
                  <!--
                    <div class="col-xs-4 text-center" style="border-right: 1px solid #f4f4f4">
                      <input type="text" class="knob" data-readonly="true" value="20" data-width="60" data-height="60" data-fgColor="#39CCCC" />
                      <div class="knob-label">Mail-Orders</div>
                    </div>
                    <div class="col-xs-4 text-center" style="border-right: 1px solid #f4f4f4">
                      <input type="text" class="knob" data-readonly="true" value="50" data-width="60" data-height="60" data-fgColor="#39CCCC" />
                      <div class="knob-label">Online</div>
                    </div>
                    <div class="col-xs-4 text-center">
                      <input type="text" class="knob" data-readonly="true" value="30" data-width="60" data-height="60" data-fgColor="#39CCCC" />
                      <div class="knob-label">In-Store</div>
                    </div>
                    -->
                  </div><!-- /.row -->
                </div><!-- /.box-footer -->
              </div><!-- /.box -->
              
              

            </section><!-- /.Left col -->
            <!-- right col (We are only adding the ID to make the widgets sortable)-->
            <section class="col-lg-5 connectedSortable ui-sortable">


              <!-- Map box -->
              <div class="box box-solid bg-light-blue-gradient">
                <div class="box-header">

                  <i class="fa fa-map-marker"></i>
                  <h3 class="box-title">Endpoints</h3>
                </div>
                <div class="box-body">
                  <div id="world-map" style="height: 250px; width: 100%;"></div>
                </div><!-- /.box-body-->
                <div class="box-footer no-border">
                  <div class="row">
                  <!--
                    <div class="col-xs-4 text-center" style="border-right: 1px solid #f4f4f4">
                      <div id="sparkline-1"></div>
                      <div class="knob-label">Visitors</div>
                    </div>
                    <div class="col-xs-4 text-center" style="border-right: 1px solid #f4f4f4">
                      <div id="sparkline-2"></div>
                      <div class="knob-label">Online</div>
                    </div>
                    <div class="col-xs-4 text-center">
                      <div id="sparkline-3"></div>
                      <div class="knob-label">Exists</div>
                    </div>
                    -->
                  </div><!-- /.row -->
                </div>
              </div>
              <!-- /.box -->
              
            </section><!-- right col -->
          </div>
</#macro>

<#macro js>
	  
	  <script src="/webjars/raphaeljs/2.1.4/raphael-min.js" type="text/javascript"></script>
	  <script src="/webjars/morrisjs/0.5.0/morris.min.js" type="text/javascript"></script>
	  <script src="/webjars/jquery-knob/1.2.11/jquery.knob.min.js" type="text/javascript"></script>
      <script>
      
	  var pagesByHostChartCanvas = $("#pagesByHostChart").get(0).getContext("2d");
	  var pagesByHostChart = new Chart(pagesByHostChartCanvas);
	    
	    var pagesByHost = [
	    <#if (metrics.hosts)??>
		<#list metrics.hosts?keys as key>
			<#assign value = metrics.hosts[key]>
			<#if key?starts_with("1")>
			  <#assign color="#00c0ef">
			<#elseif key?starts_with("2")>
			  <#assign color="#00a65a">
			<#elseif key?starts_with("3")>
			  <#assign color="#3c8dbc">
			<#elseif key?starts_with("4")>
			  <#assign color="#f39c12">
			<#elseif key?starts_with("5")>
			  <#assign color="#f56954">
			<#else>
			  <#assign color="#d2d6de">
			</#if>
			{
			  value: ${value?c},
		      color: "#f56954",
		      highlight: "#f56954",
		      label: "${key}"
		    }<#sep>, </#sep>
		</#list>
		</#if>
		];
	  
	  var pagesByStatusChartCanvas = $("#pagesByStatusChart").get(0).getContext("2d");
	  var pagesByStatusChart = new Chart(pagesByStatusChartCanvas);
        
        var pagesByStatus = [
        <#if (metrics.statuses)??>
		<#list metrics.statuses?keys as key>
			<#assign value = metrics.statuses[key]>
			<#if key?starts_with("1")>
			  <#assign color="#00c0ef">
			<#elseif key?starts_with("2")>
			  <#assign color="#00a65a">
			<#elseif key?starts_with("3")>
			  <#assign color="#3c8dbc">
			<#elseif key?starts_with("4")>
			  <#assign color="#f39c12">
			<#elseif key?starts_with("5")>
			  <#assign color="#f56954">
			<#else>
			  <#assign color="#d2d6de">
			</#if>
			{
		      value: ${value?c},
		      color: "${color}",
		      highlight: "${color}",
		      label: "${key}"
		    }<#sep>, </#sep>
		</#list>
		</#if>
		];
		
	  var pieOptions = {
	    segmentShowStroke: true,
	    segmentStrokeColor: "#fff",
	    segmentStrokeWidth: 1,
	    percentageInnerCutout: 50, // This is 0 for Pie charts
	    animationSteps: 100,
	    animationEasing: "easeOutBounce",
	    animateRotate: true,
	    animateScale: false,
	    responsive: true,
	    maintainAspectRatio: false,
	    legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>",
	    tooltipTemplate: "<%=value %> <%=label%>"
	  };
	  //Create pie or douhnut chart
	  // You can switch between pie and douhnut using the method below.  
	  pieOptions['tooltipTemplate'] = "<%=value %> pages for http status <%=label%>";
	  pagesByStatusChart.Doughnut(pagesByStatus, pieOptions);
	  pieOptions['tooltipTemplate'] = "<%=value %> pages for host <%=label%>";
	  pagesByHostChart.Doughnut(pagesByHost, pieOptions);
			  
        /* jQueryKnob */
  $(".knob").knob();
  
        //Sparkline charts
  var myvalues = [1000, 1200, 920, 927, 931, 1027, 819, 930, 1021];
  $('#sparkline-1').sparkline(myvalues, {
    type: 'line',
    lineColor: '#92c1dc',
    fillColor: "#ebf4f9",
    height: '50',
    width: '80'
  });
  myvalues = [515, 519, 520, 522, 652, 810, 370, 627, 319, 630, 921];
  $('#sparkline-2').sparkline(myvalues, {
    type: 'line',
    lineColor: '#92c1dc',
    fillColor: "#ebf4f9",
    height: '50',
    width: '80'
  });
  myvalues = [15, 19, 20, 22, 33, 27, 31, 27, 19, 30, 21];
  $('#sparkline-3').sparkline(myvalues, {
    type: 'line',
    lineColor: '#92c1dc',
    fillColor: "#ebf4f9",
    height: '50',
    width: '80'
  });
  
  
  //jvectormap data
  var visitorsData = {
    "US": 398, //USA
    "SA": 400, //Saudi Arabia
    "CA": 1000, //Canada
    "DE": 500, //Germany
    "FR": 760, //France
    "CN": 300, //China
    "AU": 700, //Australia
    "BR": 600, //Brazil
    "IN": 800, //India
    "GB": 320, //Great Britain
    "RU": 3000 //Russia
  };
  //World map by jvectormap
  $('#world-map').vectorMap({
    map: 'world_mill_en',
    backgroundColor: "transparent",
    regionStyle: {
      initial: {
        fill: '#e4e4e4',
        "fill-opacity": 1,
        stroke: 'none',
        "stroke-width": 0,
        "stroke-opacity": 1
      }
    },
    series: {
      regions: [{
          values: visitorsData,
          scale: ["#92c1dc", "#ebf4f9"],
          normalizeFunction: 'polynomial'
        }]
    },
    onRegionLabelShow: function (e, el, code) {
      if (typeof visitorsData[code] != "undefined")
        el.html(el.html() + ': ' + visitorsData[code] + ' targeted blob');
    }
  });
  	var line = new Morris.Line({
    element: 'line-chart',
    resize: true,
    data: [
	<#list totalSize as el>
      {y: "${el.time}", item1: ${el.value}}<#sep>, </#sep>
     </#list>
    ],
    xkey: 'y',
    ykeys: ['item1'],
    labels: ['Item 1'],
    lineColors: ['#efefef'],
    lineWidth: 2,
    hideHover: 'auto',
    gridTextColor: "#fff",
    gridStrokeWidth: 0.4,
    pointSize: 4,
    pointStrokeColors: ["#efefef"],
    gridLineColor: "#efefef",
    gridTextFamily: "Open Sans",
    gridTextSize: 10
  });	
		</script>
</#macro>

<@display_page/>