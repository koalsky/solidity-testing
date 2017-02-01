package nl.mwensveen.etereum.test.ethereumj;

import java.math.BigInteger;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Class voor SelfdDestruct.
 *
 * @author Micha Wensveen
 */
public class SelfDestructTest extends AbstractContractTest {
	// Amount to send
	private static final BigInteger ETHER_TO_SEND = BigInteger.valueOf(2);
	private static final BigInteger WEIS_TO_SEND = WEIS_IN_ETHER.multiply(ETHER_TO_SEND);

	@Test
	public void testContractCreation() {
		// send weis to manager. It will pass it on to the selfdestruct contract.
		SolidityCallResult callFunction = contract.callFunction(WEIS_TO_SEND.longValue(), "give");
		System.out.println(callFunction);
		Assert.assertTrue(callFunction.isSuccessful());

		// no balance on manager.
		BigInteger balance = blockChain.getBlockchain().getRepository().getBalance(contract.getAddress());
		Assert.assertEquals(BigInteger.ZERO, balance);
		// make sure we dit send wies. the value is stored.
		Object[] callConstFunction = contract.callConstFunction("value");
		Assert.assertEquals(WEIS_TO_SEND, callConstFunction[0]);

		// let the manager selfdestruct the SelfDestruct contract.
		callFunction = contract.callFunction("endContract");
		Assert.assertTrue(callFunction.isSuccessful());

		// now the manager should have balance
		balance = blockChain.getBlockchain().getRepository().getBalance(contract.getAddress());
		Assert.assertEquals(WEIS_TO_SEND, balance);

		// give some more weis.
		callFunction = contract.callFunction(WEIS_TO_SEND.longValue(), "give");
		System.out.println(callFunction);
		Assert.assertTrue(callFunction.isSuccessful());

		// now the manager should have same balance. We lost the ether.
		balance = blockChain.getBlockchain().getRepository().getBalance(contract.getAddress());
		Assert.assertEquals(WEIS_TO_SEND, balance);

		callFunction = contract.callFunction("endContract");
		System.out.println(callFunction);
		Assert.assertFalse(callFunction.isSuccessful());
	}

	@Override
	String getSolidifyFileName() {
		return "SelfDestructManager.sol";
	}

	@Override
	protected String getContractName() {
		return "SelfDestructManager";
	}
}
