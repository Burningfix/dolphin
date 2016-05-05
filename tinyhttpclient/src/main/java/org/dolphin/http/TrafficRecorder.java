package org.dolphin.http;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dolphin.lib.util.FileInfoUtil;

/**
 * Created by dolphin on 2015/5/11.
 * Record the traffic status of current http request.
 */
public interface TrafficRecorder {
    public static final long PRINTLN_INDIVER = 2000; // 1S

    public void onBodyIn(long cursor, long length);

    public void onBodyOut(long cursor, long length);

    public void onBodyIn(long newReadCount);

    public void onBodyOut(long newWrittenCount);

    public long getInSize();

    public long getOutSize();

    public long getInCost();

    public long getOutCost();

    public long spotUpSpeed();

    public long spotDownSpeed();

    static class Entry<T1, T2> {
        private T1 key;
        private T2 value;
        private Entry(T1 key, T2 value) {
            this.key = key;
            this.value = value;
        }

        private T1 getKey(){
            return key;
        }

        private T2 getValue(){
            return value;
        }
    }

    public static final TrafficRecorder GLOBAL_TRAFFIC_RECORDER = new TrafficRecorder() {
        private long lastPrintInTime = 0;
        private long lastPrintOutTime = 0;
        private long printTimeInterval = 2000;
        private long inSize = 0;
        private long outSize = 0;
        private long firstInTimestamp = 0;
        private long firstOutTimeStamp = 0;
        /**
         * 保留的读取的历史纪录
         */
        private final List<Entry<Long, Long>> inHistory = new LinkedList<Entry<Long, Long>>();

        /**
         * 保留的写入的历史纪录
         */
        private final List<Entry<Long, Long>> outHistory = new LinkedList<Entry<Long, Long>>();

        private long now() {
            return System.currentTimeMillis();
        }

        @Override
        public void onBodyIn(long cursor, long length) {
            throw new UnsupportedOperationException("Global traffice recorder not support this fucntion");
        }

        @Override
        public void onBodyOut(long cursor, long length) {
            throw new UnsupportedOperationException("Global traffice recorder not support this fucntion");
        }

        @Override
        public synchronized void onBodyIn(long newReadCount) {
            inSize += newReadCount;
            long now = System.currentTimeMillis();

            if (firstInTimestamp <= 0) {
                firstInTimestamp = now;
            }
            inHistory.add(new Entry<Long, Long>(now, newReadCount));

            if (lastPrintInTime <= 0) {
                lastPrintInTime = now;
                return;
            }


            if (now - lastPrintInTime > printTimeInterval) {
                System.out.println("应用下载速度: " + FileInfoUtil.formatSize(spotDownSpeed()));
                lastPrintInTime = now;
            }
        }

        @Override
        public synchronized void onBodyOut(long newWrittenCount) {
            outSize += newWrittenCount;
            long now = System.currentTimeMillis();
            if (firstOutTimeStamp <= 0) {
                firstOutTimeStamp = now;
            }
            outHistory.add(new Entry<Long, Long>(now, newWrittenCount));

            if (lastPrintOutTime <= 0) {
                lastPrintOutTime = now;
                return;
            }

            if (now - lastPrintOutTime > printTimeInterval) {
                System.out.println("应用上传速度: " + spotDownSpeed());
                lastPrintOutTime = now;
            }
        }

        @Override
        public long getInSize() {
            return inSize;
        }

        @Override
        public long getOutSize() {
            return outSize;
        }

        @Override
        public long getInCost() {
            return firstInTimestamp > 0 ? now() - firstInTimestamp : 0;
        }

        @Override
        public long getOutCost() {
            return firstOutTimeStamp > 0 ? now() - firstOutTimeStamp : 0;
        }

        @Override
        public long spotUpSpeed() {
            synchronized(this) {
                return calculateCounts(outHistory, printTimeInterval);
            }
        }

        @Override
        public long spotDownSpeed() {
            synchronized(this) {
                return calculateCounts(inHistory, printTimeInterval);
            }
        }

        private long calculateCounts(List<Entry<Long, Long>> history, final long timeInterval) {
            synchronized(this) {
                long now = now();
                long count = 0;
                Iterator<Entry<Long, Long>> iterator = history.iterator();
                while (iterator.hasNext()) {
                    Entry<Long, Long> pair = iterator.next();
                    if (pair == null || now - pair.getKey().longValue() > timeInterval) {
                        iterator.remove();
                    } else {
                        count += pair.getValue().longValue();
                    }
                }
                return count;
            }
        }
    };

    public static class Builder {
        public static TrafficRecorder build(final TrafficRecorder parent) {
            return new TrafficRecorderImpl() {
                @Override
                public void onBodyIn(long newReadedCount) {
                    super.onBodyIn(newReadedCount);
                    parent.onBodyIn(newReadedCount);
                    GLOBAL_TRAFFIC_RECORDER.onBodyIn(newReadedCount);
                }

                @Override
                public void onBodyOut(long newWrittenCount) {
                    super.onBodyOut(newWrittenCount);
                    parent.onBodyOut(newWrittenCount);
                    GLOBAL_TRAFFIC_RECORDER.onBodyOut(newWrittenCount);
                }
            };
        }

