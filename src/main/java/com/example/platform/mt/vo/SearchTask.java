package com.example.platform.mt.vo;

/**
 * Represents a search task.
 */
public class SearchTask extends AbstractTask {
    public SearchTask(String taskDescription) {
        super(taskDescription);
    }

    @Override
    public void run() {
        try {
            //sleeping for 1ms, to mimic a short/quick task. It might not be
            //same as its not consuming CPU, but should be ok.
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
