package sysc3303_elevator.networking;

import java.util.concurrent.BlockingQueue;

import sysc3303_elevator.Pair;

public class BlockingChannelBuilder {
	static class SenderInternal<T> implements BlockingSender<T> {
		private BlockingQueue<T> queue;
		public SenderInternal(BlockingQueue<T> queue) {
			this.queue = queue;
		}

		@Override
		public void put(T e) throws InterruptedException {
			this.queue.put(e);
		}
	}

	static class ReceiverInternal<T> implements BlockingReceiver<T> {
		private BlockingQueue<T> queue;
		public ReceiverInternal(BlockingQueue<T> queue) {
			this.queue = queue;
		}

		@Override
		public T take() throws InterruptedException {
			return this.queue.take();
		}
	}

	public static <T> Pair<BlockingSender<T>, BlockingReceiver<T>> FromBlockingQueue(BlockingQueue<T> queue) {
		return new Pair<>(new SenderInternal<>(queue), new ReceiverInternal<>(queue));
	}
}
