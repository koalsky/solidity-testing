package nl.mwensveen.etereum.contract.api.test.ethereumj;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.adridadou.ethereum.EthereumFacade;
import org.adridadou.ethereum.blockchain.EthereumJTest;
import org.adridadou.ethereum.blockchain.EthereumProxy;
import org.adridadou.ethereum.blockchain.EthereumProxyEthereumJ;
import org.adridadou.ethereum.blockchain.TestConfig;
import org.adridadou.ethereum.converters.input.InputTypeHandler;
import org.adridadou.ethereum.converters.output.OutputTypeHandler;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.swarm.SwarmService;
import org.adridadou.ethereum.values.CompiledContract;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.ethereum.values.EthAddress;
import org.adridadou.ethereum.values.SoliditySource;
import org.ethereum.listener.EthereumListener;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Class based on EthereumProviderTest
 */
public class DelegateContractTest {
	private final EthereumJTest ethereumj = new EthereumJTest(TestConfig.builder().build());
	private final InputTypeHandler inputTypeHandler = new InputTypeHandler();
	private final OutputTypeHandler outputTypeHandler = new OutputTypeHandler();
	private final EthereumEventHandler handler = new EthereumEventHandler(ethereumj);
	private final EthereumProxy bcProxy = new EthereumProxyEthereumJ(ethereumj, handler, inputTypeHandler, outputTypeHandler);
	private final EthAccount sender = ethereumj.defaultAccount();
	private final EthereumFacade ethereum = new EthereumFacade(bcProxy, inputTypeHandler, outputTypeHandler, SwarmService.from(SwarmService.PUBLIC_HOST),
			SolidityCompiler.getInstance());

	@Before
	public void before() {
		handler.onSyncDone(EthereumListener.SyncState.COMPLETE);
	}

	@Test
	public void checkSuccessCase() throws IOException, ExecutionException, InterruptedException {
		// Main Contract
		SoliditySource contractSource = SoliditySource.from(Resources.toString(Resources.getResource("MainContract.sol"), Charsets.UTF_8));
		CompiledContract compiledContract = ethereum.compile(contractSource, "MainContract").get();
		EthAddress address = ethereum.publishContract(compiledContract, sender).get();
		MainContract proxy = ethereum.createContractProxy(compiledContract, address, sender, MainContract.class);
		// SubContract
		contractSource = SoliditySource.from(Resources.toString(Resources.getResource("SubContract.sol"), Charsets.UTF_8));
		compiledContract = ethereum.compile(contractSource, "SubContract").get();
		EthAddress address2 = ethereum.publishContract(compiledContract, sender).get();

		assertEquals(BigInteger.ZERO, proxy.getCounter());
		// set delegate on maincontract
		CompletableFuture<Void> completableFuture = proxy.setDelegate(address2);
		completableFuture.get();
		// call the bump
		CompletableFuture<Void> completableFuture2 = proxy.bumpCounter(BigInteger.TEN);
		completableFuture2.get();
		if (completableFuture2.isCompletedExceptionally()) {
			System.out.println(completableFuture2.get());
			Assert.fail();
		}
		assertEquals(BigInteger.TEN, proxy.getCounter());
	}

	private interface MainContract {
		CompletableFuture<Void> setDelegate(EthAddress delegate);

		CompletableFuture<Void> bumpCounter(BigInteger counter);

		BigInteger getCounter();

	}

}
