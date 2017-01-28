package nl.mwensveen.etereum.test.ethereumj;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigInteger;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.Transaction;
import org.ethereum.listener.EthereumListener.PendingTransactionState;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Class voor Transaction1.sol.
 * Test sending of ether/weis to a contract.
 * Transaction1.sol will accept ether. Transaction2 won't.
 *
 * @author Micha Wensveen
 */
public class Transaction1Test extends AbstractContractTest {
	// Amount to send
	private static final BigInteger ETHER_TO_SEND = BigInteger.valueOf(2);
	private static final BigInteger WEIS_TO_SEND = WEIS_IN_ETHER.multiply(ETHER_TO_SEND);

	@Test
	public void testContractCreation() {
		SolidityCallResult callFunction = contract.callFunction("value");
		Assert.assertEquals(BigInteger.valueOf(0L), callFunction.getReturnValues()[0]);

		callFunction = contract.callFunction("getSender");
		Assert.assertArrayEquals(blockChain.getSender().getAddress(), (byte[]) callFunction.getReturnValues()[0]);
	}

	@Test
	public void testSendEtherUsingTransaction() throws InterruptedException {
		PendingListener pendingListener = new PendingListener();
		blockChain.addEthereumListener(pendingListener);
		PendingStateImpl pendingState = (PendingStateImpl) blockChain.getBlockchain().getPendingState();
		blockChain.createBlock();
		pendingListener.onBlock.poll(5, SECONDS);

		Assert.assertEquals(BigInteger.ZERO, blockChain.getBlockchain().getRepository().getBalance(contract.getAddress()));
		BigInteger senderBalance = blockChain.getBlockchain().getRepository().getBalance(blockChain.getSender().getAddress());
		Assert.assertNotEquals(BigInteger.ZERO, senderBalance);

		long nonce = blockChain.getBlockchain().getRepository().getNonce(blockChain.getSender().getAddress()).longValue();
		System.out.println("Nonce: " + nonce);
		Transaction transaction = blockChain.createTransaction(nonce, contract.getAddress(), WEIS_TO_SEND.longValue(), new byte[0]);
		pendingState.addPendingTransaction(transaction);
		Assert.assertEquals(PendingTransactionState.NEW_PENDING, pendingListener.pollTxUpdateState(transaction));

		blockChain.createBlock();
		Assert.assertEquals(PendingTransactionState.PENDING, pendingListener.pollTxUpdateState(transaction));

		blockChain.submitTransaction(transaction);
		blockChain.createBlock();
		Assert.assertEquals(PendingTransactionState.INCLUDED, pendingListener.pollTxUpdateState(transaction));

		Assert.assertEquals(WEIS_TO_SEND, blockChain.getBlockchain().getRepository().getBalance(contract.getAddress()));
		BigInteger newSenderBalance = blockChain.getBlockchain().getRepository().getBalance(blockChain.getSender().getAddress());
		Assert.assertNotEquals(BigInteger.ZERO, newSenderBalance);
		Assert.assertTrue(newSenderBalance.compareTo(senderBalance) < 0);

		SolidityCallResult callFunction = contract.callFunction("value");
		Assert.assertEquals(WEIS_TO_SEND, callFunction.getReturnValue());

		nonce = blockChain.getBlockchain().getRepository().getNonce(blockChain.getSender().getAddress()).longValue();
		System.out.println("Nonce: " + nonce);
		transaction = blockChain.createTransaction(nonce, contract.getAddress(), WEIS_TO_SEND.longValue(), new byte[0]);
		// pendingState.addPendingTransaction(transaction);
		// blockChain.createBlock();
		blockChain.submitTransaction(transaction);
		blockChain.createBlock();

		Assert.assertEquals(WEIS_TO_SEND.multiply(BigInteger.valueOf(2)), blockChain.getBlockchain().getRepository().getBalance(contract.getAddress()));
	}

	@Test
	public void testSendEtherUsingsendEther() {
		blockChain.sendEther(contract.getAddress(), WEIS_TO_SEND);
		Assert.assertEquals(WEIS_TO_SEND, blockChain.getBlockchain().getRepository().getBalance(contract.getAddress()));
	}

	@Override
	String getSolidifyFileName() {
		return "transaction1.sol";
	}

}
