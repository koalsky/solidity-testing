package nl.mwensveen.etereum.test.ethereumj;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.util.blockchain.SolidityContract;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Abstract base class for testing contracts with the StandAlone Bockchain.
 * Initialization based on {@link org.ethereum.samples.StandaloneBlockchainSample).
 *
 * @see keys-addr.txt (in ethereumj-core) for accounts that are created by default (using genesis-light-sb.json)
 * @author Micha Wensveen
 */
public abstract class AbstractContractTest {
	protected static final BigInteger WEIS_IN_ETHER = BigInteger.valueOf(1_000_000_000_000_000_000L);
	static StandaloneBlockchain blockChain;
	SolidityContract contract;

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

	@Before
	public void setup() throws IOException {
		// create the contract to be tested.
		String solidifyFileName = getSolidifyFileName();
		contract = createContractFromFile(solidifyFileName);
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

		SolidityContract createdContract = blockChain.submitNewContract(contractSrc, getConstructorArgs());
		System.out.println("Contract created");
		System.out.println("on address: " + BaseEncoding.base16().encode(createdContract.getAddress()));
		System.out.println("ABI: " + createdContract.getABI());
		return createdContract;
	}

	/**
	 * Get the arguments needed to construct the contracts.
	 *
	 * @return Object[]
	 */
	Object[] getConstructorArgs() {
		return new Object[0];
	}

	/**
	 * Get the filename of the Solidity contract.
	 *
	 * @return name.
	 */
	abstract String getSolidifyFileName();

}
