package org.dolphin.http;

import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

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


    public static class Builder {
        public static TrafficRecorder build(TrafficRecorder parent) {


            return null;
        }

        public static TrafficRecorder build() {

            return null;
        }
    }


    public static class TrafficRecorder1 implements TrafficRecorder {
        public static final long DEFAULT_TIME_INTERVAL = 2000; // 2000 ms
        /**
         * 第一次接受得到输入的时间戳
         */
        private long firstInTimestamp = 0;

        /**
         * 第一次接受得到输出的时间戳
         */
        private long firstOutTimestamp = 0;

        private long lastInTimestamp = 0;

        private long lastOutTimestamp = 0;

        private long firstInCursor = 0;


        private long firstOutCursor = 0;

        /**
         * 上一次输入记录的位置
         */
        private long lastInCursor = 0;

        /**
         * 上一次输出记录的位置
         */
        private long lastOutCursor = 0;

        /**
         * 保留的读取的历史纪录
         */
        private final List<Pair<Long, Long>> inHistory = new LinkedList<Pair<Long, Long>>();

        /**
         * 保留的写入的历史纪录
         */
        private final List<Pair<Long, Long>> outHistory = new LinkedList<Pair<Long, Long>>();

        /**
         * 瞬时速度的统计时常，默认是两秒统计一次
         */
        public final long timeInterval;

        public TrafficRecorder1() {
            timeInterval = DEFAULT_TIME_INTERVAL;
        }

        public TrafficRecorder1(TrafficRecorder parent, long timeInterval) {
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

            if (firstInCursor <= 0) {
                firstInCursor = cursor;
                return;
            }

            if (lastInCursor <= 0) {
                lastInCursor = cursor;
                onBodyIn(0);
            } else {
                onBodyIn(cursor - lastInCursor);
                lastInCursor = cursor;
            }

        }

        @Override
        public void onBodyOut(long cursor, long length) {
            long curr = now();
            if (firstOutTimestamp <= 0) {
                firstOutTimestamp = curr;
            }
            lastOutTimestamp = curr;
            if (firstOutCursor <= 0) {
                firstOutCursor = cursor;
                return;
            }

            if (lastOutCursor <= 0) {
                lastOutCursor = cursor;
                onBodyOut(0);
            } else {
                onBodyOut(cursor - lastOutCursor);
                lastOutCursor = cursor;
            }
        }

        @Override
        public void onBodyIn(long newReadedCount) {
            long curr = now();
            inHistory.add(new Pair<Long, Long>(curr, newReadedCount))
        }

        @Override
        public void onBodyOut(long newWrittenCount) {
            long curr = now();
            outHistory.add(new Pair<Long, Long>(curr, newWrittenCount));
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
            return 0;
        }

        @Override
        public long spotDownSpeed() {
            return 0;
        }

        private void limitInHistorySize() {

        }

        private void limitOutHistorySize() {
            
        }
    }


    public static final TrafficRecorder GLOBAL_TRAFFIC_RECORDER = new TrafficRecorderImpl() {
        @Override
        public synchronized void onBodyIn(long cursor, long length) {
            throw new UnsupportedOperationException("Global traffic recorder cannot invoke onBodyIn(long cursor, long length) method.");
        }

        @Override
        public void onBodyOut(long cursor, long length) {
            throw new UnsupportedOperationException("Global traffic recorder cannot invoke onBodyOut(long cursor, long length) method.");
        }
    };

