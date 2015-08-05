<#include "../layout.ftl">

<#macro page_head>
  <@common_page_head/>
  <title>New spider</title>
</#macro>

<#macro content_header>
		<h1>
            New spider
            <small>a new one please!</small>
          </h1>
          <ol class="breadcrumb">
            <li><a href="/"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="/spiders"><i class="fa fa-tasks"></i> Spiders</a></li>
            <li class="active">Create</li>
          </ol>
</#macro>

<#macro js>
		<script>
		$('#type-source-modal').on('show.bs.modal', function(event) {
			var modal=$(this)
		});
		</script>
</#macro>

<#macro page_body>


            <div id="type-source-modal" class="modal fade" role="dialog">
              <div class="modal-dialog" role="document">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title">Choose the type of the source</h4>
                  </div>
                  <div class="modal-body">
                  	<form role="form" class="form-horizontal">
	                  <div class="box-body">
	                    <div class="form-group">
	                      <label>Select</label>
	                      <select class="form-control">
	                        <option>Fixed</option>
	                        <option>JMS</option>
	                        <option>Robots.txt</option>
	                        <option>CSV</option>
	                        <option>JDBC</option>
	                      </select>
	                    </div>
	                  </div>
	                </form>
                  </div>
                  <div class="modal-footer">
                    <button type="button" class="btn btn-default pull-left" data-dismiss="modal">Close</button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" data-toggle="modal" data-target="#new-source-modal"><i class="fa fa-plus"></i> Configure the new source!</button>
                  </div>
                </div><!-- /.modal-content -->
              </div><!-- /.modal-dialog -->
            </div><!-- /.modal -->
            <div id="new-source-modal" class="modal fade" role="dialog">
              <div class="modal-dialog" role="document">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title">Modal Default</h4>
                  </div>
                  <div class="modal-body">
                    <form role="form" class="form-horizontal">
	                  <div class="box-body">
	                    <div class="form-group">
	                      <label for="robotsTxt" class="col-sm-2 control-label">robotsTxt</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="robotsTxt" placeholder="robotsTxt">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="maxDepth" class="col-sm-2 control-label">maxDepth</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="maxDepth" placeholder="maxDepth">
	                      </div>
	                    </div>
	                  </div><!-- /.box-body -->
                    </form>
                  </div>
                  <div class="modal-footer">
                    <button type="button" class="btn btn-default pull-left" data-dismiss="modal">Close</button>
                    <button type="button" class="btn btn-primary">Save changes</button>
                  </div>
                </div><!-- /.modal-content -->
              </div><!-- /.modal-dialog -->
            </div><!-- /.modal -->
		
		<div class="row">		
			<div class="col-md-6">
	            
	              <!-- General -->
	              <div class="box box-primary">
	                <div class="box-header with-border">
	                  <h3 class="box-title">General</h3>
	                </div><!-- /.box-header -->
		            <form role="form" class="form-horizontal">
	                  <div class="box-body">
	                    <div class="form-group">
	                      <label for="name" class="col-sm-2 control-label">Name</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="name" placeholder="Name">
	                      </div>
	                    </div>
	                  </div><!-- /.box-body -->
                    </form>
	              </div><!-- /.box -->
	              
	              <!-- Sources -->
	              <div class="box box-success" id="sources">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Sources</h3>
	                </div>
	                <div class="box-body">
	                  <div class="callout">
	                  	<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
	                    <h4>Source ''</h4>
	                    <dl class="dl-horizontal">
		                    <dt>Description lists</dt>
		                    <dd>A description list is perfect for defining terms.</dd>
		                    <dt>Euismod</dt>
		                    <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
		                    <dd>Donec id elit non mi porta gravida at eget metus.</dd>
		                    <dt>Malesuada porta</dt>
		                    <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
		                    <dt>Felis euismod semper eget lacinia</dt>
		                    <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
		              	</dl>
	                  </div>
	                  
	                  <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#type-source-modal"><i class="fa fa-plus"></i> Add a new source!</button>
	                </div><!-- /.box-body -->
	              </div><!-- /.box -->
	              
	              <!-- Filters -->
	              <div class="box box-success" id="filters">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Filters</h3>
	                </div>
	                <div class="box-body">
	                	<form role="form" class="form-horizontal">
		                  <div class="box-body">
		                    <div class="form-group">
		                      <label for="links" class="col-sm-2 control-label">Links</label>
		                      <div class="col-sm-10">
		                        <textarea class="form-control" id="links" placeholder="Name" rows="3">
		                        </textarea>
		                      </div>
		                    </div>
		                  </div><!-- /.box-body -->
	                    </form>
	                </div><!-- /.box-body -->
	              </div><!-- /.box -->
	              
	              <!-- Extractors -->
	              <div class="box box-success">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Extractors</h3>
	                </div>
	                <div class="box-body">
	                  <div class="callout">
	                  	<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
	                    <h4>Extractor ''</h4>
	                    <dl class="dl-horizontal">
		                    <dt>Description lists</dt>
		                    <dd>A description list is perfect for defining terms.</dd>
		                    <dt>Euismod</dt>
		                    <dd>Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>
		                    <dd>Donec id elit non mi porta gravida at eget metus.</dd>
		                    <dt>Malesuada porta</dt>
		                    <dd>Etiam porta sem malesuada magna mollis euismod.</dd>
		                    <dt>Felis euismod semper eget lacinia</dt>
		                    <dd>Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>
		              	</dl>
	                  </div>
	                  <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#type-extractor-modal"><i class="fa fa-plus"></i> Add a new extractor!</button>
	                </div><!-- /.box-body -->
	              </div><!-- /.box -->
	              
	              <!-- Strategy -->
	              <div class="box box-primary">
	                <div class="box-header with-border">
	                  <h3 class="box-title">Strategy</h3>
	                </div><!-- /.box-header -->
		            <form role="form" class="form-horizontal">
	                  <div class="box-body">
	                    <div class="form-group">
	                      <label for="requestTimeOut" class="col-sm-2 control-label">requestTimeOut</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="requestTimeOut" placeholder="requestTimeOut">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="maxRedirects" class="col-sm-2 control-label">maxRedirects</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="maxRedirects" placeholder="maxRedirects">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="socketTimeout" class="col-sm-2 control-label">socketTimeout</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="socketTimeout" placeholder="socketTimeout">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="connectTimeout" class="col-sm-2 control-label">connectTimeout</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="connectTimeout" placeholder="connectTimeout">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="reuseAddress" class="col-sm-2 control-label">reuseAddress</label>
	                      <div class="col-sm-10">
	                        <div class="checkbox">
	                          <label>
	                            <input type="checkbox" id="reuseAddress">
	                          </label>
	                        </div>
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="tcpNoDelay" class="col-sm-2 control-label">tcpNoDelay</label>
	                      <div class="col-sm-10">
	                        <div class="checkbox">
	                          <label>
	                            <input type="checkbox" id="tcpNoDelay">
	                          </label>
	                        </div>
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="keepAlive" class="col-sm-2 control-label">keepAlive</label>
	                      <div class="col-sm-10">
	                        <div class="checkbox">
	                          <label>
	                            <input type="checkbox" id="keepAlive">
	                          </label>
	                        </div>
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="maxParallel" class="col-sm-2 control-label">maxParallel</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="maxParallel" placeholder="maxParallel">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="maxPersistentConnections" class="col-sm-2 control-label">maxPersistentConnections</label>
	                      <div class="col-sm-10">
	                        <input type="text" class="form-control" id="maxPersistentConnections" placeholder="maxPersistentConnections">
	                      </div>
	                    </div>
	                    <div class="form-group">
	                      <label for="followRedirects" class="col-sm-2 control-label">followRedirects</label>
	                      <div class="col-sm-10">
	                        <div class="checkbox">
	                          <label>
	                            <input type="checkbox" id="followRedirects">
	                          </label>
	                        </div>
	                      </div>
	                    </div> 
	                  </div><!-- /.box-body -->
                    </form>
	              </div><!-- /.box -->
	              
            </div>
	</div>

</#macro>

<@display_page/>