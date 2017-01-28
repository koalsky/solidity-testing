package nl.mwensveen.etereum.test.ethereumj;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ethereum.core.Block;
import org.ethereum.core.PendingState;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListenerAdapter;

/**
 * based on {@link org.ethereum.core.PendingStateTest}
 *
 */
public class PendingListener extends EthereumListenerAdapter {
	public BlockingQueue<Pair<Block, List<TransactionReceipt>>> onBlock = new LinkedBlockingQueue<>();
	public BlockingQueue<Object> onPendingStateChanged = new LinkedBlockingQueue<>();
	// public BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>> onPendingTransactionUpdate = new LinkedBlockingQueue<>();

	Map<ByteArrayWrapper, BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>>> onPendingTransactionUpdate = new HashMap<>();

	@Override
	public void onBlock(Block block, List<TransactionReceipt> receipts) {
		System.out.println("PendingStateTest.onBlock:" + "block = [" + block.getShortDescr() + "]");
		onBlock.add(Pair.of(block, receipts));
	}

	@Override
	public void onPendingStateChanged(PendingState pendingState) {
		System.out.println("PendingStateTest.onPendingStateChanged.");
		onPendingStateChanged.add(new Object());
	}

	@Override
	public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
		System.out.println("PendingStateTest.onPendingTransactionUpdate:" + "txReceipt.err = [" + txReceipt.getError() + "], state = [" + state + "], block: "
				+ block.getShortDescr());
		getQueueFor(txReceipt.getTransaction()).add(Triple.of(txReceipt, state, block));
	}

	public synchronized BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>> getQueueFor(Transaction tx) {
		ByteArrayWrapper hashW = new ByteArrayWrapper(tx.getHash());
		BlockingQueue<Triple<TransactionReceipt, PendingTransactionState, Block>> queue = onPendingTransactionUpdate.get(hashW);
		if (queue == null) {
			queue = new LinkedBlockingQueue<>();
			onPendingTransactionUpdate.put(hashW, queue);
		}
		return queue;
	}

	public PendingTransactionState pollTxUpdateState(Transaction tx) throws InterruptedException {
		return getQueueFor(tx).poll(5, SECONDS).getMiddle();
	}

	public Triple<TransactionReceipt, PendingTransactionState, Block> pollTxUpdate(Transaction tx) throws InterruptedException {
		return getQueueFor(tx).poll(5, SECONDS);
	}
}
