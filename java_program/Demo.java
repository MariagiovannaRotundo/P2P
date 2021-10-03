package mayor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;


public class Demo {

	
	public static void main(String[] args) {

		//json file to use for read the private keys
		String path = "./keys.json";
		
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
		
		//parser JSON to read the private keys
		JSONParser parser = new JSONParser();
		JSONObject jsonObj=null;
		try {
			jsonObj = (JSONObject) parser.parse(readWholeFile(path));
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		JSONObject keysList = (JSONObject) jsonObj.get("private_keys");
		Object[] keysArray = keysList.values().toArray();
		
		//array with the private keys 
		String[] keys = Arrays.copyOf(keysArray, keysArray.length, String[].class);
		
		//start the first demo
		Demo1(web3, keys);
		//start the second demo
		Demo2(web3, keys);
	}

	
	public static void Demo1(Web3j web3, String[] keys){
		
		//this demo use 4 candidates, quorum = 5 and 7 people (first 8 accounts)
		//account 7 is the escrow
		
		Logic logic = new Logic(web3);
		
		//credentials used in this demo (the first 8)
		Credentials[] credentials = new Credentials[8];
		
		System.out.println(keys.length);
		
		//for each key, create a credential to interact with the smart contract
		for(int i=0;i<=7;i++){
			credentials[i] = logic.createCredential(keys[i]);
		}
		
		BigInteger gasPrice;
    	try {
    		// eth_gasPrice, returns the current price per gas in wei.
			gasPrice = web3.ethGasPrice().send().getGasPrice();
		} catch (IOException e) {
			//gasPrice = Convert.toWei("20", Unit.GWEI).toBigInteger();
			e.printStackTrace();
			e.getMessage();
			return;
		} 
    	
    	ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, BigInteger.valueOf(6721975));
		
    	//create the list of the candidates (the first 4 accounts)
    	List<String> candidatesList = new ArrayList<>();
    	for(int i=0;i<4;i++){
    		candidatesList.add(credentials[i].getAddress());
		}
    	
    	String contractAddress;
		
		try {
			//deploy the contract on the network
			Mayor contract = Mayor.deploy(web3, credentials[0], gasProvider, candidatesList, 
					credentials[7].getAddress(), BigInteger.valueOf(5)).send();
			
			//get the contract address
			contractAddress = contract.getContractAddress();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		
		//file log of the test
		try {
			//create the new file
			File resultsFile = new File("./demo1.txt");
			if (resultsFile.createNewFile()) {
				System.out.println("File created: " + resultsFile.getName());
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
			return;
		}
		
		FileWriter myWriter;
		 try {
			//create a file write to write on the file
			myWriter = new FileWriter("./demo1.txt");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
				
		 
		//create an instance of logic to interact with the smart contract
		Logic[] logics = new Logic[8];
		for(int i=0;i<=7;i++){
			logics[i] = new Logic(web3);
			//initialize the instance of Logic
			logics[i].inizialize(credentials[i], contractAddress, BigInteger.valueOf(6721975));
		}
		 
		 
		//start the test simulating buttons
		
		String s = "";
		 
		//get address and balance for each account
		for(int i=0;i<=7;i++){
			s+=logics[i].getAddress()+" has balance of Ether: "+logics[i].getYoutEther()+"\n";
		}
		s+="\n\n";
		
		s+="Expected error cannot vote: \n";
		s+= FakeButton.vote(logics[0], "2", logics[0].getAddress(), "7");
		s+="\n";
		
		s+="Expected error cannot open: \n";
		s+= FakeButton.open(logics[0], "2", logics[0].getAddress(), "3");
		s+="\n";
		
		s+="Expected error cannot get results: \n";
		s+= FakeButton.results(logics[0]);
		s+="\n";
		
		s+="Expected error cannot ask for refund: \n";
		s+= FakeButton.refund(logics[0]);
		s+="\n\n";
		
		
		//candidates send souls
		for(int i=0;i<4;i++){
			
			s+="Expected sent without error: \n";
			s+=FakeButton.sentSoul(logics[i], Integer.toString(i+2));
			
			s+="Expected read info without error: \n";
			s+=FakeButton.updatetab1(logics[i])+"\n";
		}
		
		//invalid vote
		s+="Expected error, soul not valid: \n";
		s+=FakeButton.vote(logics[0], "3", logics[0].getAddress(), "-7")+"\n";
		
		//voting
		for(int i=0;i<5;i++){
			
			s+="Expected voting without error: \n";
			if(i%2 == 0){
				s+=FakeButton.vote(logics[i], Integer.toString(i), candidatesList.get(1), "2");
			} else{
				s+=FakeButton.vote(logics[i], Integer.toString(i), candidatesList.get(2), "2");
			}
			
			s+="Expected read info about voting without error: \n";
			s+=FakeButton.updatetab2(logics[i])+"\n";
		}
		
		//open envelopes
		for(int i=0;i<5;i++){
			
			s+="Expected open without error: \n";
			if(i%2 == 0){
				s+=FakeButton.open(logics[i], Integer.toString(i), candidatesList.get(1), "2");
			} else{
				s+=FakeButton.open(logics[i], Integer.toString(i), candidatesList.get(2), "2");
			}
			
			s+="Expected read info about voting without error: \n";
			s+=FakeButton.updatetab3(logics[i])+"\n";
		}
		
		//results
		
		s+="\n\nExpected winner: "+candidatesList.get(1)+"\n\n";
		
		for(int i=0;i<5;i++){
			
			s+="Person:  "+logics[i].getAddress()+"\n";
			s+=FakeButton.results(logics[i])+"\n\n";
			
		}
		
		//ask for refund
		s+="\n\nRefund available:\n";
		
		for(int i=0;i<5;i++){
			
			s+="Person:  "+logics[i].getAddress()+"\n";
			s+=FakeButton.refund(logics[i])+"\n\n";
			
		}
		
		//get the final balance of each considered account
		s+="\nFinal balance:\n";
		for(int i=0;i<=7;i++){
			s+=logics[i].getAddress()+" has balance of Ether: "+logics[i].getYoutEther()+"\n";
		}
		
		
		System.out.println(s);
		
		//write on the file
		try {
			myWriter.write(s);
			myWriter.flush();
			myWriter.close();

			System.out.println("Write");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Close");
	}
	
	
	
	
	
	public static void Demo2(Web3j web3, String[] keys){
		
		//this demo use 4 candidates, quorum = 6 and 7 people (first 8 accounts)
		//account 7 is the escrow
		//this test want to test the use of escrow
		
		Logic logic = new Logic(web3);
		
		//create credentials
		Credentials[] credentials = new Credentials[8];
		
		System.out.println(keys.length);
		
		//create credential for each private key considered
		for(int i=0;i<=7;i++){
			credentials[i] = logic.createCredential(keys[i+10]);
		}
		
		 BigInteger gasPrice;
		
    	try {
    		// eth_gasPrice, returns the current price per gas in wei.
			gasPrice = web3.ethGasPrice().send().getGasPrice();
		} catch (IOException e) {
			//gasPrice = Convert.toWei("20", Unit.GWEI).toBigInteger();
			e.printStackTrace();
			e.getMessage();
			return;
		} 
    	
    	ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, BigInteger.valueOf(6721975));
		
    	//creation of the list of the candidates
    	List<String> candidatesList = new ArrayList<>();
    	for(int i=0;i<4;i++){
    		candidatesList.add(credentials[i].getAddress());
		}
    	
    	String contractAddress;
		
		try {
			//deploy of the contract
			Mayor contract = Mayor.deploy(web3, credentials[0], gasProvider, candidatesList, 
					credentials[7].getAddress(), BigInteger.valueOf(6)).send();
			
			//get the address of the contract
			contractAddress = contract.getContractAddress();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		
		//file log of the test
		try {
			//create the file demo2.txt
			File resultsFile = new File("./demo2.txt");
			if (resultsFile.createNewFile()) {
				System.out.println("File created: " + resultsFile.getName());
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
			return;
		}
		
		//define filewriter to write on the file demo2.txt
		FileWriter myWriter;
		 try {
			myWriter = new FileWriter("./demo2.txt");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
				
		 
		//create an instance of logic for each considered account 
		Logic[] logics = new Logic[8];
		for(int i=0;i<=7;i++){
			logics[i] = new Logic(web3);
			logics[i].inizialize(credentials[i], contractAddress, BigInteger.valueOf(6721975));
		}
		 
		 
		//start the test simulating buttons
		String s = "";
		 
		//read the addresses and the associated balance
		for(int i=0;i<=7;i++){
			s+=logics[i].getAddress()+" has balance of Ether: "+logics[i].getYoutEther()+"\n";
		}
		s+="\n\n";
		
		
		//candidates send souls
		for(int i=0;i<4;i++){
			
			s+="Expected sent without error: \n";
			s+=FakeButton.sentSoul(logics[i], Integer.toString(i+2));
			
			s+="Expected read info without error: \n";
			s+=FakeButton.updatetab1(logics[i])+"\n";
		}
		
		//voting
		for(int i=0;i<6;i++){
			
			s+="Expected voting without error: \n";
			if(i%2 == 0){
				s+=FakeButton.vote(logics[i], Integer.toString(i), candidatesList.get(1), "2");
			} else{
				s+=FakeButton.vote(logics[i], Integer.toString(i), candidatesList.get(2), "2");
			}
			
			s+="Expected read info about voting without error: \n";
			s+=FakeButton.updatetab2(logics[i])+"\n";
		}
		
		//open envelopes
		for(int i=0;i<6;i++){
			
			s+="Expected open without error: \n";
			if(i%2 == 0){
				s+=FakeButton.open(logics[i], Integer.toString(i), candidatesList.get(1), "2");
			} else{
				s+=FakeButton.open(logics[i], Integer.toString(i), candidatesList.get(2), "2");
			}
			
			s+="Expected read info about voting without error: \n";
			s+=FakeButton.updatetab3(logics[i])+"\n";
		}
		
		//results
		
		s+="\n\nExpected winner: no winner\n\n";
		
		for(int i=0;i<6;i++){
			
			s+="Person:  "+logics[i].getAddress()+"\n";
			s+=FakeButton.results(logics[i])+"\n\n";
			
		}
		
		//refund
		s+="\n\nRefund available:\n";
		
		for(int i=0;i<6;i++){
			
			s+="Person:  "+logics[i].getAddress()+"\n";
			s+=FakeButton.refund(logics[i])+"\n\n";
			
		}
		
		//get final balances
		s+="\nFinal balance:\n";
		for(int i=0;i<=7;i++){
			s+=logics[i].getAddress()+" has balance of Ether: "+logics[i].getYoutEther()+"\n";
		}
		
		
		System.out.println(s);
		
		//write on file the results
		try {
			myWriter.write(s);
			myWriter.flush();
			myWriter.close();

			System.out.println("Write");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Close");
	}
	
	

	
	//method used to read from the json file keys.json
	public static String readWholeFile(String filename){
		
		File file = new File(filename);
		
		String s = "";
		System.out.println(filename);
		
		//check if the file exists
		if(file.exists()) {	
			try{
				FileChannel inChannel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ);
				ByteBuffer buff = ByteBuffer.allocateDirect(3000);
				
				byte[] b = new byte[1];
			
				while ((inChannel.read(buff))!=-1){
					//read mode
					buff.flip();
					while (buff.hasRemaining()){
						b[0]=buff.get();
						s+=new String(b);	
					}
						            
					buff.clear();
				}
				inChannel.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		return s;
	}
	
}
