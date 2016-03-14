<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Spider</title>
  
  <script src="/public/js/jsoneditor.js"></script>
  <script src="/public/js/jsoneditor-theme.js"></script>
	<script>
		JSONEditor.defaults.theme = 'adminlte';
	    JSONEditor.defaults.iconlib = 'bootstrap3';
	    JSONEditor.defaults.options.disable_properties = true;
	    JSONEditor.defaults.options.disable_edit_json = true;
	    JSONEditor.defaults.options.no_additional_properties = true;
	    JSONEditor.defaults.options.required_by_default = true;
	    JSONEditor.defaults.options.keep_oneof_values = false;
	    JSONEditor.defaults.options.disable_collapse = true;
	</script>   
	    
	</script> 
</#macro>

<#macro content_header>
		<h1>
            Spider '${spider.name}'
            <small>how the job is done</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/spiders"><i class="fa fa-tasks"></i> Spiders</a></li>
            <li class="active">${spider.name}</li>
          </ol>
</#macro>

<#macro js>
		<script>
			'use strict';
			$(function () {
			
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
			  
			  /* jVector Maps
			   * ------------
			   * Create a world map with markers
			   */
			  $('#world-map-markers').vectorMap({
			    map: 'world_mill_en',
			    normalizeFunction: 'polynomial',
			    hoverOpacity: 0.7,
			    hoverColor: false,
			    backgroundColor: 'transparent',
			    regionStyle: {
			      initial: {
			        fill: 'rgba(210, 214, 222, 1)',
			        "fill-opacity": 1,
			        stroke: 'none',
			        "stroke-width": 0,
			        "stroke-opacity": 1
			      },
			      hover: {
			        "fill-opacity": 0.7,
			        cursor: 'pointer'
			      },
			      selected: {
			        fill: 'yellow'
			      },
			      selectedHover: {
			      }
			    },
			    markerStyle: {
			      initial: {
			        fill: '#00a65a',
			        stroke: '#111'
			      }
			    },
			    markers: [
			      {latLng: [41.90, 12.45], name: 'Vatican City'},
			      {latLng: [43.73, 7.41], name: 'Monaco'},
			      {latLng: [-0.52, 166.93], name: 'Nauru'},
			      {latLng: [-8.51, 179.21], name: 'Tuvalu'},
			      {latLng: [43.93, 12.46], name: 'San Marino'},
			      {latLng: [47.14, 9.52], name: 'Liechtenstein'},
			      {latLng: [7.11, 171.06], name: 'Marshall Islands'},
			      {latLng: [17.3, -62.73], name: 'Saint Kitts and Nevis'},
			      {latLng: [3.2, 73.22], name: 'Maldives'},
			      {latLng: [35.88, 14.5], name: 'Malta'},
			      {latLng: [12.05, -61.75], name: 'Grenada'},
			      {latLng: [13.16, -61.23], name: 'Saint Vincent and the Grenadines'},
			      {latLng: [13.16, -59.55], name: 'Barbados'},
			      {latLng: [17.11, -61.85], name: 'Antigua and Barbuda'},
			      {latLng: [-4.61, 55.45], name: 'Seychelles'},
			      {latLng: [7.35, 134.46], name: 'Palau'},
			      {latLng: [42.5, 1.51], name: 'Andorra'},
			      {latLng: [14.01, -60.98], name: 'Saint Lucia'},
			      {latLng: [6.91, 158.18], name: 'Federated States of Micronesia'},
			      {latLng: [1.3, 103.8], name: 'Singapore'},
			      {latLng: [1.46, 173.03], name: 'Kiribati'},
			      {latLng: [-21.13, -175.2], name: 'Tonga'},
			      {latLng: [15.3, -61.38], name: 'Dominica'},
			      {latLng: [-20.2, 57.5], name: 'Mauritius'},
			      {latLng: [26.02, 50.55], name: 'Bahrain'},
			      {latLng: [0.33, 6.73], name: 'SÃ£o TomÃ© and PrÃ­ncipe'}
			    ]
			  });
			
			  /* SPARKLINE CHARTS
			   * ----------------
			   * Create a inline charts with spark line
			   */
			
			  //-----------------
			  //- SPARKLINE BAR -
			  //-----------------
			  $('.sparkbar').each(function () {
			    var $this = $(this);
			    $this.sparkline('html', {
			      type: 'bar',
			      height: $this.data('height') ? $this.data('height') : '30',
			      barColor: $this.data('color')
			    });
			  });
			
			  //-----------------
			  //- SPARKLINE PIE -
			  //-----------------
			  $('.sparkpie').each(function () {
			    var $this = $(this);
			    $this.sparkline('html', {
			      type: 'pie',
			      height: $this.data('height') ? $this.data('height') : '90',
			      sliceColors: $this.data('color')
			    });
			  });
			
			  //------------------
			  //- SPARKLINE LINE -
			  //------------------
			  $('.sparkline').each(function () {
			    var $this = $(this);
			    $this.sparkline('html', {
			      type: 'line',
			      height: $this.data('height') ? $this.data('height') : '90',
			      width: '100%',
			      lineColor: $this.data('linecolor'),
			      fillColor: $this.data('fillcolor'),
			      spotColor: $this.data('spotcolor')
			    });
			  });
			});
			</script>

	<script>
	  var $output = document.getElementById("output");
	  document.getElementById('submit').addEventListener('click',function() {
	    var form = document.createElement("form");
		form.setAttribute("method", "post");
		form.setAttribute("action", "/spiders/${spider.id?c}/edit");
		
		var hiddenField = document.createElement("input");              
		hiddenField.setAttribute("type", "hidden");
        hiddenField.setAttribute("name", "definition");
        hiddenField.setAttribute("value", $output.value);
		form.appendChild(hiddenField);
		document.body.appendChild(form);
		
		form.submit();
      });
      
	  function refresh() {
	    
	  	var json = baseEditor.getValue();
	  	json.stores = storesEditor.getValue();
	  	json.frontier = frontierEditor.getValue();
	  	json.extractors = extractionEditor.getValue();
	  	json.politeness = politenessEditor.getValue();
	  	json.client = advancedEditor.getValue();
	    $output.value = JSON.stringify(json,null,5);
	  }
	  
      function prepare(baseValue, baseEditorId, ref, outputId, validationId) {
	      var $output_errors = document.getElementById(outputId + "_errors");
	      var $output_errors_nb = document.getElementById(outputId + "_errors_nb");
	      var starting_value = baseValue;
	      var editor = new JSONEditor(document.getElementById(baseEditorId),{
	        ajax: true,
	        schema: {
	          $ref: ref
	        },
	        startval: starting_value,
	      });
	      editor.on('change',function() {
	        refresh();
	      	var json = editor.getValue();
	        var errors = editor.validate();
	        $output_errors.getElementsByTagName("ul")[0].innerHTML = '';
	        // Not valid
	        if(errors.length) {
	          console.log(errors);
	          $output_errors.className = $output_errors.className.replace(/\bhidden\b/,'');
	          errors.forEach(function (error, i) {
	          	$output_errors.getElementsByTagName("ul")[0].innerHTML += "<li>" + errors[i].message + "(" + errors[i].path + ")" + "</li>";
	          });
	          $output_errors_nb.className = $output_errors_nb.className.replace(/\bhidden\b/,'');
	          $output_errors_nb.setAttribute("data-original-title",errors.length + " errors");
	          $output_errors_nb.innerHTML=errors.length;
	        }
	        // Valid
	        else {
	          if($output_errors.className.indexOf('hidden') == -1) {
  	            $output_errors.className += 'hidden';
	          }
	          if($output_errors_nb.className.indexOf('hidden') == -1) {
  	            $output_errors_nb.className += 'hidden';
	          }
	        }
	      });
	      return editor;
      }
      
      var baseEditor = prepare(${baseValue}, 'base_editor_holder', "/public/schemas/base.json", 'output_base', 'valid_indicator_base');
      var storesEditor = prepare(${storesValue}, 'stores_editor_holder', "/public/schemas/stores.json", 'output_stores', 'valid_indicator_stores');
      var frontierEditor = prepare(${frontierValue}, 'frontier_editor_holder', "/public/schemas/frontier.json", 'output_frontier', 'valid_indicator_frontier');
      var extractionEditor = prepare(${extractionValue}, 'extraction_editor_holder', "/public/schemas/extraction.json", 'output_extraction', 'valid_indicator_extraction');
      var politenessEditor = prepare(${politenessValue}, 'politeness_editor_holder', "/public/schemas/politeness.json", 'output_politeness', 'valid_indicator_politeness');
      var advancedEditor = prepare(${advancedValue}, 'advanced_editor_holder', "/public/schemas/advanced.json", 'output_advanced', 'valid_indicator_advanced');
      
    </script>
