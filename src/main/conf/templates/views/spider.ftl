<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>Spider</title>
</#macro>

<#macro content_header>
		<h1>
            Spiders
            <small>how the job is done</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/spiders"><i class="fa fa-dashboard"></i> Spiders</a></li>
            <li class="active">The Evil Spider</li>
          </ol>
</#macro>

<#macro js>
		<script>
			'use strict';
			$(function () {
			  // Get context with jQuery - using jQuery's .get() method.
			  var pieChartCanvas = $("#pieChart").get(0).getContext("2d");
			  var pieChart = new Chart(pieChartCanvas);
			  var PieData = [
			    {
			      value: 700,
			      color: "#f56954",
			      highlight: "#f56954",
			      label: "Chrome"
			    },
			    {
			      value: 500,
			      color: "#00a65a",
			      highlight: "#00a65a",
			      label: "IE"
			    },
			    {
			      value: 400,
			      color: "#f39c12",
			      highlight: "#f39c12",
			      label: "FireFox"
			    },
			    {
			      value: 600,
			      color: "#00c0ef",
			      highlight: "#00c0ef",
			      label: "Safari"
			    },
			    {
			      value: 300,
			      color: "#3c8dbc",
			      highlight: "#3c8dbc",
			      label: "Opera"
			    },
			    {
			      value: 100,
			      color: "#d2d6de",
			      highlight: "#d2d6de",
			      label: "Navigator"
			    }
			  ];
			  var pieOptions = {
			    //Boolean - Whether we should show a stroke on each segment
			    segmentShowStroke: true,
			    //String - The colour of each segment stroke
			    segmentStrokeColor: "#fff",
			    //Number - The width of each segment stroke
			    segmentStrokeWidth: 1,
			    //Number - The percentage of the chart that we cut out of the middle
			    percentageInnerCutout: 50, // This is 0 for Pie charts
			    //Number - Amount of animation steps
			    animationSteps: 100,
			    //String - Animation easing effect
			    animationEasing: "easeOutBounce",
			    //Boolean - Whether we animate the rotation of the Doughnut
			    animateRotate: true,
			    //Boolean - Whether we animate scaling the Doughnut from the centre
			    animateScale: false,
			    //Boolean - whether to make the chart responsive to window resizing
			    responsive: true,
			    // Boolean - whether to maintain the starting aspect ratio or not when responsive, if set to false, will take up entire container
			    maintainAspectRatio: false,
			    //String - A legend template
			    legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>",
			    //String - A tooltip template
			    tooltipTemplate: "<%=value %> <%=label%> users"
			  };
			  //Create pie or douhnut chart
			  // You can switch between pie and douhnut using the method below.  
			  pieChart.Doughnut(PieData, pieOptions);
			  
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

</#macro>

<#macro page_body>

		<div class="row">
            <!-- Left col -->
            <div class="col-md-8">
              <!-- MAP & BOX PANE -->
              <div class="box box-success">
                <div class="box-header with-border">
                  <h3 class="box-title">Visitors Report</h3>
                  <div class="box-tools pull-right">
                    <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                    <button class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                  </div>
                </div><!-- /.box-header -->
                <div class="box-body no-padding">
                  <div class="row">
                    <div class="col-md-9 col-sm-8">
                      <div class="pad">
                        <!-- Map will be created here -->
                        <div id="world-map-markers" style="height: 325px;"></div>
                      </div>
                    </div><!-- /.col -->
                    <div class="col-md-3 col-sm-4">
                      <div class="pad box-pane-right bg-green" style="min-height: 280px">
                        <div class="description-block margin-bottom">
                          <div class="sparkbar pad" data-color="#fff">90,70,90,70,75,80,70</div>
                          <h5 class="description-header">8390</h5>
                          <span class="description-text">Visits</span>
                        </div><!-- /.description-block -->
                        <div class="description-block margin-bottom">
                          <div class="sparkbar pad" data-color="#fff">90,50,90,70,61,83,63</div>
                          <h5 class="description-header">30%</h5>
                          <span class="description-text">Referrals</span>
                        </div><!-- /.description-block -->
                        <div class="description-block">
                          <div class="sparkbar pad" data-color="#fff">90,50,90,70,61,83,63</div>
                          <h5 class="description-header">70%</h5>
                          <span class="description-text">Organic</span>
                        </div><!-- /.description-block -->
                      </div>
                    </div><!-- /.col -->
                  </div><!-- /.row -->
                </div><!-- /.box-body -->
              </div><!-- /.box -->

            </div><!-- /.col -->

            <div class="col-md-4">
              <!-- Info Boxes Style 2 -->
              <div class="info-box bg-yellow">
                <span class="info-box-icon"><i class="ion ion-ios-pricetag-outline"></i></span>
                <div class="info-box-content">
                  <span class="info-box-text">Inventory</span>
                  <span class="info-box-number">5,200</span>
                  <div class="progress">
                    <div class="progress-bar" style="width: 50%"></div>
                  </div>
                  <span class="progress-description">
                    50% Increase in 30 Days
                  </span>
                </div><!-- /.info-box-content -->
              </div><!-- /.info-box -->
              <div class="info-box bg-green">
                <span class="info-box-icon"><i class="ion ion-ios-heart-outline"></i></span>
                <div class="info-box-content">
                  <span class="info-box-text">Mentions</span>
                  <span class="info-box-number">92,050</span>
                  <div class="progress">
                    <div class="progress-bar" style="width: 20%"></div>
                  </div>
                  <span class="progress-description">
                    20% Increase in 30 Days
                  </span>
                </div><!-- /.info-box-content -->
              </div><!-- /.info-box -->
              <div class="info-box bg-red">
                <span class="info-box-icon"><i class="ion ion-ios-cloud-download-outline"></i></span>
                <div class="info-box-content">
                  <span class="info-box-text">Downloads</span>
                  <span class="info-box-number">114,381</span>
                  <div class="progress">
                    <div class="progress-bar" style="width: 70%"></div>
                  </div>
                  <span class="progress-description">
                    70% Increase in 30 Days
                  </span>
                </div><!-- /.info-box-content -->
              </div><!-- /.info-box -->
              <div class="info-box bg-aqua">
                <span class="info-box-icon"><i class="ion-ios-chatbubble-outline"></i></span>
                <div class="info-box-content">
                  <span class="info-box-text">Direct Messages</span>
                  <span class="info-box-number">163,921</span>
                  <div class="progress">
                    <div class="progress-bar" style="width: 40%"></div>
                  </div>
                  <span class="progress-description">
                    40% Increase in 30 Days
                  </span>
                </div><!-- /.info-box-content -->
              </div><!-- /.info-box -->


			
              <div class="box box-default">
                <div class="box-header with-border">
                  <h3 class="box-title">Browser Usage</h3>
                  <div class="box-tools pull-right">
                    <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
                    <button class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                  </div>
                </div><!-- /.box-header -->
                <div class="box-body">
                  <div class="row">
                    <div class="col-md-8">
                      <div class="chart-responsive">
                        <canvas id="pieChart" height="160" width="318" style="width: 318px; height: 160px;"></canvas>
                      </div><!-- ./chart-responsive -->
                    </div><!-- /.col -->
                    <div class="col-md-4">
                      <ul class="chart-legend clearfix">
                        <li><i class="fa fa-circle-o text-red"></i> Chrome</li>
                        <li><i class="fa fa-circle-o text-green"></i> IE</li>
                        <li><i class="fa fa-circle-o text-yellow"></i> FireFox</li>
                        <li><i class="fa fa-circle-o text-aqua"></i> Safari</li>
                        <li><i class="fa fa-circle-o text-light-blue"></i> Opera</li>
                        <li><i class="fa fa-circle-o text-gray"></i> Navigator</li>
                      </ul>
                    </div><!-- /.col -->
                  </div><!-- /.row -->
                </div><!-- /.box-body -->
                <div class="box-footer no-padding">
                  <ul class="nav nav-pills nav-stacked">
                    <li><a href="#">United States of America <span class="pull-right text-red"><i class="fa fa-angle-down"></i> 12%</span></a></li>
                    <li><a href="#">India <span class="pull-right text-green"><i class="fa fa-angle-up"></i> 4%</span></a></li>
                    <li><a href="#">China <span class="pull-right text-yellow"><i class="fa fa-angle-left"></i> 0%</span></a></li>
                  </ul>
                </div><!-- /.footer -->
              </div><!-- /.box -->
            </div><!-- /.col -->
          </div>

</#macro>

<@display_page/>