//    public static TrafficRecorder getGlobaleTrafficeRecorder(){
//        return GLOBALE_TRAFFICE_RECORDER;
//    }


    public static class TrafficRecorderImpl implements TrafficRecorder {
        private long printLogDivider = PRINTLN_INDIVER;
        /**
         * 第一次接受得到输入的时间戳
         */
        private long startInTimestamp = 0;
        /**
         * 第一次接受得到输出的时间戳
         */
        private long startOutTimestamp = 0;
        /**
         * 最后一次收到读取记录的时间戳
         */
        private long lastUpdateInTimestamp = 0;
        /**
         * 最后一次收到发送记录的时间戳
         */
        private long lastUpdateOutTimestamp = 0;
        /**
         * 上一次记录的位置
         */
        private long lastInCursor = 0;
        /**
         * 上一次记录的位置
         */
        private long lastOutCursor = 0;

        // 日志相关
        /**
         * 上次打印日志时间
         */
        private long lastPrintInLogTime = -1;
        /**
         * 上次打印日志时间
         */
        private long lastPrintOutLogTime = -1;
        /**
         * 上次打印输出的位置点
         */
        private long lastPrintedOutCursor = -1;
        /**
         * 上次打印输入的位置点
         */
        private long lastPrintedInCursor = -1;


        @Override
        public synchronized void onBodyIn(long cursor, long length) {
            if (lastInCursor >= 0) {
                onBodyIn(cursor - lastInCursor);
            } else {
                onBodyIn(cursor);
            }
        }

        @Override
        public void onBodyIn(final long newReadedCount) {
            long time = System.currentTimeMillis();
            if (startInTimestamp <= 0) {
                startInTimestamp = time;
            }
            if (lastUpdateInTimestamp <= 0) {
                lastUpdateInTimestamp = time;
            }
            lastInCursor += newReadedCount;
            printInLog();
            if (this != GLOBAL_TRAFFIC_RECORDER) {
                GLOBAL_TRAFFIC_RECORDER.onBodyIn(newReadedCount);
            }
        }


        @Override
        public synchronized void onBodyOut(long cursor, long length) {
            if (lastOutCursor >= 0) {
                onBodyIn(cursor - lastInCursor);
            } else {
                onBodyIn(cursor);
            }
        }


        @Override
        public void onBodyOut(final long newWrittenCount) {
            long time = System.currentTimeMillis();
            if (startOutTimestamp <= 0) {
                startOutTimestamp = time;
            }
            if (lastUpdateOutTimestamp <= 0) {
                lastUpdateOutTimestamp = time;
            }
            lastOutCursor += newWrittenCount;
            printOutLog();

            if (this != GLOBAL_TRAFFIC_RECORDER) {
                GLOBAL_TRAFFIC_RECORDER.onBodyOut(newWrittenCount);
            }
        }

        @Override
        public long getInSize() {
            return lastInCursor;
        }

        @Override
        public long getOutSize() {
            return lastOutCursor;
        }

        @Override
        public long getInCost() {
            return lastUpdateInTimestamp - startInTimestamp;
        }

        @Override
        public long getOutCost() {
            return lastUpdateOutTimestamp - startOutTimestamp;
        }

        @Override
        public void printInLog() {
            if (lastPrintInLogTime <= 0) {
                lastPrintInLogTime = System.currentTimeMillis();
                lastPrintedInCursor = 0;
                return;
            }
            long deltaTime = System.currentTimeMillis() - lastPrintInLogTime;
            if (deltaTime >= printLogDivider) {
                long delta = lastInCursor - lastPrintedInCursor;
                HttpLog.d("TinyHttpClient", "平均下载速度: " + (delta / (float) deltaTime * 1000));
                lastPrintInLogTime = System.currentTimeMillis();
                lastPrintedInCursor = lastInCursor;
            }
        }

        @Override
        public void printOutLog() {
            if (lastPrintOutLogTime <= 0) {
                lastPrintOutLogTime = System.currentTimeMillis();
                lastPrintedOutCursor = 0;
                return;
            }
            long deltaTime = System.currentTimeMillis() - lastPrintOutLogTime;
            if (deltaTime >= printLogDivider) {
                long delta = lastOutCursor - lastPrintedOutCursor;
//                HttpLog.d("TinyHttpClient", "平均上传速度: " + (delta / (float) deltaTime * 1000));
                lastPrintOutLogTime = System.currentTimeMillis();
                lastPrintedOutCursor = lastOutCursor;
            }
        }

        @Override
        public void printLog() {
            long cost = getInCost();
            long size = getInSize();
            if (cost > 0) {
//                HttpLog.d("TinyHttpClient", "平均下载速度: " + (size / (float) cost * 1000));
            }
            cost = getOutCost();
            size = getOutSize();
            if (cost > 0) {
//                HttpLog.d("TinyHttpClient", "平均上传速度: " + (size / (float) cost * 1000));
            }
        }
    }
}
