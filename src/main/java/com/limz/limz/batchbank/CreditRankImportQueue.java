package javabean.newcredit.creditbank.thread;

/**
 * @ClassName: CreditRankImportQueue ��������������--OBF������Ϣ�������롿
 * @Description: ���ڶ�дexcel�̼߳�����ݴ������
 * @author Limz
 * @date 2017-7-5 ����9:16:09
 * @param <E>
 */
public class CreditRankImportQueue<E> {

    private Entry<E> header = new Entry<E>(null, null, null);
    private Entry<E> tail = new Entry<E>(null, null, null);

    /**
     * ����һ���µ�ʵ�� CreditRankImportQueue.
     * <p>
     * Description:Ĭ��20����
     * </p>
     */
    public static CreditRankImportQueue<Object[]> buildQueue() {
        return new CreditRankImportQueue<Object[]>(20);
    }

    /**
     * ���췽��
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
     * push(����������һ��Ԫ��)
     * 
     * @param e
     * @return �Ƿ�ɹ�
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
     * hasNext(������)
     * 
     * @return true/false
     * @author Limz
     */
    public boolean hasNext() {
        return !tail.next.isEmpty();
    }

    /**
     * pop(�Ӷ�����ȡ��һ��Ԫ�ز�ɾ��)
     * 
     * @return Ԫ��/null
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
     * clear(��������е�����Ԫ��)
     * 
     * @author Limz
     */
    public synchronized void clear() {
        Entry<E> e = header.next;
        while (e != header) {
            e.remove();
            e = e.next;
        }
        System.out.println("�������");
    }

    /**
     * @ClassName: Entry
     * @Description: Ԫ�ش洢����
     * @author Limz
     * @date 2017-7-5 ����9:18:51
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
          * remove(�Ƴ�Ԫ���е����ݲ����䷵��)
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
          * put(�����ݷ���Ԫ��)
          * @param e
          * @author Limz
         */
        void put(E e) {
            element = e;
            empty = false;
        }

        /**
          * isEmpty(Ԫ���Ƿ�Ϊ��)
          * @return true/false
          * @author Limz
         */
        boolean isEmpty() {
            return empty;
        }
    }
}