package nl.mwensveen.etereum.test.ethereumj;

import java.io.IOException;
import java.math.BigInteger;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Class voor ArrayContract.sol.
 * ArrayContract.sol is copied from {@link http://solidity.readthedocs.io/en/develop/types.html}
 *
 * @author Micha Wensveen
 */
public class ArrayContractTest extends AbstractContractTest {

	@Test
	public void test() throws IOException {
		SolidityCallResult callFunction = contract.callFunction("addFlag", ((Object) new Boolean[] { true, false }));
		Object[] returnValues = callFunction.getReturnValues();
		Assert.assertEquals(BigInteger.ONE, returnValues[0]);

		callFunction = contract.callFunction("addFlag", ((Object) new Boolean[] { true, false }));
		returnValues = callFunction.getReturnValues();
		Assert.assertEquals(BigInteger.valueOf(2), returnValues[0]);

		Object[] callConstFunctionResult = contract.callConstFunction("m_pairsOfFlags");
		Object actual = callConstFunctionResult[0];
		Assert.assertEquals(Boolean.TRUE, actual);

	}

	@Override
	String getSolidifyFileName() {
		return "ArrayContract.sol";
	}

}
