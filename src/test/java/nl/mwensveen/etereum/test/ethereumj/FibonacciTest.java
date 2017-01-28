package nl.mwensveen.etereum.test.ethereumj;

import java.math.BigInteger;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Class voor fibonacci.sol.
 *
 * @author Micha Wensveen
 */
public class FibonacciTest extends AbstractContractTest {

	@Test
	public void testNumber0() {
		SolidityCallResult callFunction = contract.callFunction("getNumber", 0);
		Assert.assertEquals(BigInteger.ZERO, callFunction.getReturnValues()[0]);

		// length check
		Assert.assertEquals(BigInteger.ZERO, contract.callConstFunction("numbers", 2)[0]);
	}

	@Test
	public void testNumber1And2() {
		SolidityCallResult callFunction = contract.callFunction("getNumber", 1);
		Assert.assertEquals(BigInteger.ONE, callFunction.getReturnValues()[0]);
		callFunction = contract.callFunction("getNumber", 2);
		Assert.assertEquals(BigInteger.ONE, callFunction.getReturnValues()[0]);
		Assert.assertEquals(BigInteger.valueOf(2L), callFunction.getReturnValues()[1]);

		// length check
		Assert.assertEquals(BigInteger.ZERO, contract.callConstFunction("numbers", 2)[0]);
	}

	@Test
	public void testNumberFirstNumberToCalculate() {
		SolidityCallResult callFunction = contract.callFunction("getNumber", 3);
		Assert.assertEquals(BigInteger.valueOf(2L), callFunction.getReturnValues()[0]);
		Assert.assertEquals(BigInteger.valueOf(3L), callFunction.getReturnValues()[1]);

		// length check
		Assert.assertEquals(BigInteger.valueOf(2L), contract.callConstFunction("numbers", 2)[0]);
		Assert.assertEquals(BigInteger.ZERO, contract.callConstFunction("numbers", 3)[0]);
	}

	@Test
	public void testNumber10() {
		SolidityCallResult callFunction = contract.callFunction("getNumber", 10);
		Assert.assertEquals(BigInteger.valueOf(55L), callFunction.getReturnValues()[0]);
		Assert.assertEquals(BigInteger.valueOf(10L), callFunction.getReturnValues()[1]);

	}

	@Test
	public void testNumber64() {
		SolidityCallResult callFunction = contract.callFunction("getNumber", 64);
		Assert.assertEquals(BigInteger.valueOf(10_610_209_857_723l), callFunction.getReturnValues()[0]);
		Assert.assertEquals(BigInteger.valueOf(64L), callFunction.getReturnValues()[1]);
		// length check
		Assert.assertEquals(BigInteger.valueOf(10_610_209_857_723l), contract.callConstFunction("numbers", 63)[0]);
		Assert.assertEquals(BigInteger.ZERO, contract.callConstFunction("numbers", 64)[0]);

		// number 10: size should not change
		callFunction = contract.callFunction("getNumber", 10);
		Assert.assertEquals(BigInteger.valueOf(55L), callFunction.getReturnValues()[0]);
		Assert.assertEquals(BigInteger.valueOf(64L), callFunction.getReturnValues()[1]);

	}

	@Override
	String getSolidifyFileName() {
		return "Fibonacci.sol";
	}

}