</#macro>

<#macro page_body>

	<div class="row">
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-aqua"><i class="fa fa-signal"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Bandwidth</span>
	          <span class="info-box-number">?/s</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-green"><i class="fa fa-download"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total size</span>
	          <span class="info-box-number">${printBytesSize(metrics.totalSize)}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-yellow"><i class="fa fa-files-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Total pages</span>
	          <span class="info-box-number">${(metrics.nbPagesTotal)!0}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	    <div class="col-md-3 col-sm-6 col-xs-12">
	      <div class="info-box">
	        <span class="info-box-icon bg-red"><i class="fa fa-star-o"></i></span>
	        <div class="info-box-content">
	          <span class="info-box-text">Timeout</span>
	          <span class="info-box-number">${(metrics.connectTimeout)!0 + (metrics.readTimeout)!0}</span>
	        </div><!-- /.info-box-content -->
	      </div><!-- /.info-box -->
	    </div><!-- /.col -->
	  </div>
	  
		<div class="row">
            <!-- Left col -->
            <div class="col-md-8">
            	<div class="row">
            		<div class="col-md-12">
					  <div class="box">
					    <div class="box-header">
					      <h3 class="box-title">Actions</h3>
					    </div>
					    <div class="box-body">
					      <p>Current status: ${spider.status}</p>
					      <a class="btn btn-app <#if spider.status != "initiated" && spider.status != "started">disabled</#if>" href="#configuration">
					        <i class="fa fa-edit"></i> Edit
					      </a>
					      <a class="btn btn-app <#if spider.status != "initiated" && spider.status != "paused">disabled</#if>" href="/spiders/${spider.id?c}/start">
					        <i class="fa fa-play"></i> Start
					      </a>
					      <a class="btn btn-app <#if spider.status != "started">disabled</#if>" href="/spiders/${spider.id?c}/pause">
					        <i class="fa fa-pause"></i> Pause
					      </a>
					      <a class="btn btn-app <#if spider.status != "initiated" && spider.status != "started">disabled</#if>" href="/spiders/${spider.id?c}/cancel">
					        <i class="fa fa-exclamation-triangle"></i> Cancel
					      </a>
					      <a class="btn btn-app <#if spider.status != "killed" && spider.status != "ended">disabled</#if>" href="/spiders/${spider.id?c}/delete">
					        <i class="fa fa-eraser"></i> Delete
					      </a>
					      <a class="btn btn-app <#if spider.status != "started">disabled</#if>" href="/spiders/${spider.id?c}/reinject">
					        <i class="fa fa-circle-o-notch"></i> Re-source
					      </a>
					      <a class="btn btn-app" href="/spiders/${spider.id?c}/fork">
					        <i class="fa fa-code-fork"></i> Duplicate
					      </a>
					    </div><!-- /.box-body -->
					  </div><!-- /.box -->
					 </div>
            	</div>
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
		      
		      <div class="box box-default" id="configuration">
		        <div class="box-header with-border">
                  <h3 class="box-title">Configuration</h3>
                </div><!-- /.box-header -->
                <div class="box-body">
                
                  <#if errors??>
                  <div class="alert alert-warning">
		            <h4><i class="icon fa fa-warning"></i> Alert!</h4>
		            <ul>
	                  <#list errors as error>
		            	<li>${error}</li>
			          </#list>
		            </ul>
		          </div>
		          </#if>
                
		          <!-- Custom Tabs -->
	              <div class="nav-tabs-custom">
	                <ul class="nav nav-tabs">
	                  <li class="active"><a href="#tab_1" data-toggle="tab">Base <span id="output_base_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
	                  <li><a href="#tab_2" data-toggle="tab">Stores <span id="output_stores_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
	                  <li><a href="#tab_3" data-toggle="tab">Frontier <span id="output_frontier_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
	                  <li><a href="#tab_4" data-toggle="tab">Extraction <span id="output_extraction_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
	                  <li><a href="#tab_5" data-toggle="tab">Politeness <span id="output_politeness_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
	                  <li><a href="#tab_6" data-toggle="tab">Advanced <span id="output_advanced_errors_nb" data-toggle="tooltip" title="" class="badge bg-yellow hidden" data-original-title="3 errors">3</span></a></li>
	                  <li class="pull-right"><a class="text-muted"><i class="fa fa-gear"></i></a></li>
	                </ul>
	                <div class="tab-content">
	                
	                  <div class="tab-pane active" id="tab_1">
	                  <div class="alert alert-warning hidden" id="output_base_errors">
			            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
			            <ul></ul>
			          </div>
	                    <div class="box-body" id="base_editor_holder"></div>
	                  </div><!-- /.tab-pane -->
	                  
	                  <div class="tab-pane" id="tab_2">
	                  	 <div class="alert alert-warning hidden" id="output_stores_errors">
				            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
				            <ul></ul>
				          </div>
						<div class="box-body" id="stores_editor_holder"></div>
	                  </div><!-- /.tab-pane -->
	                  
	                  <div class="tab-pane" id="tab_3">
	                  	 <div class="alert alert-warning hidden" id="output_frontier_errors">
				            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
				            <ul></ul>
				          </div>
						<div class="box-body" id="frontier_editor_holder"></div>
	                  </div><!-- /.tab-pane -->
	                  
	                  <div class="tab-pane" id="tab_4">
	                  	<div class="alert alert-warning hidden" id="output_extraction_errors">
				            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
				            <ul></ul>
				          </div>
						<div class="box-body" id="extraction_editor_holder"></div>
	                  </div><!-- /.tab-pane -->
	                  
	                  <div class="tab-pane" id="tab_5">
	                  	<div class="alert alert-warning hidden" id="output_politeness_errors">
				            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
				            <ul></ul>
				          </div>
						<div class="box-body" id="politeness_editor_holder"></div>
	                  </div><!-- /.tab-pane -->
	                                    
	                  <div class="tab-pane" id="tab_6">
	                  	<div class="alert alert-warning hidden" id="output_advanced_errors">
				            <h4><i class="icon fa fa-warning hidden"></i> Alert!</h4>
				            <ul></ul>
				          </div>
						<div class="box-body" id="advanced_editor_holder"></div>
	                  </div><!-- /.tab-pane -->
	                  
	                </div><!-- /.tab-content -->
	              </div><!-- nav-tabs-custom -->
	           </div>
	              <div class="box-footer">
	                    <button id="submit" type="submit" class="btn btn-info pull-right <#if spider.status != "initiated" && spider.status != "paused">disabled</#if>">Submit</button>
	                  </div>
	           </div>
	               
		          <!-- general form elements disabled -->
              <div class="box box-warning">
                <div class="box-header with-border">
                  <h3 class="box-title">Raw JSON definition</h3>
                </div><!-- /.box-header -->
                <div class="box-body">
                  <form role="form">

                    <!-- textarea -->
                    <div class="form-group">
                      <label>JSON</label>
                      <textarea class="form-control" rows="20">${json}</textarea>
                    </div>

                  </form>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
		          
              <!-- MAP & BOX PANE 
              <div class="box box-success">
                <div class="box-header with-border">
                  <h3 class="box-title">IP Locations Report</h3>
                  <div class="box-tools pull-right">
                    <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                    <button class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                  </div>
                </div>
                <div class="box-body no-padding">
                  <div class="row">
                    <div class="col-md-9 col-sm-8">
                      <div class="pad">
                        <div id="world-map-markers" style="height: 325px;"></div>
                      </div>
                    </div>
                    <div class="col-md-3 col-sm-4">
                      <div class="pad box-pane-right bg-green" style="min-height: 280px">
                        <div class="description-block margin-bottom">
                          <div class="sparkbar pad" data-color="#fff">90,70,90,70,75,80,70</div>
                          <h5 class="description-header">8390</h5>
                          <span class="description-text">Visits</span>
                        </div>
                        <div class="description-block margin-bottom">
                          <div class="sparkbar pad" data-color="#fff">90,50,90,70,61,83,63</div>
                          <h5 class="description-header">30%</h5>
                          <span class="description-text">Referrals</span>
                        </div>
                        <div class="description-block">
                          <div class="sparkbar pad" data-color="#fff">90,50,90,70,61,83,63</div>
                          <h5 class="description-header">70%</h5>
                          <span class="description-text">Organic</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>-->

            </div><!-- /.col -->

            <div class="col-md-4">
	        	<div class="small-box bg-aqua disabled">
	                <div class="inner">
	                  <h3>${(metrics.nbPagesTotal)!0}</h3>
	                  <p>Blobs of raw data</p>
	                </div>
	                <div class="icon">
	                  <i class="ion ion-ios-cloud-download-outline"></i>
	                </div>
	                <#if spider.stores.blobStore?? && spider.status != "deleted">
	                <div class="margin">
	                    <div class="btn-group">
	                      <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
	                        Download
	                        <span class="caret"></span>
	                        <span class="sr-only">Toggle Dropdown</span>
	                      </button>
	                      <ul class="dropdown-menu" role="menu">
	                        <li><a href="/api/v1/spiders/${spider.id?c}/raw/export?format=json" target="_blank">JSON</a></li>
	                        <li><a href="/api/v1/spiders/${spider.id?c}/raw/export?format=csv" target="_blank">CSV</a></li>
	                      </ul>
	                    </div>
	                  </div>
	                  <a href="#" class="small-box-footer">
	                  More info <i class="fa fa-arrow-circle-right"></i>
	                </a>
		            </#if>
	              </div>
	              <#if spider.extractors?? && spider.extractors.data??>
		              <#list spider.extractors.data as extractor>
		              <div class="small-box bg-orange disabled">
		                <div class="inner">
		                  <h3>${(metrics.extractors[extractor.getName()])!"0"}</h3>
		                  <p>Extractor '${extractor.getName()}'</p>
		                </div>
		                <div class="icon">
		                  <i class="ion ion-ios-pricetag-outline"></i>
		                </div>
		                <#if spider.status != "deleted">
		                <div class="margin">
		                    <div class="btn-group">
		                      <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
		                        Download
		                        <span class="caret"></span>
		                        <span class="sr-only">Toggle Dropdown</span>
		                      </button>
		                      <ul class="dropdown-menu" role="menu">
		                        <li><a href="/api/v1/spiders/${spider.id?c}/export/${extractor.getName()}?format=json" target="_blank">JSON</a></li>
		                        <li><a href="/api/v1/spiders/${spider.id?c}/export/${extractor.getName()}?format=csv" target="_blank">CSV</a></li>
		                      </ul>
		                    </div>
		                  </div>
		                  <a href="/spiders/${spider.id?c}/data/${extractor.getName()}" class="small-box-footer">
		                  View data <i class="fa fa-arrow-circle-right"></i>
		                </a>
		                </#if>
		              </div>
		              </#list>
	              </#if>
            </div><!-- /.col -->
          </div>

</#macro>

<@display_page/>