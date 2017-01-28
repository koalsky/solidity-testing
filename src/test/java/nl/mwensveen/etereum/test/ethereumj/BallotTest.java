package nl.mwensveen.etereum.test.ethereumj;

import java.io.IOException;
import java.math.BigInteger;
import org.ethereum.core.Account;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class BallotTest extends AbstractContractTest {

	@Test
	public void test() throws IOException {
		byte[] chairperson = (byte[]) contract.callConstFunction("chairperson")[0];
		ECKey sender = blockChain.getSender();
		Account senderAccount = new Account();
		senderAccount.init(sender);
		byte[] senderAddress = senderAccount.getAddress();
		Assert.assertArrayEquals(senderAddress, chairperson);

		System.out.println(Hex.toHexString(senderAccount.getAddress()));
		System.out.println(senderAccount.getEcKey().getPrivKey());
		System.out.println(blockChain.getBlockchain().getRepository().getBalance(senderAccount.getAddress()));

		// proposals is an Array: argument is the index.
		Object[] proposal = contract.callConstFunction("proposals", 0);
		// name
		Assert.assertEquals("a", new String((byte[]) proposal[0]).trim());
		// votecount
		Assert.assertEquals(BigInteger.valueOf(0), proposal[1]);

		Object[] proposalName = contract.callConstFunction("getProposalName", 0);
		Assert.assertEquals("a", new String((byte[]) proposalName[0]).trim());

	}

	@Test
	public void testVoting() {

		Account account1 = new Account();
		ECKey k = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
		account1.init(k);
		byte[] address1 = account1.getAddress();
		Assert.assertArrayEquals(Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c"), address1);

		Object[] callConstFunction = contract.callConstFunction("chairperson", new Object[0]);
		String chairmanAddress = Hex.toHexString((byte[]) callConstFunction[0]);
		Assert.assertEquals("5db10750e8caff27f906b41c71b3471057dd2004", chairmanAddress);
		SolidityCallResult callFunctionResult = contract.callFunction("giveRightToVote", address1);
		Assert.assertFalse(callFunctionResult.getExecutionSummary().isFailed());

		// check the map with voters
		Object[] callConstFunctionResult = contract.callConstFunction("voters", new Object[] { address1 });
		Assert.assertEquals(BigInteger.ONE, callConstFunctionResult[0]); // weight
		Assert.assertEquals(false, callConstFunctionResult[1]); // voted
		Assert.assertArrayEquals(new byte[20], (byte[]) callConstFunctionResult[2]); // delegate
		Assert.assertEquals(BigInteger.ZERO, callConstFunctionResult[3]);// vote

		// vote account1
		blockChain.setSender(k);
		contract.callFunction("vote", BigInteger.ONE);

		Object[] callConstFunction2 = contract.callConstFunction("votedAddresses", BigInteger.ZERO);
		Assert.assertEquals(Hex.toHexString(address1), Hex.toHexString((byte[]) callConstFunction2[0]));

		// check the map with voters
		callConstFunctionResult = contract.callConstFunction("voters", address1);
		Assert.assertEquals(BigInteger.ONE, callConstFunctionResult[0]); // weight
		Assert.assertEquals(true, callConstFunctionResult[1]); // voted
		Assert.assertArrayEquals(new byte[20], (byte[]) callConstFunctionResult[2]); // delegate
		Assert.assertEquals(BigInteger.ONE, callConstFunctionResult[3]);// vote

		// check the map with voters chairman
		callConstFunctionResult = contract.callConstFunction("voters", new Object[] { chairmanAddress });
		Assert.assertEquals(BigInteger.ONE, callConstFunctionResult[0]); // weight
		Assert.assertEquals(false, callConstFunctionResult[1]); // voted
		Assert.assertArrayEquals(new byte[20], (byte[]) callConstFunctionResult[2]); // delegate
		Assert.assertEquals(BigInteger.ZERO, callConstFunctionResult[3]);// vote
	}

	@Override
	String getSolidifyFileName() {
		return "Ballot.sol";
	}

	@Override
	Object[] getConstructorArgs() {
		// proposal names
		return new Object[] { new Object[] { "a", "b" } };
	}

}
