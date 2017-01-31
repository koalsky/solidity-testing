package nl.mwensveen.etereum.test.ethereumj;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.util.blockchain.SolidityCallResult;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Class
 */
public class DelegateContractTest {
	static StandaloneBlockchain blockChain;

	/**
	 * Initalize the Standalone blockchain
	 */
	@BeforeClass
	public static void initalize() {
		// need to modify the default Frontier settings to keep the blocks difficulty
		// low to not waste a lot of time for block mining
		SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
			@Override
			public BigInteger getMINIMUM_DIFFICULTY() {
				return BigInteger.ONE;
			}
		}));

		// Creating a blockchain which generates a new block for each transaction
		// just not to call createBlock() after each call transaction
		blockChain = new StandaloneBlockchain().withAutoblock(true);
		System.out.println("Creating first empty block (need some time to generate DAG)...");
		// warning up the block miner just to understand how long
		// the initial miner dataset is generated
		blockChain.createBlock();
	}

	@Test
	public void testBumpWithDelegate() throws IOException {
		SolidityContract contract1 = createContractFromFile("MainContract.sol");
		SolidityContract contract2 = createContractFromFile("SubContract.sol");
		contract1.callFunction("setDelegate", contract2.getAddress());

		Object[] callConstFunctionResult = contract1.callConstFunction("delegate", new Object[0]);
		Assert.assertArrayEquals(contract2.getAddress(), (byte[]) callConstFunctionResult[0]);

		SolidityCallResult callFunctionResult = contract1.callFunction("bumpCounter", BigInteger.TEN);
		System.out.println(callFunctionResult);
		callConstFunctionResult = contract1.callConstFunction("counter", new Object[0]);
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

	/**
	 * Creates a contract from file.
	 *
	 * @param solidifyFileName solidifyFileName
	 * @return de SolidityContract
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected SolidityContract createContractFromFile(String solidifyFileName) throws IOException {
		URL url = Resources.getResource(solidifyFileName);
		String contractSrc = Resources.toString(url, Charsets.UTF_8);
		System.out.println("Creating a contract...");
		System.err.println(contractSrc);
		// This compiles our Solidity contract, submits it to the blockchain
		// internally generates the block with this transaction and returns the
		// contract interface

		SolidityContract createdContract = blockChain.submitNewContract(contractSrc, new Object[0]);
		System.out.println("Contract created");
		System.out.println("on address: " + BaseEncoding.base16().encode(createdContract.getAddress()));
		System.out.println("ABI: " + createdContract.getABI());
		return createdContract;
	}

}
