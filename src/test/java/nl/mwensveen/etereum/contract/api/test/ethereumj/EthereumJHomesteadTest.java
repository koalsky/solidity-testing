package nl.mwensveen.etereum.contract.api.test.ethereumj;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import org.adridadou.ethereum.blockchain.Ethereumj;
import org.adridadou.ethereum.blockchain.TestConfig;
import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.keystore.AccountProvider;
import org.adridadou.ethereum.values.EthAccount;
import org.adridadou.exception.EthereumApiException;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.util.blockchain.StandaloneBlockchain;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumJHomesteadTest implements Ethereumj {
	private final StandaloneBlockchain blockchain;
	private final TestConfig config;
	private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100);

	public EthereumJHomesteadTest(TestConfig config) {
		SystemProperties.getDefault().setBlockchainConfig(new HomesteadConfig(new HomesteadConfig.HomesteadConstants() {
			@Override
			public BigInteger getMINIMUM_DIFFICULTY() {
				return BigInteger.ONE;
			}
		}));

		this.blockchain = new StandaloneBlockchain();
		blockchain
				.withGasLimit(config.getGasLimit())
				.withGasPrice(config.getGasPrice())
				.withCurrentTime(config.getInitialTime());

		config.getBalances().entrySet()
				.forEach(entry -> blockchain.withAccountBalance(entry.getKey().getAddress().address, entry.getValue().inWei()));

		CompletableFuture.runAsync(() -> {
			try {
				while (true) {
					blockchain.submitTransaction(transactions.take());
					blockchain.createBlock();
				}
			} catch (InterruptedException e) {
				throw new EthereumApiException("error while polling transactions for test env", e);
			}
		});

		this.config = config;
	}

	public EthAccount defaultAccount() {
		return AccountProvider.from(this.blockchain.getSender());
	}

	@Override
	public Blockchain getBlockchain() {
		return blockchain.getBlockchain();
	}

	@Override
	public void close() {

	}

	@Override
	public long getGasPrice() {
		return config.getGasPrice();
	}

	@Override
	public void submitTransaction(Transaction tx) {
		transactions.add(tx);
	}

	@Override
	public Transaction createTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimitForConstantCalls, byte[] address, BigInteger value,
			byte[] data) {
		return blockchain.createTransaction(nonce.longValue(), address, value.longValue(), data);
	}

	@Override
	public void addListener(EthereumEventHandler ethereumEventHandler) {
		blockchain.addEthereumListener(ethereumEventHandler);
	}
}
