pragma solidity 0.8.4;

contract Mayor {
    
    // Structs, events, and modifiers
    
    // Store refund data
    struct Refund {
        uint soul;
        address symbol;
    }

    struct Vote {
        uint sumSouls;
        uint number;
        uint deposit;
    }
    
    // Data to manage the confirmation
    struct Conditions {
        uint32 count_deposit;
        uint32 quorum;
        uint32 envelopes_casted;
        uint32 envelopes_opened;
    }
    
    event NewMayor(address _candidate);
    event NoWinner(address _escrow);
    event EnvelopeCast(address _voter);
    event EnvelopeOpen(address _voter, uint _soul, address _symbol);
    event RefundObtained(uint _soul);
    event DepositSoul(address _candidate, uint _deposit);

    Conditions public voting_condition;

    bool check_results = false;
    bool available_refunds = false;

    uint totalSouls = 0;

    address payable[] candidates;
    address payable[] voters;
    address payable public winner;
    address payable public escrow;

    
    // Voting phase variables
    mapping(address => bytes32) envelopes;
    mapping(address => bool) opened;
    mapping(address => Vote) votes;
    mapping(address => Refund) souls;
    


    //only the candidates can send the deposit and only before the voting phase
    modifier canDeposit() {
        require(voting_condition.count_deposit < candidates.length, "Cannot send deposit, voting started!");
        _;   
    }

    // Someone can vote as long as the quorum is not reached
    // the deposit must be before voting
    modifier canVote() {
        require(voting_condition.count_deposit == candidates.length, "Cannot vote. Waiting souls from candidates");
        require(voting_condition.envelopes_casted < voting_condition.quorum, "Cannot vote now, voting quorum has been reached");
        _;   
    }
    
    // Envelopes can be opened only after receiving the quorum
    modifier canOpen() {
        require(voting_condition.envelopes_casted == voting_condition.quorum, "Cannot open an envelope, voting quorum not reached yet");
        _;
    }
    
    // The outcome of the confirmation can be computed as soon as all the casted envelopes have been opened
    modifier canCheckOutcome() {
        require(voting_condition.envelopes_opened == voting_condition.quorum, "Cannot check the winner, need to open all the sent envelopes");
        _;
    }

    // Refunds can be requested only after the winner and the losers are known.
    modifier canGetRefund() {
        require(available_refunds == true, "Cannot ask for the refund");
        _;
    }
    

    /// @notice The constructor only initializes internal variables
    /// @param _candidate (address) The address of the mayor candidate
    /// @param _escrow (address) The address of the escrow account
    /// @param _quorum (address) The number of voters required to finalize the confirmation
    constructor(address payable[] memory _candidate, address payable _escrow, uint32 _quorum) public {
        candidates = _candidate;
        escrow = _escrow;
        voting_condition = Conditions({count_deposit: 0, quorum: _quorum, envelopes_casted: 0, envelopes_opened: 0});
    }



    //function to manage the deposits by the candidates
    function deposit() canDeposit public payable{

        require(msg.value > 0, "No soul sent");

        //check that the msg.sender is a candidate
        bool isCandidate = false;

        for(uint i=0; i<candidates.length; i++){
            if(msg.sender == candidates[i]){
                isCandidate = true;
                break;
            }
        }
        
        require(isCandidate == true, "You are not a candidate");
        require(votes[msg.sender].deposit == 0, "You have already sent the soul");
        
        //increase the number of deposits
        voting_condition.count_deposit++;

        votes[msg.sender].deposit = msg.value;
        totalSouls += msg.value;

        emit DepositSoul(msg.sender, msg.value);
    }

    
    /// @notice Store a received voting envelope
    /// @param _envelope The envelope represented as the keccak256 hash of (sigil, symbol, soul) 
    function cast_envelope(bytes32 _envelope) canVote public {
        
        if(envelopes[msg.sender] == 0x0) // => NEW, update on 17/05/2021
            voting_condition.envelopes_casted++;

        envelopes[msg.sender] = _envelope;
        emit EnvelopeCast(msg.sender);
    }
    
    
    /// @notice Open an envelope and store the vote information
    /// @param _sigil (uint) The secret sigil of a voter
    /// @param _symbol (bool) The voting preference
    /// @dev The soul is sent as crypto
    /// @dev Need to recompute the hash to validate the envelope previously casted
    function open_envelope(uint _sigil, address _symbol) canOpen public payable {

        require(envelopes[msg.sender] != 0x0, "The sender has not casted any votes");
        
        //read the envelope associate to the user
        bytes32 _casted_envelope = envelopes[msg.sender];
        bytes32 _sent_envelope = 0x0;
        
        //compute the envelope
        _sent_envelope = keccak256(abi.encode(_sigil, _symbol, msg.value));
        
        require(_casted_envelope == _sent_envelope, "Sent envelope does not correspond to the one casted");

        //check that the envelope is not been already open
        require(opened[msg.sender] == false, "The sender has already open the envelope");
        
        //remember that the envelope is open
        opened[msg.sender] = true;

        //incremento il contatore delle buste aperte
        voting_condition.envelopes_opened++;

        //the soul associate to the candidate increase of msg.value
        votes[_symbol].sumSouls += msg.value;
        //the number of vote received by the candidate increases of 1
        votes[_symbol].number++;

        //the total soul received by the contract increase of msg.value
        totalSouls += msg.value;

        //add the voter to data sctructures to ask for the refund
        voters.push(payable(msg.sender));
        souls[msg.sender] = Refund(msg.value, _symbol);

        emit EnvelopeOpen(msg.sender, msg.value, _symbol);

    }
    
    
    /// @notice Either confirm or kick out the candidate. Refund the electors who voted for the losing outcome
    function mayor_or_not_mayor() canCheckOutcome public {

        require(check_results == false, "results already checked");

        check_results = true;

        uint maxSouls = 0;
        uint maxVotes = 0;

        //compute the winner
        for(uint i=0; i<candidates.length; i++){

            //if the candidate has received the maximum is the winner
            if (votes[candidates[i]].sumSouls > maxSouls) {
                winner = candidates[i];
                //update the maximum found
                maxSouls = votes[candidates[i]].sumSouls;
                maxVotes = votes[candidates[i]].number;
            }
            //if more candidates have received the same maximum souls
            //the number of votes are considered
            else if(votes[candidates[i]].sumSouls == maxSouls){
                //if the candidate has more votes, he wins
                if (votes[candidates[i]].number > maxVotes){
                    winner = candidates[i];
                    maxSouls = votes[candidates[i]].sumSouls;
                    maxVotes = votes[candidates[i]].number;
                }
                else if (votes[candidates[i]].number == maxVotes) {
                    //no one wins between these candidates
                    winner = escrow;
                }

            }
        }

        if (winner != escrow){
            //if there is a winner if receive the soul by the his voters and
            //the deposit of the other candidates
            uint winnerSouls = votes[winner].sumSouls;
            for(uint i=0; i<candidates.length; i++){
                if(candidates[i] != winner){
                    winnerSouls += votes[candidates[i]].deposit;
                }
            }

            winner.transfer(winnerSouls);
            //enable the refund phase for voters
            available_refunds = true;
            emit NewMayor(winner);
        }
        else{
            //there is not a winner: the refund phase is not enabled because all
            //the souls go to the escrow account
            escrow.transfer(totalSouls);
            emit NoWinner(escrow);
        }
               
    }
 
 
    //function to send refund to voters when refund is available (there is a winner)
    function ask_refund() canGetRefund public {

        //used to avoid the reentrancy problem
        require(souls[msg.sender].symbol != escrow, "No refund available");

        //if msg.sender voted a loser candidate
        if(souls[msg.sender].symbol != winner){
            uint amount = souls[msg.sender].soul;
            //souls[msg.sender].soul = 0;
            souls[msg.sender].symbol = escrow;

            payable(msg.sender).transfer(amount);
            emit RefundObtained(amount);
        }
        else{
            //msg.sender voted the winner. Receive a part of the deposit of the winner
            souls[msg.sender].symbol = escrow;
            //the deposit is divided in equals parts for the voters
            uint amount = votes[winner].deposit / votes[winner].number;

            payable(msg.sender).transfer(amount);
            emit RefundObtained(amount);
        }
    }


    /// @notice Compute a voting envelope
    /// @param _sigil (uint) The secret sigil of a voter
    /// @param _symbol (bool) The voting preference
    /// @param _soul (uint) The soul associated to the vote
    function compute_envelope(uint _sigil, address _symbol, uint _soul) public pure returns(bytes32) {
        return keccak256(abi.encode(_sigil, _symbol, _soul));
    }
    
    //function to array of candidates
    function getCandidates() public view returns(address payable[] memory) {
        return candidates;
    }

    //function to get the deposit of a specific candidate
    function getDeposit(address candidate) public view returns(uint) {
        return votes[candidate].deposit;
    }

    //function to get the info about votes for a specific candidate
    function getcandidateVote(address candidate) canCheckOutcome public view returns(Vote memory) {
        return votes[candidate];
    }

}