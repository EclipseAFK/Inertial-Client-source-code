package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

public class ThreadedFileIOBase implements Runnable {
     private static final ThreadedFileIOBase INSTANCE = new ThreadedFileIOBase();
     private final List threadedIOQueue = Collections.synchronizedList(Lists.newArrayList());
     private volatile long writeQueuedCounter;
     private volatile long savedIOCounter;
     private volatile boolean isThreadWaiting;

     private ThreadedFileIOBase() {
          Thread thread = new Thread(this, "File IO Thread");
          thread.setPriority(1);
          thread.start();
     }

     public static ThreadedFileIOBase getThreadedIOInstance() {
          return INSTANCE;
     }

     public void run() {
          while(true) {
               this.processQueue();
          }
     }

     private void processQueue() {
          for(int i = 0; i < this.threadedIOQueue.size(); ++i) {
               IThreadedFileIO ithreadedfileio = (IThreadedFileIO)this.threadedIOQueue.get(i);
               boolean flag = ithreadedfileio.writeNextIO();
               if (!flag) {
                    this.threadedIOQueue.remove(i--);
                    ++this.savedIOCounter;
               }

               try {
                    Thread.sleep(this.isThreadWaiting ? 0L : 10L);
               } catch (InterruptedException var6) {
                    var6.printStackTrace();
               }
          }

          if (this.threadedIOQueue.isEmpty()) {
               try {
                    Thread.sleep(25L);
               } catch (InterruptedException var5) {
                    var5.printStackTrace();
               }
          }

     }

     public void queueIO(IThreadedFileIO fileIo) {
          if (!this.threadedIOQueue.contains(fileIo)) {
               ++this.writeQueuedCounter;
               this.threadedIOQueue.add(fileIo);
          }

     }

     public void waitForFinish() throws InterruptedException {
          this.isThreadWaiting = true;

          while(this.writeQueuedCounter != this.savedIOCounter) {
               Thread.sleep(10L);
          }

          this.isThreadWaiting = false;
     }
}
