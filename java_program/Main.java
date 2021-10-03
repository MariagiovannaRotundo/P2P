package mayor;

import java.io.IOException;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class Main {

	public static void main(String[] args) {
		
		//connect to the network (ganache)
		Web3j web3 = Web3j.build(new HttpService("http://localhost:8545"));
		
		try {
			System.out.println("Listening: "+web3.netListening().send().isListening());
		} catch (java.net.ConnectException e) {
			//failed to connect
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//start the grapical interface
		Gui gui = new Gui(web3);
		
		
	}

}
