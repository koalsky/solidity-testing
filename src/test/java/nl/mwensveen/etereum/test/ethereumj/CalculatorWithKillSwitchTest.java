package nl.mwensveen.etereum.test.ethereumj;

import java.math.BigInteger;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.junit.Assert;
import org.junit.Test;

public class CalculatorWithKillSwitchTest extends AbstractContractTest {

	@Test
	public void testAdd() {
		// call the add function
		SolidityCallResult callResult = contract.callFunction("add", BigInteger.valueOf(10));
		Assert.assertFalse(callResult.getExecutionSummary().isFailed());

		// value should be 10
		Object[] callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.TEN, callConstantResult[0]);

	}

	@Test
	public void testSubtract() {
		// call the subtract function
		SolidityCallResult callResult = contract.callFunction("subtract", BigInteger.valueOf(10));
		Assert.assertFalse(callResult.getExecutionSummary().isFailed());

		// value should be -10
		Object[] callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.valueOf(-10), callConstantResult[0]);

	}

	@Test
	public void testAddSubtract() {
		// call the add and subtract function
		contract.callFunction("add", BigInteger.valueOf(10));
		contract.callFunction("subtract", BigInteger.valueOf(3));

		// value should be 7
		Object[] callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.valueOf(7), callConstantResult[0]);

	}

	@Test
	public void testAddBlocked() {
		// call the add function
		SolidityCallResult callResult = contract.callFunction("add", BigInteger.valueOf(10));
		Assert.assertFalse(callResult.getExecutionSummary().isFailed());

		// value should be 10
		Object[] callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.TEN, callConstantResult[0]);

		// invoke the kill switch
		contract.callFunction("changeBlock", true);

		// call the add function should not work
		callResult = contract.callFunction("add", BigInteger.valueOf(10));
		Assert.assertTrue(callResult.getExecutionSummary().isFailed());

		// value should still be 10, but result() gives 0.
		callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.ZERO, callConstantResult[0]);
		callConstantResult = contract.callConstFunction("value");
		Assert.assertEquals(BigInteger.TEN, callConstantResult[0]);
	}

	@Test
	public void testSubtractBlocked() {
		// call the subtract function
		SolidityCallResult callResult = contract.callFunction("subtract", BigInteger.valueOf(10));
		Assert.assertFalse(callResult.getExecutionSummary().isFailed());

		// value should be -10
		Object[] callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.valueOf(-10), callConstantResult[0]);

		// invoke the kill switch
		contract.callFunction("changeBlock", true);

		// call the subtract function
		callResult = contract.callFunction("subtract", BigInteger.valueOf(10));
		Assert.assertTrue(callResult.getExecutionSummary().isFailed());

		// revoke the kill switch
		contract.callFunction("changeBlock", false);

		// call the subtract function
		callResult = contract.callFunction("subtract", BigInteger.valueOf(8));

		// value should be *-18
		callConstantResult = contract.callConstFunction("result");
		Assert.assertEquals(BigInteger.valueOf(-18), callConstantResult[0]);
	}

	@Override
	String getSolidifyFileName() {
		return "Calculator.sol";
	}

}
