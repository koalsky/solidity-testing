package nl.mwensveen.etereum.test.ethereumj;

import java.io.IOException;
import java.math.BigInteger;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.ethereum.util.blockchain.SolidityContract;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

/**
 * Test Class voor ArrayContract.sol.
 * ArrayContract.sol is copied from {@link http://solidity.readthedocs.io/en/develop/types.html}
 *
 * @author Micha Wensveen
 */
public class DelegateTest extends AbstractContractTest {

	@Test
	public void testInit() throws IOException {
		Object[] callConstFunctionResult = contract.callConstFunction("delegate", new Object[0]);
		Assert.assertEquals("0000000000000000000000000000000000000000", Hex.toHexString((byte[]) callConstFunctionResult[0]));

	}

	@Test
	public void testSetDelegate() throws IOException {
		SolidityContract contract2 = createContractFromFile("SubContract.sol");
		contract.callFunction("setDelegate", contract2.getAddress());

		Object[] callConstFunctionResult = contract.callConstFunction("delegate", new Object[0]);
		Assert.assertArrayEquals(contract2.getAddress(), (byte[]) callConstFunctionResult[0]);
	}

	@Test
	public void testBumpWithoutDelegate() {
		SolidityCallResult callFunctionResult = contract.callFunction("bumpCounter", new Object[0]);
		System.out.println("no delegate");
		System.out.println(callFunctionResult);
		Assert.assertFalse(callFunctionResult.isSuccessful());

	}

	@Test
	public void testBumpWithDelegate() throws IOException {
		SolidityContract contract2 = createContractFromFile("SubContract.sol");
		contract.callFunction("setDelegate", contract2.getAddress());

		Object[] callConstFunctionResult = contract.callConstFunction("delegate", new Object[0]);
		Assert.assertArrayEquals(contract2.getAddress(), (byte[]) callConstFunctionResult[0]);

		SolidityCallResult callFunctionResult = contract.callFunction("bumpCounter", BigInteger.TEN);
		System.out.println("with delegate");
		System.out.println(callFunctionResult);
		callConstFunctionResult = contract.callConstFunction("counter", new Object[0]);
		Assert.assertEquals(BigInteger.TEN, callConstFunctionResult[0]);
	}

	@Test
	public void testBumpOnContract2() throws IOException {
		SolidityContract contract2 = createContractFromFile("SubContract.sol");

		SolidityCallResult callFunctionResult = contract2.callFunction("bumpCounter", BigInteger.TEN);
		System.out.println("on Contract2");
		System.out.println(callFunctionResult);
		Object[] callConstFunctionResult = contract2.callConstFunction("counter", new Object[0]);
		Assert.assertEquals(BigInteger.TEN, callConstFunctionResult[0]);
	}

	@Override
	String getSolidifyFileName() {
		return "MainContract.sol";
	}

}
