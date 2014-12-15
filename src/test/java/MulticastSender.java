import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastSender {
	public static void main(String[] args) {
		InetAddress ia = null;
		int port = 0;
		String s = "Here's some 群播資料\n";
		byte[] data = new byte[s.length()];

		// read address from the command line
		try {
			try {
				ia = InetAddress.getByName("224.0.0.1");
			} catch (UnknownHostException e) {
				System.err.println(args[0] + " is not a valid address");
				System.exit(1);
			}
			port = Integer.parseInt("80");
		} catch (Exception e) {
			System.err.println(e);
			System.err
					.println("Usage: java MulticastSender multicastaddress port");
			System.exit(1);
		}

		data = s.getBytes();
		DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);

		try {
			MulticastSocket ms = new MulticastSocket();

			// you can try to use different TTL value here for ms.
			// ms.setTimeToLive(16);

			ms.joinGroup(ia);
			for (int i = 1; i < 10; i++)
				ms.send(dp);
			ms.leaveGroup(ia);
			ms.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}