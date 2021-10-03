package mayor;

import java.awt.*;

import javax.swing.*;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.math.BigDecimal;
import java.math.BigInteger;


public class Gui implements ActionListener{

	//path where find the images used in the GUI
	String path = "./images/";
	
	
	Logic logic;
	
	JFrame f;
	
	JTextField key, address, candidateSoul, sigilVote, symbolVote;
	JTextField soulVote, sigil, symbol, soul, readSoul,resultSoul;
	
	JTextArea Response, Response1, ResponseOpen, ResponseResult, ResponseVote, ResponseRefund;
	JLabel open, cast, missing, soulNum, missingVotesLabel, missingOpenLabel, yourEther;
	
	int numCandidatesValue, quorumNum;
	
	//constructor
	public Gui(Web3j web3){	
		
		logic = new Logic(web3);
		this.Start();
		
	}
	
	
	//initial interface: this function defines the GUI
	public void Start(){
		f=new JFrame("Elections");
		Container c=f.getContentPane();
		
		JPanel p0=new JPanel();
		p0.setBackground(Color.WHITE);
		p0.setLayout(new BorderLayout());
		
		
		JLabel electionsLabel=new JLabel("Elections!");
		electionsLabel.setFont(new Font("sansserif",Font.PLAIN,60));
		electionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel pcenter=new JPanel();
		pcenter.setBackground(Color.WHITE);
		pcenter.setLayout(new GridLayout(7,1,20,10));
		
		JLabel empty1=new JLabel("                             ");
		empty1.setFont(new Font("sansserif",Font.PLAIN,40));
		JLabel empty2=new JLabel("                             ");
		empty2.setFont(new Font("sansserif",Font.PLAIN,40));
		
		JLabel keyLabel=new JLabel("Insert your private key");
		keyLabel.setFont(new Font("sansserif",Font.PLAIN,40));
		keyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel addressLabel=new JLabel("Insert the contract address");
		addressLabel.setFont(new Font("sansserif",Font.PLAIN,40));
		addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
		key=new JTextField("",20);	
		key.setFont(new Font("sansserif",Font.PLAIN,30));
		key.setHorizontalAlignment(SwingConstants.CENTER);
		address=new JTextField("",20);
		address.setFont(new Font("sansserif",Font.PLAIN,30));
		address.setHorizontalAlignment(SwingConstants.CENTER);

		Response = new JTextArea("",2,30);
		Response.setBackground(Color.WHITE);
		Response.setForeground(Color.RED);
		Response.setEditable(false);
		Response.setFont(new Font("sansserif",Font.PLAIN,20));
		Response.setLineWrap(true);
		Response.setAutoscrolls(false);
		
		JScrollPane scroll=new JScrollPane(Response);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setPreferredSize( new Dimension(0,15) );
		scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
		
		
		pcenter.add(empty1);
		pcenter.add(keyLabel);
		pcenter.add(key);
		pcenter.add(addressLabel);
		pcenter.add(address);
		pcenter.add(empty2);
		pcenter.add(scroll);
		
		
		JButton b1=new JButton("Confirm");
		b1.setFont(new Font("sansserif",Font.PLAIN,30));
		
		p0.add(electionsLabel, BorderLayout.NORTH);
		p0.add(pcenter, BorderLayout.CENTER);
		p0.add(b1, BorderLayout.SOUTH);
		
		c.add(p0);
				
		f.setSize(905,600);
		f.setLocation(550, 5);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setResizable(false);
		f.setIconImage(new ImageIcon(path + "eth_logo.jpg").getImage());
		
		//to associate a command to the button
		b1.setActionCommand("confirm");
		b1.addActionListener(this);
		
	}
	
	
	//interface for the elections (to vote, ..)
	public void home(){
		f.dispose();
		f=new JFrame("Elections");
		Container c=f.getContentPane();
		
		
		JTabbedPane tab=new JTabbedPane();
		tab.setTabPlacement(JTabbedPane.NORTH);
		
		//panels of the tabs
		JPanel ptab1=new JPanel();
		JPanel ptab2=new JPanel();
		JPanel ptab3=new JPanel();
		JPanel ptab4=new JPanel();
		JPanel ptab5=new JPanel();

		tab.addTab("", new ImageIcon(path+"omino2.png"), ptab1, "candidates");
		tab.addTab("", new ImageIcon(path+"busta.png"), ptab2, "voting");
		tab.addTab("", new ImageIcon(path+"vote1.png"), ptab3, "opening");
		tab.addTab("", new ImageIcon(path+"medaglia.png"), ptab4, "results");
		tab.addTab("", new ImageIcon(path+"refund.png"), ptab5, "refund");
		
		
		//FIRST TAB'S PANEL 
		
		ptab1.setBackground(Color.WHITE);
		ptab1.setLayout(new BorderLayout());
		
		JPanel pnorth = new JPanel();
		pnorth.setBackground(Color.WHITE);
		pnorth.setLayout(new BorderLayout());
		
		JLabel address=new JLabel("Your address: "+ logic.getAddress());
		address.setFont(new Font("sansserif",Font.PLAIN,25));
		address.setHorizontalAlignment(JTextField.CENTER);
		
		BigDecimal etherValue = logic.getYoutEther();
		yourEther=new JLabel("Your balance: "+ etherValue);
		yourEther.setFont(new Font("sansserif",Font.PLAIN,25));
		yourEther.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel pinfo = new JPanel();
		pinfo.setBackground(Color.WHITE);
		pinfo.setLayout(new GridLayout(2,2,10,20));
		
		quorumNum = logic.getQuorum().intValue();
		if(logic.getCandidates()!=null){
			numCandidatesValue = logic.getNumCandidates();
		}else{
			numCandidatesValue = -1;
		}
		
		int soulNumValue = logic.getTransfersCandidates().intValue();
		int missingValue = numCandidatesValue - soulNumValue;
		
		JLabel quorum = new JLabel("Quorum: "+quorumNum);
		quorum.setFont(new Font("sansserif",Font.PLAIN,25));
		quorum.setHorizontalAlignment(JTextField.CENTER);
		soulNum = new JLabel("Num soul transfers: "+soulNumValue);
		soulNum.setFont(new Font("sansserif",Font.PLAIN,25));
		soulNum.setHorizontalAlignment(JTextField.CENTER);
		JLabel candidates = new JLabel("Num candidates: "+numCandidatesValue);
		candidates.setFont(new Font("sansserif",Font.PLAIN,25));
		candidates.setHorizontalAlignment(JTextField.CENTER);
		missing = new JLabel("Missing transfers: "+missingValue);
		missing.setFont(new Font("sansserif",Font.PLAIN,25));
		missing.setHorizontalAlignment(JTextField.CENTER);
		
		pinfo.add(quorum);
		pinfo.add(candidates);
		pinfo.add(soulNum);
		pinfo.add(missing);
		
		pnorth.add(address, BorderLayout.NORTH);
		pnorth.add(yourEther, BorderLayout.CENTER);
		pnorth.add(pinfo, BorderLayout.SOUTH);
		
		JPanel pcandidate = new JPanel();
		pcandidate.setBackground(Color.WHITE);
		
		JLabel empty1=new JLabel("                                         ");
		empty1.setFont(new Font("sansserif",Font.PLAIN,40));
		
		JLabel candidate=new JLabel("You are a candidate! Sent your soul (Ether)");
		candidate.setFont(new Font("sansserif",Font.PLAIN,30));
		JButton sentSoulButton=new JButton("Sent your soul!");
		sentSoulButton.setFont(new Font("sansserif",Font.PLAIN,30));
		candidateSoul = new JTextField("", 30);
		candidateSoul.setFont(new Font("sansserif",Font.PLAIN,30));
		candidateSoul.setHorizontalAlignment(JTextField.CENTER);
		
		JTextArea TAcandidate=new JTextArea(9,30);
		TAcandidate.setEditable(false);
		TAcandidate.setAutoscrolls(false);
		TAcandidate.setText("\n\nCandidates: \n"+logic.getAllCandidates());
		TAcandidate.setCaretPosition(0);
		TAcandidate.setFont(new Font("serif",Font.PLAIN,30));
		
		JScrollPane scroll=new JScrollPane(TAcandidate);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setPreferredSize( new Dimension(0,15) );
		scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
		
		Response1 = new JTextArea("",2,45);
		Response1.setBackground(Color.WHITE);
		Response1.setForeground(Color.RED);
		Response1.setEditable(false);
		Response1.setFont(new Font("sansserif",Font.PLAIN,20));
		Response1.setLineWrap(true);
		Response1.setAutoscrolls(false);
		
		JScrollPane scrollResponse1=new JScrollPane(Response1);
		scrollResponse1.setBorder(BorderFactory.createEmptyBorder());
		scrollResponse1.getVerticalScrollBar().setPreferredSize( new Dimension(0,15) );
		scrollResponse1.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
		
		pcandidate.add(empty1);
		pcandidate.add(candidate);
		pcandidate.add(candidateSoul);
		pcandidate.add(sentSoulButton);
		pcandidate.add(scroll);
		pcandidate.add(scrollResponse1);
		
		JButton updateTab1=new JButton("Update info");
		updateTab1.setFont(new Font("sansserif",Font.PLAIN,30));
		
		ptab1.add(pnorth, BorderLayout.NORTH);
		ptab1.add(pcandidate, BorderLayout.CENTER);
		ptab1.add(updateTab1, BorderLayout.SOUTH);
		
		//for not allows to non candidates to click the button
		//for send soul as candidates
		if(logic.iamCandidate() != true){
			candidate.setText("You are not a candidate. Do not send your soul!");
			sentSoulButton.setEnabled(false);
		}
		
		
		
		//SECOND TAB'S PANEL
		
		ptab2.setBackground(Color.WHITE);
		ptab2.setLayout(new BorderLayout());
		
		//panel for the info about vote
		JPanel pVoteInfo = new JPanel();
		pVoteInfo.setBackground(Color.WHITE);
		pVoteInfo.setLayout(new GridLayout(1,2,0,0));
		
		int castNum = logic.getEnvelopes().intValue();
		cast = new JLabel("Envelopes: "+castNum);
		cast.setFont(new Font("sansserif",Font.PLAIN,25));
		cast.setHorizontalAlignment(JTextField.CENTER);
		int missingVotes = quorumNum - castNum;
		missingVotesLabel =new JLabel("Missing votes: "+missingVotes);
		missingVotesLabel.setFont(new Font("sansserif",Font.PLAIN,25));
		missingVotesLabel.setHorizontalAlignment(JTextField.CENTER);
		
		pVoteInfo.add(cast);
		pVoteInfo.add(missingVotesLabel);
		
		
		//panel to vote
		JPanel pvote = new JPanel();
		pvote.setBackground(Color.WHITE);
		
		JLabel vote=new JLabel("Vote for the new mayor! Sent your envelope");
		vote.setFont(new Font("sansserif",Font.PLAIN,30));
		
		sigilVote = new JTextField("sigil", 30);
		sigilVote.setFont(new Font("sansserif",Font.PLAIN, 30));
		sigilVote.setHorizontalAlignment(JTextField.CENTER);
		symbolVote = new JTextField("symbol", 30);
		symbolVote.setFont(new Font("sansserif",Font.PLAIN, 30));
		symbolVote.setHorizontalAlignment(JTextField.CENTER);
		soulVote = new JTextField("soul", 30);
		soulVote.setFont(new Font("sansserif",Font.PLAIN, 30));
		soulVote.setHorizontalAlignment(JTextField.CENTER);
		JButton voteButton=new JButton("Give your vote!");
		voteButton.setFont(new Font("sansserif",Font.PLAIN,30));
		
		ResponseVote = new JTextArea("",5,40);
		ResponseVote.setBackground(Color.WHITE);
		ResponseVote.setForeground(Color.RED);
		ResponseVote.setEditable(false);
		ResponseVote.setFont(new Font("sansserif",Font.PLAIN,25));
		ResponseVote.setLineWrap(true);
		
		pvote.add(vote);
		pvote.add(sigilVote);
		pvote.add(symbolVote);
		pvote.add(soulVote);
		pvote.add(voteButton);
		pvote.add(ResponseVote);
		
		
		//panel to read candidates' soul
		JPanel pread = new JPanel();
		pread.setBackground(Color.WHITE);
		
		JLabel readSoulLabel=new JLabel("Read soul of a candidate");
		readSoulLabel.setFont(new Font("sansserif",Font.PLAIN,30));
		
		readSoul = new JTextField("", 30);
		readSoul.setFont(new Font("sansserif",Font.PLAIN, 30));
		readSoul.setHorizontalAlignment(JTextField.CENTER);
		JButton readSoulButton=new JButton("Read the soul!");
		readSoulButton.setFont(new Font("sansserif",Font.PLAIN,30));
		resultSoul = new JTextField("", 30);
		resultSoul.setFont(new Font("sansserif",Font.PLAIN, 30));
		resultSoul.setHorizontalAlignment(JTextField.CENTER);
		resultSoul.setEditable(false);
		resultSoul.setBackground(Color.WHITE);
		resultSoul.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		

		pread.add(readSoulLabel);
		pread.add(readSoul);
		pread.add(readSoulButton);
		pread.add(resultSoul);
		
		
		JPanel pCenterVote = new JPanel();
		pCenterVote.setBackground(Color.WHITE);
		pCenterVote.setLayout(new GridLayout(2,1,0,0));
		
		pCenterVote.add(pvote);
		pCenterVote.add(pread);
		
		JButton updateTab2=new JButton("Update info");
		updateTab2.setFont(new Font("sansserif",Font.PLAIN,30));
		
		ptab2.add(pVoteInfo, BorderLayout.NORTH);
		ptab2.add(pCenterVote, BorderLayout.CENTER);
		ptab2.add(updateTab2, BorderLayout.SOUTH);
		
		
		
		//THIRD TAB'S PANEL: PANEL FOR THE OPEN ENVELOPE
		
		//pannello per aprire la busta del voto
		ptab3.setBackground(Color.WHITE);
		ptab3.setLayout(new BorderLayout());
		
		JPanel pOpenInfo = new JPanel();
		pOpenInfo.setBackground(Color.WHITE);
		pOpenInfo.setLayout(new GridLayout(1,2,0,0));
		
		int openNum = logic.getOpens().intValue();
		open = new JLabel("Open: "+openNum);
		open.setFont(new Font("sansserif",Font.PLAIN,25));
		open.setHorizontalAlignment(JTextField.CENTER);
		int missingOpens = quorumNum - openNum;
		missingOpenLabel =new JLabel("Missing open: "+missingOpens);
		missingOpenLabel.setFont(new Font("sansserif",Font.PLAIN,25));
		missingOpenLabel.setHorizontalAlignment(JTextField.CENTER);
		
		pOpenInfo.add(open);
		pOpenInfo.add(missingOpenLabel);
		
		JPanel pOpenCenter = new JPanel();
		pOpenCenter.setBackground(Color.WHITE);
		
		JLabel open=new JLabel("Open your envelope!");
		open.setFont(new Font("sansserif",Font.PLAIN, 30));
		sigil = new JTextField("sigil", 30);
		sigil.setFont(new Font("sansserif",Font.PLAIN, 30));
		sigil.setHorizontalAlignment(JTextField.CENTER);
		symbol = new JTextField("symbol", 30);
		symbol.setFont(new Font("sansserif",Font.PLAIN, 30));
		symbol.setHorizontalAlignment(JTextField.CENTER);
		soul = new JTextField("soul", 30);
		soul.setFont(new Font("sansserif",Font.PLAIN, 30));
		soul.setHorizontalAlignment(JTextField.CENTER);
		JButton openButton=new JButton("open your envelope!");
		openButton.setFont(new Font("sansserif",Font.PLAIN, 30));
		
		ResponseOpen = new JTextArea(30,30);
		ResponseOpen.setBackground(Color.WHITE);
		ResponseOpen.setForeground(Color.RED);
		ResponseOpen.setEditable(false);
		ResponseOpen.setFont(new Font("sansserif",Font.PLAIN,30));
		ResponseOpen.setLineWrap(true);
		
		pOpenCenter.add(open);
		pOpenCenter.add(sigil);
		pOpenCenter.add(symbol);
		pOpenCenter.add(soul);
		pOpenCenter.add(openButton);
		pOpenCenter.add(ResponseOpen);
		
		
		JButton updateTab3=new JButton("Update info");
		updateTab3.setFont(new Font("sansserif",Font.PLAIN,30));
		
		ptab3.add(pOpenInfo, BorderLayout.NORTH);
		ptab3.add(pOpenCenter, BorderLayout.CENTER);
		ptab3.add(updateTab3, BorderLayout.SOUTH);
		
		
		
		//FOURTH TAB'S PANEL: PANEL FOR THE RESULTS
		ptab4.setBackground(Color.WHITE);
	
		JButton results=new JButton("Get results");
		results.setFont(new Font("sansserif",Font.PLAIN, 30));
		
		ResponseResult = new JTextArea(30,40);
		ResponseResult.setBackground(Color.WHITE);
		ResponseResult.setForeground(Color.RED);
		ResponseResult.setEditable(false);
		ResponseResult.setFont(new Font("sansserif",Font.PLAIN,25));
		ResponseResult.setLineWrap(true);
		ResponseResult.setAutoscrolls(false);
		
		JScrollPane scrollResult=new JScrollPane(ResponseResult);
		scrollResult.setBorder(BorderFactory.createEmptyBorder());
		scrollResult.getVerticalScrollBar().setPreferredSize( new Dimension(0,15) );
		scrollResult.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
		
		ptab4.add(results);
		ptab4.add(scrollResult);
		
		
		//FIFTH TAB'S PANEL: PANEL FOR THE RESULTS
				
		ptab5.setBackground(Color.WHITE);
		
		JButton refund=new JButton("Ask for refund");
		refund.setFont(new Font("sansserif",Font.PLAIN, 30));
	
		ResponseRefund = new JTextArea(30,40);
		ResponseRefund.setBackground(Color.WHITE);
		ResponseRefund.setForeground(Color.RED);
		ResponseRefund.setEditable(false);
		ResponseRefund.setFont(new Font("sansserif",Font.PLAIN,25));
		ResponseRefund.setLineWrap(true);
		
		ptab5.add(refund);
		ptab5.add(ResponseRefund);
		
	
		//SETUP OF THE FRAME
	
		c.add(tab);
				
		f.setSize(905,1000);
		f.setLocation(550, 5);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setResizable(false);
		f.setIconImage(new ImageIcon(path + "eth_logo.jpg").getImage());
		
		
		//code to associate behavior to panels
		ptab1.addComponentListener(new UpdateTab1());
		ptab2.addComponentListener(new UpdateTab2());
		ptab3.addComponentListener(new UpdateTab3());
		
		//Code to associate a behavior to buttons
		sentSoulButton.setActionCommand("sentSoul");
		updateTab1.setActionCommand("updatetab1");
		voteButton.setActionCommand("vote");
		readSoulButton.setActionCommand("candidateSoul");
		updateTab2.setActionCommand("updatetab2");
		openButton.setActionCommand("open");
		updateTab3.setActionCommand("updatetab3");
		results.setActionCommand("results");
		refund.setActionCommand("refund");
		
		
		sentSoulButton.addActionListener(this);
		updateTab1.addActionListener(this);
		voteButton.addActionListener(this);
		readSoulButton.addActionListener(this);
		updateTab2.addActionListener(this);
		openButton.addActionListener(this);
		updateTab3.addActionListener(this);
		results.addActionListener(this);
		refund.addActionListener(this);
		
	}

		
		
		
	//Code for management of the graphical interface
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//behavior of the confirm button
		if("confirm".equals(e.getActionCommand())){
			//create the credentials associated to the private key
			Credentials credentials = logic.createCredential(key.getText().trim());
			if(credentials==null){
				System.out.println("Error with the private key");
				Response.setText("Error: check that the private key is correct");
			} else{
				String msg = logic.inizialize(credentials, address.getText().trim(), BigInteger.valueOf(6721975));
				if(msg.equals("complete")){
					this.home();
				}else{
					//if there are errors
					Response.setText(msg);
				}
			}
		}
		//behavior of the button to sent soul from candidates
		if("sentSoul".equals(e.getActionCommand())){
			//get the indicated amount of soul
			String msg = logic.sentSoul(candidateSoul.getText().trim());
			if(msg.equals("sent")){
				//update the information and gives a feedback
				candidateSoul.setText(logic.getSentSoul()+" Ether had been sent");
				candidateSoul.setForeground(Color.RED);
				int soulNumValue = logic.getTransfersCandidates().intValue();
				int missingValue = numCandidatesValue - soulNumValue;
				soulNum.setText("Num soul transfers: "+soulNumValue);
				missing.setText("Missing transfers: "+missingValue);
			}
			else{
				candidateSoul.setForeground(Color.BLACK);
				Response1.setText(msg);
			}
			//update the balance
			yourEther.setText("Your balance: "+ logic.getYoutEther());
		}
		//behavior of the button to update the first tab
		if("updatetab1".equals(e.getActionCommand())){
			
			candidateSoul.setForeground(Color.BLACK);
			candidateSoul.setText("");
			Response1.setText("");
			
			int soulNumValue = logic.getTransfersCandidates().intValue();
			int missingValue = numCandidatesValue - soulNumValue;
			soulNum.setText("Num soul transfers: "+soulNumValue);
			missing.setText("Missing transfers: "+missingValue);
		
			yourEther.setText("Your balance: "+ logic.getYoutEther());
			System.out.println("Updated");
		}
		//behavior of the vote button
		if("vote".equals(e.getActionCommand())){ 
			//check that parameters are valid
			if(!logic.isCandidate(symbolVote.getText().trim())){
				System.out.println("Error: inserted symbol is not valid");
				ResponseVote.setText("Error: inserted symbol is not valid");
			}
			else if(soulVote.getText().trim().equals("") || sigilVote.getText().trim().equals("")){
					System.out.println("Error: insert a positive soul/sigil");
					ResponseVote.setText("Error: insert a positive soul/sigil");
			} 
			else{
				//compute the envelope
				byte[] env = logic.computeEnvelope(sigilVote.getText().trim(), symbolVote.getText().trim(), soulVote.getText().trim());
				
				if(env == null){
					System.out.println("Error generating the envelope: check the parameters");
					ResponseVote.setText("Error generating the envelope: check the parameters");
				}else{
					//vote using the computed envelope
					String msg = logic.voteCandidate(env);
					
					if(msg.equals("voted")){
						ResponseVote.setText("You have voted!");
						int castNum = logic.getEnvelopes().intValue();
						int missing = quorumNum - castNum;
						cast.setText("Envelopes: "+castNum); 
						missingVotesLabel.setText("Missing votes: "+missing);
						
					} else {
						ResponseVote.setText(msg);
					}	
				}
			}
		}
		//behavior of the button to get the deposits
		if("candidateSoul".equals(e.getActionCommand())){ 
			//check validity of the symbol
			if(!logic.isCandidate(readSoul.getText().trim())){
				System.out.println("Error: inserted symbol is not valid");
				resultSoul.setText("Error: inserted symbol is not valid");
			}
			else {
				//get the deposit
				BigDecimal souldDeposited = logic.getDeposit(readSoul.getText().trim());
				if(souldDeposited.compareTo(BigDecimal.valueOf(-1)) == 0){
					resultSoul.setText("An error occurred");
					resultSoul.setForeground(Color.RED);
				}else{
					resultSoul.setText("Soul deposited (Ether) : "+souldDeposited);
					resultSoul.setForeground(Color.BLACK);
				}
			}
		}
		//behavior of the button to update the second tab
		if("updatetab2".equals(e.getActionCommand())){
			ResponseVote.setText("");
			
			int castNum = logic.getEnvelopes().intValue();
			int missingVotes = quorumNum - castNum;
			cast.setText("Envelopes: "+castNum);
			missingVotesLabel.setText("Missing votes: "+missingVotes);
		}
		//behavior of the open button
		if("open".equals(e.getActionCommand())){
			//check the validity of the parameters inserted
			if(!logic.isCandidate(symbol.getText().trim())){
				System.out.println("Error: inserted symbol is not valid");
				ResponseOpen.setText("Error: inserted symbol is not valid");
			}
			else if(soulVote.getText().trim().equals("") || sigilVote.getText().trim().equals("")){
				System.out.println("Error: insert a positive soul/sigil");
				ResponseVote.setText("Error: insert a positive soul/sigil");
			} 
			else{
				//open the envelope
				String msg = logic.openEnvelope(sigil.getText().trim(), symbol.getText().trim(), soul.getText().trim());
				
				if(msg.equals("open")){
					int openNum = logic.getOpens().intValue();
					open.setText("Open: "+openNum);
					int missing = quorumNum-openNum;
					missingOpenLabel.setText("Missing open: "+missing);
					ResponseOpen.setText("Your envelope has been opened! You have sent "+logic.getSentSoulOpen()+" Ether");
				}
				else{
					ResponseOpen.setText(msg);
				}
			}
		}
		//behavior of the button to update the third tab
		if("updatetab3".equals(e.getActionCommand())){

			int openNum = logic.getOpens().intValue();
			open.setText("Open: "+openNum);
			int missingOpens = quorumNum - openNum;
			missingOpenLabel.setText("Missing open: "+missingOpens);
			
			ResponseOpen.setText("");
		}
		//behavior of the results button
		if("results".equals(e.getActionCommand())){
			
			String msg = logic.results();
			if(msg.equals("checked") || msg.contains("results already checked")){
				if(logic.getWinner().equals(logic.getAddress())){
					ResponseResult.setText("You are the Winner!!!\nYou get a bonus of: "
					+logic.getWinnerBonus()+" Ether!\n\n"+logic.getAllCandidatesVotes());
				}else{
					ResponseResult.setText("Winner : "+ logic.getWinner() +"\n\n"+logic.getAllCandidatesVotes());
				}
				ResponseResult.setForeground(Color.BLACK);
				
			} else{
				ResponseResult.setText(msg);
				ResponseResult.setForeground(Color.RED);		
			}
		}
		//behavior of the refund button
		if("refund".equals(e.getActionCommand())){
			
			if(logic.getWinner().equals("no winner")){
				ResponseRefund.setText("No winner: cannot ask for the refund");
			}
			
			String msg = logic.askRefund();
			if(msg.contains("No refund available")){
				ResponseRefund.setText("No refund available");
			}
			else{
				System.out.println(msg);
				ResponseRefund.setText(msg);
			}
		}
	}

	
	//classes to update the tab 1, 2, and 3 when they are shown
	class UpdateTab1 implements ComponentListener{

		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentResized(ComponentEvent arg0) {}
		
		@Override
		public void componentShown(ComponentEvent arg0) {
	
			candidateSoul.setForeground(Color.BLACK);
			candidateSoul.setText("");
			Response1.setText("");
			
			int soulNumValue = logic.getTransfersCandidates().intValue();
			int missingValue = numCandidatesValue - soulNumValue;
			soulNum.setText("Num soul transfers: "+soulNumValue);
			missing.setText("Missing transfers: "+missingValue);
		
			yourEther.setText("Your balance: "+ logic.getYoutEther());
			System.out.println("Updated");
		}
	}
	
	class UpdateTab2 implements ComponentListener{

		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentResized(ComponentEvent arg0) {}
		
		@Override
		public void componentShown(ComponentEvent arg0) {
	
			ResponseVote.setText("");
			
			int castNum = logic.getEnvelopes().intValue();
			int missingVotes = quorumNum - castNum;
			cast.setText("Envelopes: "+castNum);
			missingVotesLabel.setText("Missing votes: "+missingVotes);
		}
	}
	
	class UpdateTab3 implements ComponentListener{

		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentResized(ComponentEvent arg0) {}
		
		@Override
		public void componentShown(ComponentEvent arg0) {
	
			int openNum = logic.getOpens().intValue();
			open.setText("Open: "+openNum);
			int missingOpens = quorumNum - openNum;
			missingOpenLabel.setText("Missing open: "+missingOpens);
			
			ResponseOpen.setText("");
		}
	}
	
}
