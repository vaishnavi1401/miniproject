package noobchain;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
//import com.google.gson.GsonBuilder;
import java.util.Map;
import java.util.Scanner;
public class NoobChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static ArrayList<Wallet> wallets = new ArrayList<Wallet>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	public static Wallet walletA;
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	public static Transaction genesisTransaction;
static Scanner sc=new Scanner (System.in);
	public static void main(String[] args) {	
		//add our blocks to the blockchain ArrayList:
		int choice,ch;
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

		walletA=new Wallet();
		Wallet coinbase = new Wallet();
		System.out.println("enter balance of  A");
		float balanceA=sc.nextFloat();
		//create genesis transaction, which sends 100 NoobCoin to walletA: 
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, balanceA, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
		genesisTransaction.transactionId = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		Block block1 = null;
		wallets.add(walletA);
	
		do {
		System.out.println("eeter 1 for new wallet,2 for transcation and 3 to view transaction");
		//Create wallets:
		choice=sc.nextInt();
		if(choice==1)
		{
Wallet B = new Wallet();		
			
			//testing
			//addBlock(new Block(s1,blockchain.get(blockchain.size()-1).hash));

			 block1 = new Block(blockchain.get(blockchain.size()-1).hash);
			 wallets.add(B);
	
		}
		if(choice ==2)
		{
			Wallet walletB=wallets.get(1);
			System.out.println("\nWalletA's balance is: " + walletA.getBalance());

			System.out.println("enter funds to be sent to B");
			float balance=sc.nextFloat();
			System.out.println("\nWalletA is Attempting to send funds  to WalletB...");
			block1.addTransaction(walletA.sendFunds(walletB.publicKey, balance));
			addBlock(block1);
			System.out.println("\nWalletA's balance is: " + walletA.getBalance());
			System.out.println("WalletB's balance is: " + walletB.getBalance());
		}
		ch=sc.nextInt();
		}while(ch!=0);
/*
		}
				
		Block block3 = new Block(block1.hash);

		System.out.println("enter funds to be sent to B");
		balance=sc.nextFloat();
		System.out.println("\nWalletB is Attempting to send funds to WalletA...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, balance));
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		*/
		isChainValid();


for(int i=0;i<blockchain.size();i++)
	System.out.println(blockchain.get(i));

for(int i=0;i<wallets.size();i++)
	System.out.println(wallets.get(i));
		  String blockchainJson = StringUtil.getJson(blockchain);
		  System.out.println("\nThe block chain: ");
		 System.out.println(blockchainJson);
		 		
	}
	
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}


