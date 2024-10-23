package org.jc;

import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NumberTest {


    static class TestInt
    {
        private final static AtomicInteger counter = new AtomicInteger();
        final int value;
        final int id  = counter.getAndIncrement();


        TestInt(int value)
        {
            this.value = value;
        }

        public String toString()
        {
            return "id=" + id + "," + value;
        }

    }
    @Test
    public void numberEquality()
    {
        for(int i = 0; i < 256; i++)
        {
            int a = i;
            int b = i;
            long lA = i;
            long lB = i;
            assert a==b;
            assert lA==lB;
            assert lA==a;
        }
    }

    @Test
    public void priorityTest()
    {





        PriorityQueue<TestInt> maxHeap = new PriorityQueue<>((o1, o2) -> o1.value - o2.value);


        // Add elements
        maxHeap.add(new TestInt(10));
        maxHeap.add(new TestInt(100));
        maxHeap.add(new TestInt(150));
        maxHeap.add(new TestInt(50));
        maxHeap.add(new TestInt(200));
        maxHeap.add(new TestInt(10));

        // Poll elements (should be in descending order)
        while (!maxHeap.isEmpty()) {
            System.out.println(maxHeap.poll());
        }
    }
}
