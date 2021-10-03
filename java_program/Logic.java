package mayor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import mayor.Mayor.RefundObtainedEventResponse;
import mayor.Mayor.Vote;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;


public class Logic {

	private Credentials credentials;
	private String contractAddress;
	
	private Web3j web3;
	private BigInteger gasPrice;
	private Mayor contract = null;
	
	private String[] candidates = null;
	
	private BigDecimal sentSoulCandidate = BigDecimal.ZERO;
	private BigDecimal sentSoulOpen = BigDecimal.ZERO;
	
	
	//constructor
	public Logic(Web3j web3){
		this.web3 = web3;
	}
	
	
	//create an object of type credentials associate to the private key
	public Credentials createCredential(String privateKey){
		try{
			return Credentials.create(privateKey);
		} catch (java.lang.NumberFormatException e){
			e.printStackTrace();
			return null;
		}
	}
	
	//get the address of the user
	public String getAddress(){
		return credentials.getAddress();
	}	
	
	//create and return an object of the Mayor class correspondent to the deployed contract 
	public String inizialize(Credentials credentials, String contractAddress, BigInteger gasLimit){
		
		this.credentials = credentials;
		this.contractAddress = contractAddress;
		
		// eth_gasPrice, returns the current price per gas in wei.
    	try {
			gasPrice = web3.ethGasPrice().send().getGasPrice();
		} catch (IOException e) {
			//gasPrice = Convert.toWei("20", Unit.GWEI).toBigInteger();
			e.printStackTrace();
			return e.getMessage();
		} 
    	
    	ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
    	
    	try {
    		//load("0x<address>", web3j, credentials, GAS_PRICE, GAS_LIMIT);
    		contract = Mayor.load(this.contractAddress, web3, this.credentials, gasProvider);
    		
    		//check that the contract is a valid contract
			if(contract.isValid() == false)
				return "Error with contract address: check that is the correct address";
		} catch (org.web3j.ens.EnsResolutionException | IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
    	
    	try {
    		//read the list of the candidates
			Object[] candidatesObj = contract.getCandidates().send().toArray();
			candidates = Arrays.copyOf(candidatesObj, candidatesObj.length, String[].class);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
    	
    	return "complete";
    	
	}
	
	
	//method to sent soul to the contract used by the candidates
	public String sentSoul(String soul){
		//checks parameters are valid
		if(soul.equals("")){
			return "Insert a valid amount of soul";
		}
		
		try {
			BigInteger soulToSent = Convert.toWei(soul, Unit.ETHER).toBigInteger();
			if(soulToSent.compareTo(BigInteger.ZERO) != 1)
				return "Insert a positive amount of soul";
			
			//call the function of the contract: deposit 
			TransactionReceipt transactionReceipt = contract.deposit(soulToSent).send();
			
			//read the ether deposited
			Transaction t =web3.ethGetTransactionByHash(transactionReceipt.getTransactionHash()).send().getTransaction().get();
			sentSoulCandidate =  Convert.fromWei(t.getValue().toString(), Unit.ETHER);
			System.out.println("Value "+sentSoulCandidate);
			
		} catch (java.lang.NumberFormatException e){
			e.printStackTrace();
			return "Insert a valid amount of soul";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
		return "sent";
	}
	
	//get the soul sent from the user that call this function
	public BigDecimal getSentSoul(){
		return sentSoulCandidate;
	}
	
	//get the balance of the user that call this function
	public BigDecimal getYoutEther(){
		BigInteger ethGetBalance = null;
		try {
			ethGetBalance = web3.ethGetBalance(
					this.getAddress(), DefaultBlockParameterName.LATEST
					).send().getBalance();
		} catch (IOException e) {
			e.printStackTrace();
			return BigDecimal.valueOf(-1);
		}
		BigDecimal ether = Convert.fromWei(ethGetBalance.toString(), Unit.ETHER);
		return ether;
	}
	
	//get how many candidates sent their soul
	public BigInteger getTransfersCandidates(){
		
		BigInteger num = BigInteger.valueOf(-1);
		
		try {
			num = contract.voting_condition().send().component1();
		} catch (Exception e) {
			e.printStackTrace();
			return num;
		}
		return num;
	}	
	
	//returns the array of candidates' addresses
	public String[] getCandidates(){
		return candidates;
	}	
	
	//get the number of candidates
	public int getNumCandidates(){
		return candidates.length;
	}	
	
	//get a string with all the candidates
	public String getAllCandidates(){
		
		String all="";
		for(int i=1;i<=candidates.length;i++){
			all += i+" - "+candidates[i-1]+"\n";
		}
		return all;
	}
	
	//check if the indicates address is of a candidate
	public boolean isCandidate(String candidate){
		
		if(this.candidates != null){	
			for(int i=0; i<candidates.length; i++){
				if(candidates[i].equals(candidate)){
					return true;
				}
			}
		}
		return false;
	}	
	
	//check if the user that call this function is a candidate
	public boolean iamCandidate(){
		
		if(this.candidates != null){	
			for(int i=0; i<candidates.length; i++){
				if(candidates[i].equals(this.getAddress())){
					return true;
				}
			}
		}
		return false;
	}
	
	//compute envelope to send
	public byte[] computeEnvelope(String sigil, String symbol, String soul){
		
		byte[] env = null;
		
		//check if the inserted address is of a candidate
		if(!this.isCandidate(symbol)){
			return null;
		}
		
		try {
			
			BigInteger sigilToSent = new BigInteger(sigil);
			BigDecimal soulRead = new BigDecimal(soul);

			//check the parameters are valid 
			if(sigilToSent.compareTo(BigInteger.ZERO)==-1 || soulRead.compareTo(BigDecimal.ZERO)==-1){
				return null;
			}
			
			//convert ether to wei
			BigInteger soulToSent = Convert.toWei(soul, Unit.ETHER).toBigInteger();
			
			//compute the envelope
			env = contract.compute_envelope(sigilToSent, symbol, soulToSent).send();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return env;
	}
	
	//vote the candidate
	public String voteCandidate(byte[] envelope){
		
		try {
			contract.cast_envelope(envelope).send();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return "voted";
	}
	
	//get the deposit of a candidate
	public BigDecimal getDeposit(String candidate){
		
		BigInteger deposit = BigInteger.valueOf(-1);
		
		//check that the inserted address is of a candidate
		if(!this.isCandidate(candidate)){
			return BigDecimal.valueOf(-1);
		}
		
		try {
			//get the deposit
			deposit = contract.getDeposit(candidate).send();
		} catch (Exception e) {
			e.printStackTrace();
			return BigDecimal.valueOf(-1);
		}
		//convert in ether
		return Convert.fromWei(deposit.toString(), Unit.ETHER);
	}	
	
	//get the number of received envelopes
	public BigInteger getEnvelopes(){
		
		BigInteger cast = BigInteger.valueOf(-1);
		
		try {
			cast = contract.voting_condition().send().component3();
		} catch (Exception e) {
			e.printStackTrace();
			return cast;
		}
		return cast;
	}	
	
	//open the envelope
	public String openEnvelope(String sigil, String symbol, String soul){
		//check that the symbol is of a candidate
		if(!this.isCandidate(symbol)){
			return "Insert a valid sigil, soul and symbol";
		}
		
		try {
			BigInteger sigilToSent = new BigInteger(sigil);
			BigDecimal soulRead = new BigDecimal(soul);
			
			if(sigilToSent.compareTo(BigInteger.ZERO)==-1 || soulRead.compareTo(BigDecimal.ZERO)==-1){
				return "Insert a valid sigil, soul and symbol";
			}
			
			BigInteger soulToSent = Convert.toWei(soul, Unit.ETHER).toBigInteger();
			
			TransactionReceipt transactionReceipt = contract.open_envelope(sigilToSent, symbol, soulToSent).send();
			//get the amount of sent ether
			Transaction t =web3.ethGetTransactionByHash(transactionReceipt.getTransactionHash()).send().getTransaction().get();
			sentSoulOpen =  Convert.fromWei(t.getValue().toString(), Unit.ETHER);
			System.out.println("Ether sent: "+sentSoulOpen);
			
			return "open";
		} catch (java.lang.NumberFormatException e){
			e.printStackTrace();
			return "Insert a valid sigil, soul and symbol";
		}catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
	}
	
	//get the amount if soul sent for the opening
	public BigDecimal getSentSoulOpen(){
		return sentSoulOpen;
	}
	
	//get the quorum
	public BigInteger getQuorum(){
		
		BigInteger quorum = BigInteger.valueOf(-1);
		try {
			quorum = contract.voting_condition().send().component2();
		} catch (Exception e) {
			e.printStackTrace();
			return quorum;
		}
		return quorum;
	}	
	
	//get the number of open envelopes
	public BigInteger getOpens(){
	
		BigInteger open = BigInteger.valueOf(-1);
		try {
			open = contract.voting_condition().send().component4();
		} catch (Exception e) {
			e.printStackTrace();
			return open;
		}
		return open;
	}	
	
	//ask for the results
	public String results(){

		try {
			TransactionReceipt results = contract.mayor_or_not_mayor().send();
			System.out.println(results.getLogs());
			return "checked";
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
	}
	
	//get the winner (if there is a winner)
	public String getWinner(){
		
		String winner = "";
		String escrow = "";
		
		try {
			winner = contract.winner().send();
			escrow = contract.escrow().send();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
		if (winner.equals(escrow))
			return "no winner";
		else
			return winner;
	}	
	
	//get the information about the votes received by the candidates
	public String getAllCandidatesVotes(){
		
		String all="N - Candidate - Soul - Votes\n";
		Vote v = null;
	
		for(int i=1;i<=candidates.length;i++){
			try {
				v = contract.getcandidateVote(candidates[i-1]).send();
			} catch (Exception e) {
				e.printStackTrace();
				return "Error";
			}
			BigDecimal soul=Convert.fromWei(v.sumSouls.toString(), Unit.ETHER);
			
			all += i+" - "+candidates[i-1]+" - "+soul+" - "+v.number+"\n";
		}
		return all;
	}
	
	//ask for the refund for the user that call this function
	public String askRefund(){
		
		try {
			
			TransactionReceipt transactionReceipt = contract.ask_refund().send();
			
			//get the amount of wei/ether of the refund
			List<RefundObtainedEventResponse> listRefundEvents = contract.getRefundObtainedEvents(transactionReceipt);
			
			System.out.println("size: "+listRefundEvents.size());
			if(listRefundEvents.size() == 1){
				RefundObtainedEventResponse refundEvent = listRefundEvents.get(0);
				
				System.out.println("Wei refund:"+refundEvent._soul);
				BigDecimal refund=Convert.fromWei(refundEvent._soul.toString(), Unit.ETHER);
				
				return "You have obtained "+refund + " Ether as refund. Check your balance";
			}
			
			//System.out.println(refundEvent.log.toString());
			return "More than one refund found";
			
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	//get the bonus received by the winner
	public BigDecimal getWinnerBonus(){
		
		BigInteger bonus = BigInteger.ZERO;
		Vote v = null;
	
		for(int i=0;i<candidates.length;i++){
			try {
				v = contract.getcandidateVote(candidates[i]).send();
			} catch (Exception e) {
				e.printStackTrace();
				return BigDecimal.valueOf(-1);
			}
			if(candidates[i].equals(this.getWinner())){
				bonus = bonus.add(v.sumSouls);
			}
			else{
				bonus = bonus.add(v.deposit);
			}
		}
		
		return Convert.fromWei(bonus.toString(), Unit.ETHER);
	}
	
	
}
