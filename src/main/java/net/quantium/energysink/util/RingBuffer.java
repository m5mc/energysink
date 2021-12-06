package net.quantium.energysink.util;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;

public class RingBuffer {
    private final long[] data;
    private int ptr;
    private long sum;

    public RingBuffer(int size) {
        this.data = new long[size];
    }

    public long sum() {
        return sum;
    }

    public long average() {
        return sum / data.length;
    }

    public long get(int idx) {
        if (idx < 0 || idx >= data.length) throw new IllegalArgumentException();

        idx = ptr - idx - 1;
        while (idx < 0) idx += data.length;

        return data[idx];
    }

    public int size() {
        return data.length;
    }

    public void push(long v) {
        long value = data[ptr];
        data[ptr] = v;
        ptr++;
        if(ptr >= data.length) ptr = 0;

        sum -= value;
        sum += v;
    }

    public NBTTagList serialize() {
        NBTTagList list = new NBTTagList();

        for(int i = 0; i < size(); i++) {
            list.appendTag(new NBTTagLong(get(i)));
        }

        return list;
    }

    public static RingBuffer deserialize(int size, NBTTagList list) {
        RingBuffer buffer = new RingBuffer(size);

        for(int i = list.tagCount() - 1; i >= 0; i--) {
            buffer.push(((NBTTagLong)list.get(i)).getLong());
        }

        return buffer;
    }
}