        public static TrafficRecorder build() {
            return new TrafficRecorderImpl() {
                @Override
                public void onBodyIn(long newReadedCount) {
                    super.onBodyIn(newReadedCount);
                    GLOBAL_TRAFFIC_RECORDER.onBodyIn(newReadedCount);
                }

                @Override
                public void onBodyOut(long newWrittenCount) {
                    super.onBodyOut(newWrittenCount);
                    GLOBAL_TRAFFIC_RECORDER.onBodyOut(newWrittenCount);
                }
            };
        }
    }


    static class TrafficRecorderImpl implements TrafficRecorder {
        public static final long DEFAULT_TIME_INTERVAL = 2000; // 2000 ms
        /**
         * 第一次接受得到输入的时间戳
         */
        private long firstInTimestamp = -1;

        /**
         * 第一次接受得到输出的时间戳
         */
        private long firstOutTimestamp = -1;

        /**
         * 最后一次输入时间
         */
        private long lastInTimestamp = -1;

        /**
         * 最后一次输出时间
         */
        private long lastOutTimestamp = -1;

        /**
         * 首次输入的cursor
         */
        private long firstInCursor = -1;

        /**
         * 首次输出的cursor
         */
        private long firstOutCursor = -1;

        /**
         * 上一次输入记录的位置
         */
        private long lastInCursor = -1;

        /**
         * 上一次输出记录的位置
         */
        private long lastOutCursor = -1;

        /**
         * 保留的读取的历史纪录
         */
        private final List<Entry<Long, Long>> inHistory = new LinkedList<Entry<Long, Long>>();

        /**
         * 保留的写入的历史纪录
         */
        private final List<Entry<Long, Long>> outHistory = new LinkedList<Entry<Long, Long>>();

        /**
         * 瞬时速度的统计时常，默认是两秒统计一次
         */
        public final long timeInterval;

        private TrafficRecorderImpl() {
            timeInterval = DEFAULT_TIME_INTERVAL;
        }

        private TrafficRecorderImpl(TrafficRecorder parent, long timeInterval) {
            this.timeInterval = timeInterval;
        }

        private static long now() {
            return System.currentTimeMillis();
        }

        @Override
        public void onBodyIn(long cursor, long length) {
            long curr = now();
            if (firstInTimestamp <= 0) {
                firstInTimestamp = curr;
            }

            lastInTimestamp = curr;

            if (firstInCursor < 0) {
                lastInCursor = firstInCursor = cursor;
                return;
            }

            onBodyIn(cursor - lastInCursor);
            lastInCursor = cursor;
        }

        @Override
        public void onBodyOut(long cursor, long length) {
            long curr = now();
            if (firstOutTimestamp < 0) {
                firstOutTimestamp = curr;
            }

            lastOutTimestamp = curr;

            if (firstOutCursor < 0) {
                lastOutCursor = firstOutCursor = cursor;
                return;
            }

            onBodyOut(cursor - lastOutCursor);
            lastOutCursor = cursor;
        }

        @Override
        public void onBodyIn(long newReadedCount) {
            long curr = now();
            inHistory.add(new Entry<Long, Long>(curr, newReadedCount));
            spotDownSpeed();
        }

        @Override
        public void onBodyOut(long newWrittenCount) {
            long curr = now();
            outHistory.add(new Entry<Long, Long>(curr, newWrittenCount));
            spotUpSpeed();
        }

        @Override
        public long getInSize() {
            return lastInCursor > firstInCursor ? lastInCursor - firstInCursor : 0;
        }

        @Override
        public long getOutSize() {
            return lastOutCursor > firstOutCursor ? lastOutCursor - firstOutCursor : 0;
        }

        @Override
        public long getInCost() {
            return lastInTimestamp > firstInTimestamp ? lastInTimestamp - firstInTimestamp : 0;
        }

        @Override
        public long getOutCost() {
            return lastOutTimestamp > firstOutTimestamp ? lastOutTimestamp - firstOutTimestamp : 0;
        }


        /**
         * 返回最近一段时间内的瞬时速度, see {@link #timeInterval}
         *
         * @return
         */
        @Override
        public long spotUpSpeed() {
            return calculateCounts(outHistory, timeInterval);
        }

        @Override
        public long spotDownSpeed() {
            return calculateCounts(inHistory, timeInterval);
        }

        private static long calculateCounts(List<Entry<Long, Long>> history, final long timeInterval) {
            long now = now();
            long count = 0;
            Iterator<Entry<Long, Long>> iterator = history.iterator();
            while (iterator.hasNext()) {
                Entry<Long, Long> pair = iterator.next();
                if (null == pair || now - pair.getKey().longValue() > timeInterval) {
                    iterator.remove();
                } else {
                    count += pair.getValue().longValue();
                }
            }
            return count;
        }
    }
}
