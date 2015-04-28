package io.mandrel;

import io.undertow.client.ClientCallback;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientExchange;
import io.undertow.client.ClientRequest;
import io.undertow.client.UndertowClient;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import io.undertow.util.Protocols;
import io.undertow.util.StringReadChannelListener;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.xnio.ByteBufferSlicePool;
import org.xnio.ChannelListeners;
import org.xnio.IoFuture;
import org.xnio.IoFuture.Status;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Pool;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.StreamSinkChannel;

public class MyTest {

	@Test
	public void undertow() {

		// Need dis
		URI url = URI.create("http://corvette:9200/_search");
		HeaderMap headers = new HeaderMap();
		String method = "POST";
		headers.add(HttpString.tryFromString("X-Tested-For"), "Test");

		ByteBuffer body = ByteBuffer.wrap("{}".getBytes());
		long contentLength = 2;

		// Add some magic
		long connectionTimeOutInMilliseconds = 5000;

		// Let's rock
		Pool<ByteBuffer> bufferPool = new ByteBufferSlicePool(1048, 1048);
		OptionMap optionMap = OptionMap.builder().set(Options.WORKER_IO_THREADS, 8).set(Options.TCP_NODELAY, true).set(Options.KEEP_ALIVE, true)
				.set(Options.WORKER_NAME, "HttpClient").getMap();
		XnioWorker worker;
		try {
			worker = Xnio.getInstance().createWorker(optionMap);
		} catch (IOException e1) {
			throw new RuntimeException("Well...");
		}

		UndertowClient client = UndertowClient.getInstance();

		IoFuture<ClientConnection> connect = client.connect(url, worker, bufferPool, optionMap);

		StringBuilder response = new StringBuilder();
		try {
			Status awaitInterruptibly = connect.awaitInterruptibly(connectionTimeOutInMilliseconds, TimeUnit.MILLISECONDS);
			if (Status.DONE.equals(awaitInterruptibly)) {

				ClientConnection clientConnection;
				try {
					clientConnection = connect.get();

					ClientRequest request = new ClientRequest().setMethod(HttpString.tryFromString(method)).setProtocol(Protocols.HTTP_1_1)
							.setPath(url.getPath());
					if (body != null) {
						request.getRequestHeaders().add(HttpString.tryFromString("Content-Length"), contentLength);
					}

					clientConnection.sendRequest(request, new ClientCallback<ClientExchange>() {
						@Override
						public void failed(IOException e) {
							onFailure(e);
						}

						@Override
						public void completed(ClientExchange exchange) {

							exchange.setResponseListener(new ClientCallback<ClientExchange>() {
								@Override
								public void completed(final ClientExchange result) {
									System.err.println("Response code: ");
									System.err.println(result.getResponse().getResponseCode());
									new StringReadChannelListener(result.getConnection().getBufferPool()) {
										@Override
										protected void stringDone(String result) {
											response.append(result);
										}

										@Override
										protected void error(IOException ex) {
											onFailure(ex);
										}
									}.setup(result.getResponseChannel());

								}

								@Override
								public void failed(IOException ex) {
									onFailure(ex);
								}
							});

							try {
								if (body != null) {
									exchange.getRequestChannel().write(body);
								}
								exchange.getRequestChannel().shutdownWrites();
								if (!exchange.getRequestChannel().flush()) {
									exchange.getRequestChannel().getWriteSetter()
											.set(ChannelListeners.<StreamSinkChannel> flushingChannelListener(null, null));
									exchange.getRequestChannel().resumeWrites();
								}
							} catch (IOException ex) {
								onFailure(ex);
							}
						}

						private void onFailure(IOException ex) {
							throw new RuntimeException(ex);
						}
					});

				} catch (CancellationException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			} else {
				throw new RuntimeException("Connection timeout");
			}
		} catch (InterruptedException e) {
			connect.cancel();
		}

		System.err.println("Response: ");
		System.err.println(response.toString());

	}
}
