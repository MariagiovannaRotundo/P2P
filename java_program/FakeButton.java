package mayor;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.web3j.crypto.Credentials;

public class FakeButton {

	
	public FakeButton(){}
	
	//simulate the behavior of the confirm button
	public static String confirm(Logic logic, String privateKey, String contractAddress){
		
		Credentials credentials = logic.createCredential(privateKey);
		if(credentials==null){
			return "Error: check that the private key is correct\n";
		} else{
			String msg = logic.inizialize(credentials, contractAddress, BigInteger.valueOf(6721975));
			if(msg.equals("complete")){
				return "Autentication complete\n";
			}else{
				return msg;
			}
		}
	}
	
	//simulate the behavior of the sentSoul button
	public static String sentSoul(Logic logic, String candidateSoul){
	
		String msg = logic.sentSoul(candidateSoul);
		if(msg.equals("sent")){
			int soulNumValue = logic.getTransfersCandidates().intValue();
			int missingValue = logic.getNumCandidates() - soulNumValue;
			
			String r = logic.getSentSoul()+" Ether had been sent\nNum soul transfers: "+
				soulNumValue+ "\nMissing transfers: "+missingValue+"\n";
			
			return r;
		}
		else{
			return msg;
		}
		
	}
	
	//simulate the behavior of the update button of the first tab
	public static String updatetab1(Logic logic){
		
		int soulNumValue = logic.getTransfersCandidates().intValue();
		int missingValue = logic.getNumCandidates() - soulNumValue;
		
		String r="Update: Num soul transfers: "+soulNumValue+"\n"+
				"Update: Missing transfers: "+missingValue+"\n"+
				"Update: Your balance: "+ logic.getYoutEther()+"\n";
		return r;
	}
	
	//simulate the behavior of the vote button
	public static String vote(Logic logic, String sigilVote, String symbolVote, String soulVote){
		
		if(!logic.isCandidate(symbolVote)){
			return "Error: inserted symbol is not valid\n";
		}
		else if(soulVote.equals("") || Integer.parseInt(soulVote) <0){
			return "Error: insert a positive soul\n";
		} else {
			
			byte[] env = logic.computeEnvelope(sigilVote, symbolVote, soulVote);
			
			if(env == null){
				return"Error generating the envelope\n";
			}else{
				String msg = logic.voteCandidate(env);
				
				if(msg.equals("voted")){
					
					int castNum = logic.getEnvelopes().intValue();
					int missing = logic.getQuorum().intValue() - castNum;
					
					return "You have voted!\nEnvelopes: "+castNum+"\nMissing votes: "+missing+"\n";
					
				} else {
					return msg;
				}	
			}
		}
	}
	
	//simulate the behavior of the button to read the deposits
	public static String candidateSoul(Logic logic, String readSoul){
		if(!logic.isCandidate(readSoul)){
			return "Error: inserted symbol is not valid\n";
		}
		else {
			BigDecimal souldDeposited = logic.getDeposit(readSoul);
			if(souldDeposited.compareTo(BigDecimal.valueOf(-1)) == 0){
				return "An error occurred\n";
			}else{
				return "Soul deposited (Ether) : "+souldDeposited+"\n";
			}
		}
	}
	
	//simulate the behavior of the button to update the second tab
	public static String updatetab2(Logic logic){
		
		int castNum = logic.getEnvelopes().intValue();
		int missingVotes = logic.getQuorum().intValue() - castNum;
		return "Update: Envelopes: "+castNum+"\nUpdate: Missing votes: "+missingVotes+"\n";
	}
	
	//simulate the behavior of the open button
	public static String open(Logic logic,  String sigil, String symbol, String soul){
		
		if(!logic.isCandidate(symbol)){
			return "Error: inserted symbol is not valid\n";
		}
		else if(soul.equals("") || Integer.parseInt(soul) <0){
			return "Error: insert a positive soul\n";
		} 
		else{
			String msg = logic.openEnvelope(sigil, symbol, soul);
			
			if(msg.equals("open")){
				int openNum = logic.getOpens().intValue();
				int missing = logic.getQuorum().intValue()-openNum;
				
				String s = "Your envelope has been opened! You have sent "+
				logic.getSentSoulOpen()+" Ether\nOpen: "+openNum+
				"\nMissing open: "+missing+"\n";
				return s;
			}
			else{
				return msg;
			}
		}
	}

	
	//simulate the behavior of the button to update the third tab
	public static String updatetab3(Logic logic){
		int openNum = logic.getOpens().intValue();
		int missingOpens = logic.getQuorum().intValue() - openNum;
		
		return "Update: Open: "+openNum+"\nUpdate: Missing open: "+missingOpens+"\n";
	}

	//simulate the behavior of the button to get the results
	public static String results(Logic logic){
		String msg = logic.results();
		if(msg.equals("checked") || msg.contains("results already checked")){
			if(logic.getWinner().equals(logic.getAddress())){
				return"You are the Winner!!!\nYou get a bonus of: "
				+logic.getWinnerBonus()+" Ether!\n\n"+logic.getAllCandidatesVotes()+"\n";
			}else{
				return "Winner : "+ logic.getWinner() +"\n\n"+logic.getAllCandidatesVotes()+"\n";
			}
		} else{
			return msg;		
		}
	}
	
	//simulate the behavior of the refund button
	public static String refund(Logic logic){
		
		if(logic.getWinner().equals("no winner")){
			return "No winner: cannot ask for the refund\n";
		}
		
		String msg = logic.askRefund();
		if(msg.contains("No refund available\n")){
			return "No refund available\n";
		}
		else{
			return msg;
		}
	}
	
}
