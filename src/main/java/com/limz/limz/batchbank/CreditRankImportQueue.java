package javabean.newcredit.creditbank.thread;

/**
 * @ClassName: CreditRankImportQueue 【导入银行资信--OBF评级信息批量导入】
 * @Description: 用于读写excel线程间的数据传输队列
 * @author Limz
 * @date 2017-7-5 上午9:16:09
 * @param <E>
 */
public class CreditRankImportQueue<E> {

    private Entry<E> header = new Entry<E>(null, null, null);
    private Entry<E> tail = new Entry<E>(null, null, null);

    /**
     * 创建一个新的实例 CreditRankImportQueue.
     * <p>
     * Description:默认20长度
     * </p>
     */
    public static CreditRankImportQueue<Object[]> buildQueue() {
        return new CreditRankImportQueue<Object[]>(20);
    }

    /**
     * 构造方法
     * @param size
     */
    private CreditRankImportQueue(int size) {
        header.previous = tail;
        tail.next = header;
        Entry<E> cursor = header;
        for (int i = 0; i < size; i++) {
            Entry<E> newEntry = new Entry<E>(null, null, cursor);
            cursor.next = newEntry;
            cursor = newEntry;
        }
        cursor.next = tail;
        tail.previous = cursor;
    }

    /**
     * push(向队列中添加一个元素)
     * 
     * @param e
     * @return 是否成功
     * @author Limz
     */
    public synchronized boolean push(E e) {

        if (header.next == tail) {
            return false;
        }
        Entry<E> entry = header.next;
        entry.put(e);
        Entry<E> entry3 = entry.next;
        entry.next = header;
        header.next = entry3;
        entry3.previous = header;
        Entry<E> entry2 = header.previous;
        header.previous = entry;
        entry.previous = entry2;
        entry2.next = entry;
        return true;
    }

    /**
     * hasNext(还有吗)
     * 
     * @return true/false
     * @author Limz
     */
    public boolean hasNext() {
        return !tail.next.isEmpty();
    }

    /**
     * pop(从队列中取出一个元素并删除)
     * 
     * @return 元素/null
     * @author Limz
     */
    public synchronized E pop() {
        Entry<E> entry;
        if (tail.next == header) {
            return null;
        }
        entry = tail.next;
        Entry<E> entry2 = entry.next;
        entry.next = tail;
        tail.next = entry2;
        entry2.previous = tail;
        Entry<E> entry3 = tail.previous;
        entry.previous = entry3;
        entry3.next = entry;
        tail.previous = entry;
        return entry.remove();
    }

    /**
     * clear(清除队列中的所有元素)
     * 
     * @author Limz
     */
    public synchronized void clear() {
        Entry<E> e = header.next;
        while (e != header) {
            e.remove();
            e = e.next;
        }
        System.out.println("清除容器");
    }

    /**
     * @ClassName: Entry
     * @Description: 元素存储对象
     * @author Limz
     * @date 2017-7-5 上午9:18:51
     * @param <E>
     */
    private static class Entry<E> {
        boolean empty;
        E element;
        Entry<E> next;
        Entry<E> previous;

        Entry(E element, Entry<E> next, Entry<E> previous) {
            this.element = element;
            this.next = next;
            this.previous = previous;
            this.empty = true;
        }

        /**
          * remove(移除元素中的数据并将其返回)
          * @return
          * @author Limz
         */
        E remove() {
            E e = element;
            element = null;
            empty = true;
            return e;
        }

        /**
          * put(将数据放入元素)
          * @param e
          * @author Limz
         */
        void put(E e) {
            element = e;
            empty = false;
        }

        /**
          * isEmpty(元素是否为空)
          * @return true/false
          * @author Limz
         */
        boolean isEmpty() {
            return empty;
        }
    }
}