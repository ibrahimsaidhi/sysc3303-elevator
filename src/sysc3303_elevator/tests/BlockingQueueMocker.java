package sysc3303_elevator.tests;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingQueueMocker<T> implements BlockingQueue<T> {

	@Override
	public T remove() {
		throw new RuntimeException("'remove' not implemented");
	}

	@Override
	public T poll() {
		throw new RuntimeException("'poll' not implemented");
	}

	@Override
	public T element() {
		throw new RuntimeException("'element' not implemented");
	}

	@Override
	public T peek() {
		throw new RuntimeException("'peek' not implemented");
	}

	@Override
	public int size() {
		throw new RuntimeException("'size' not implemented");
	}

	@Override
	public boolean isEmpty() {
		throw new RuntimeException("'isEmpty' not implemented");
	}

	@Override
	public Iterator<T> iterator() {
		throw new RuntimeException("'iterator' not implemented");
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException("'toArray' not implemented");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new RuntimeException("'toArray' not implemented");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("'containsAll' not implemented");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new RuntimeException("'addAll' not implemented");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("'removeAll' not implemented");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("'retainAll' not implemented");
	}

	@Override
	public void clear() {
		throw new RuntimeException("'clear' not implemented");
	}

	@Override
	public boolean add(T e) {
		throw new RuntimeException("'add' not implemented");
	}

	@Override
	public boolean offer(T e) {
		throw new RuntimeException("'offer' not implemented");
	}

	@Override
	public void put(T e) throws InterruptedException {
		throw new RuntimeException("'put' not implemented");
	}

	@Override
	public boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
		throw new RuntimeException("'offer' not implemented");
	}

	@Override
	public T take() throws InterruptedException {
		throw new RuntimeException("'take' not implemented");
	}

	@Override
	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		throw new RuntimeException("'poll' not implemented");
	}

	@Override
	public int remainingCapacity() {
		throw new RuntimeException("'remainingCapacity' not implemented");
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("'remove' not implemented");
	}

	@Override
	public boolean contains(Object o) {
		throw new RuntimeException("'contains' not implemented");
	}

	@Override
	public int drainTo(Collection<? super T> c) {
		throw new RuntimeException("'drainTo' not implemented");
	}

	@Override
	public int drainTo(Collection<? super T> c, int maxElements) {
		throw new RuntimeException("'drainTo' not implemented");
	}

}
