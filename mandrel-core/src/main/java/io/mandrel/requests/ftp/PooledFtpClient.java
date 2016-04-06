package io.mandrel.requests.ftp;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.weakref.jmx.internal.guava.base.Throwables;

import com.google.common.net.HostAndPort;

@Data
public class PooledFtpClient {

	private final KeyedObjectPool<HostAndPort, FTPClient> pool;

	public PooledFtpClient(FtpClientConfiguration configuration) {
		this(new GenericKeyedObjectPool<>(new InnerObjectFactory(configuration)));
	}

	public PooledFtpClient(KeyedObjectPool<HostAndPort, FTPClient> pool) {
		this.pool = pool;
	}

	public FTPClient getResource(HostAndPort hostAndPort) {
		try {
			return pool.borrowObject(hostAndPort);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public void returnBrokenResource(HostAndPort hostAndPort, FTPClient resource) {
		returnBrokenResourceObject(hostAndPort, resource);
	}

	public void returnResource(HostAndPort hostAndPort, FTPClient resource) {
		returnResourceObject(hostAndPort, resource);
	}

	protected void returnBrokenResourceObject(HostAndPort hostAndPort, FTPClient resource) {
		try {
			pool.invalidateObject(hostAndPort, resource);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	protected void returnResourceObject(HostAndPort hostAndPort, FTPClient resource) {
		try {
			pool.returnObject(hostAndPort, resource);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public void destroy() {
		close();
	}

	public void close() {
		try {
			pool.close();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	@Accessors(chain = true, fluent = true)
	public static class InnerObjectFactory extends BaseKeyedPooledObjectFactory<HostAndPort, FTPClient> {

		private final FtpClientConfiguration configuration;

		@Override
		public FTPClient create(HostAndPort hostAndPort) throws Exception {

			FTPClient client = new FTPClient();
			client.setControlKeepAliveTimeout(300);

			FTPClientConfig config = new FTPClientConfig();
			client.configure(config);

			try {
				if (hostAndPort.getPort() > 0) {
					client.connect(hostAndPort.getHostText(), hostAndPort.getPort());
				} else {
					client.connect(hostAndPort.getHostText());
				}
				if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
					throw new IOException("Ftp error: " + client.getReplyCode());
				}
				if (StringUtils.isNotBlank(configuration.username())) {
					if (!client.login(configuration.username(), configuration.password())) {
						throw new IOException("Ftp error: " + client.getReplyCode());
					}
				}
				if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
					throw new IOException("Ftp error");
				}
			} catch (Exception e) {
				if (client.isConnected()) {
					client.disconnect();
				}
				throw e;
			}

			return client;
		}

		@Override
		public PooledObject<FTPClient> wrap(FTPClient value) {
			return new DefaultPooledObject<FTPClient>(value);
		}

		@Override
		public void destroyObject(HostAndPort hostAndPort, PooledObject<FTPClient> object) throws Exception {
			FTPClient client = object.getObject();
			client.logout();
			client.disconnect();
		}

		@Override
		public boolean validateObject(HostAndPort hostAndPort, PooledObject<FTPClient> object) {
			FTPClient client = object.getObject();
			try {
				client.sendNoOp();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}

	@Data
	@Accessors(chain = true, fluent = true)
	public static class FtpClientConfiguration {
		private String username;
		private String password;
		private int fileType = FTP.BINARY_FILE_TYPE;

		//		private int dataConnectionMode;
		//	private int dataTimeout;
		//	private int passivePort;
		//	private String passiveHost;
		//	private int activeMinPort;
		//	private int activeMaxPort;
		//	private InetAddress activeExternalHost;
		//	private InetAddress reportActiveExternalHost; // overrides activeExternalHost in EPRT/PORT commands
		//	/** The address to bind to on passive connections, if necessary. */
		//	private InetAddress passiveLocalHost;
		//
		//	private boolean remoteVerificationEnabled;
		//	private long restartOffset;
		//	private FTPFileEntryParserFactory parserFactory;
		//	private int bufferSize; // buffersize for buffered data streams
		//	private int sendDataSocketBufferSize;
		//	private int receiveDataSocketBufferSize;
		//	private boolean listHiddenFiles;
		//	private boolean useEPSVwithIPv4; // whether to attempt EPSV with an IPv4 connection
	}
}
