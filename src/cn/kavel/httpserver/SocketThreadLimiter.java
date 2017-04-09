package cn.kavel.httpserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wjw_w on 2017/4/8.
 */
public class SocketThreadLimiter {

    private List<Thread> threadList = new ArrayList<>();
    private List<SocketHandler> socketHandlerWaitList = new CopyOnWriteArrayList<>();
    private int mCurrentThreadLimit;
    private int mCurrentSocketAwaiting = 0;

    SocketThreadLimiter() {
        mCurrentThreadLimit = ServerSocketRunnable.mThreadPerRunningStep;
        new Thread(new ThreadMonitor()).start();
        new Thread(new ThreadLimitOrganizer()).start();
    }

    synchronized void addNewHandlerToWaitList(SocketHandler socketHandler) {
        socketHandlerWaitList.add(socketHandler);
        mCurrentSocketAwaiting += socketHandler.getSocketCount();
    }

    class ThreadMonitor implements Runnable {
        @Override
        public void run() {
            try {
                File logfile = new File("D:\\Workspace\\JavaNtwkJAVA\\testResult", "log.csv");
                logfile.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logfile));
                while (true) {

                    threadList.removeIf(thread -> !thread.isAlive());



                    if (!socketHandlerWaitList.isEmpty() && threadList.size() < mCurrentThreadLimit) {
                        SocketHandler socketHandler = socketHandlerWaitList.remove(0);
                        mCurrentSocketAwaiting -= socketHandler.getSocketCount();
                        Thread t = new Thread(socketHandler);
                        t.start();
                        threadList.add(t);

                        int ThreadHandling = threadList.size();
                        int ThreadAwait = socketHandlerWaitList.size();
                        int SocketHandling = SocketHandler.getClientServingCount();
                        int SocketAwait = mCurrentSocketAwaiting+ ServerSocketRunnable.getSocketAwaiting();
                        System.out.println("ThreadMonitor:  ThreadHandling:" + ThreadHandling + " ThreadLimit:" + mCurrentThreadLimit +
                                " ThreadAwait:" + ThreadAwait + " SocketHandling:" + SocketHandling +
                                " SocketAwait:" + SocketAwait);
                        bufferedWriter.write(ThreadHandling + "," + mCurrentThreadLimit +
                                "," + ThreadAwait + "," + SocketHandling +
                                "," + SocketAwait + ",");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ThreadLimitOrganizer implements Runnable {
        @Override
        public void run() {

            while (true) {
                int SocketAwait = socketHandlerWaitList.size() * ServerSocketRunnable.mSocketPerThread + ServerSocketRunnable.getSocketAwaiting();
                try {
                    if (SocketAwait > 5000 && mCurrentThreadLimit < ServerSocketRunnable.mThreadPerRunningMaximum) {
                        mCurrentThreadLimit += ServerSocketRunnable.mThreadPerRunningStep;
                        //System.out.println("Calibrated+:" + mCurrentThreadLimit);
                        Thread.sleep(1000); //I got tired...
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (SocketAwait < 10 && mCurrentThreadLimit > ServerSocketRunnable.mThreadPerRunningStep) {
                        mCurrentThreadLimit -= ServerSocketRunnable.mThreadPerRunningStep;
                        //System.out.println("Calibrated-:" + mCurrentThreadLimit);
                        Thread.sleep(1000); //I got tired...
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
