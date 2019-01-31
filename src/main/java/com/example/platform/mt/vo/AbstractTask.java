package com.example.platform.mt.vo;

/**
 * Abstract class which represents a Task, which can be extended by
 * concrete Tasks.
 */
public abstract class AbstractTask implements Runnable {
    protected String taskDescription;

    public AbstractTask(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    /**
     * Returns task description.
     * @return task description.
     */
    public Object getDescription() {
        return taskDescription;
    }

    @Override
    public abstract void run();
}
