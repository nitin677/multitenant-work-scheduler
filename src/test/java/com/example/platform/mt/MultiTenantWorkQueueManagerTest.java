package com.example.platform.mt;

import com.example.platform.mt.exception.UnknownTenantException;
import com.example.platform.mt.impl.MTWorkerThread;
import com.example.platform.mt.vo.SchedulingStrategy;
import com.example.platform.mt.vo.SearchTask;
import com.example.platform.mt.vo.TenantAwareTask;
import com.example.platform.mt.vo.TenantConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MultiTenantWorkQueueManagerTest {
    private MultiTenantWorkQueueManager queueManager;
    static int noOfTenants = 60;
    private int concurrentClientsPerTenant = 100;
    static int tasksPerClient = 100;
    private List<String> tenantIds;
    private int totalWorkerThreads = 40;
    private int workCapacity = 100;

    @Before
    public void setUp() throws Exception {
        List<TenantConfig> tenantConfigList = createTenantConfigs();
        queueManager = new MultiTenantWorkQueueManager(tenantConfigList,
            SchedulingStrategy.FAIR_QUEUEING);
    }

    private List<TenantConfig> createTenantConfigs() {
        List<TenantConfig> tenantConfigList = new ArrayList<>();
        tenantIds = new ArrayList<>();
        for (int i = 0; i < noOfTenants; i++) {
            String tenantId = "tenantId:"+i;
            TenantConfig cfg = new TenantConfig(tenantId, "tenantName:"+i, workCapacity);
            tenantIds.add(tenantId);
            tenantConfigList.add(cfg);
        }
        return tenantConfigList;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMultitenantFairStrategy() {
        try {
            long start = System.currentTimeMillis();
            //init and start worker threads
            for (int workers = 0; workers < totalWorkerThreads; workers++) {
                MTWorkerThread worker = new MTWorkerThread(queueManager, workers);
                worker.start();
            }

            //init producer threads for each tenant and start them.
            Set<TaskProducer> producers = new HashSet<>();
            for (String tenantId : tenantIds) {
                for (int j = 0; j < concurrentClientsPerTenant; j++) {
                    TaskProducer producer = new TaskProducer(queueManager, tenantId, j, tasksPerClient);
                    producers.add(producer);
                }
            }
            for (TaskProducer producer : producers)
                producer.start();

            queueManager.provisionTenant(new TenantConfig("tenantId:"+noOfTenants, "tenantName:"+noOfTenants, 100));
            TaskProducer producer = new TaskProducer(queueManager, "tenantId:"+noOfTenants, 0, 1);
            Thread.sleep(2000);
            producer.start();

            int target = (noOfTenants)* concurrentClientsPerTenant * tasksPerClient;
            while (queueManager.getProcessedTasksCount() != target+1) {
                //System.out.println("Main thread: size "+fairQueue.size()+", total taskDescription processed "+fairQueue.getProcessedTasksCount());
                Thread.sleep(1000);
            }
            long end = System.currentTimeMillis();
            System.out.println("Total time taken to process "+queueManager.getProcessedTasksCount()+" noOfTasks in seconds : "+(end - start)/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deProvisionTenantSubmitWorkThrowsException() {
        queueManager.deProvisionTenant(tenantIds.get(0));
        assertEquals(queueManager.getNoOfTenants(), noOfTenants-1);

        boolean unknownTenantExceptionThrown = false;
        try {
            queueManager.submitWork(new TenantAwareTask(tenantIds.get(0), new SearchTask("Search task from "+tenantIds.get(0))));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownTenantException e) {
            unknownTenantExceptionThrown = true;
        }
        assertTrue(unknownTenantExceptionThrown);

    }

    @Test
    public void provisionTenantAndSubmitWorkSuccessful() {
        String newTenantId = "tenantId:"+noOfTenants;
        queueManager.provisionTenant(new TenantConfig(newTenantId, "tenantName:"+noOfTenants, 100));
        assertEquals(queueManager.getNoOfTenants(), noOfTenants+1);

        boolean unknownTenantExceptionThrown = false;
        try {
            queueManager.submitWork(new TenantAwareTask(newTenantId, new SearchTask("Search task from "+newTenantId)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownTenantException e) {
            unknownTenantExceptionThrown = true;
        }
        assertFalse(unknownTenantExceptionThrown);
    }

    @Test
    public void submitWorkPositive() {
        boolean unknownTenantExceptionThrown = false;
        try {
            queueManager.submitWork(new TenantAwareTask(tenantIds.get(0), new SearchTask("searching user 123")));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownTenantException e) {
            e.printStackTrace();
            unknownTenantExceptionThrown = true;
        }
        assertFalse(unknownTenantExceptionThrown);
    }

    @Test
    public void submitWorkWithoutProvisioningTenant() {
        boolean unknownTenantExceptionThrown = false;
        try {
            String unknownTenantId = "tenantId:1234";
            queueManager.submitWork(new TenantAwareTask(unknownTenantId, new SearchTask("searching user 123")));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownTenantException e) {
            unknownTenantExceptionThrown = true;
        }
        assertTrue(unknownTenantExceptionThrown);
    }

    @Test
    public void submitWorkBlocksWhenWorkCapacityExceeded() {
        TaskProducer producer = new TaskProducer(queueManager, tenantIds.get(0), 1, workCapacity+1);
        producer.start();

        checkBlocking(producer);
        producer.interrupt();
    }

    private void checkBlocking(TaskProducer thread) {
        try {
            Thread.sleep(1000);
            assertEquals(Thread.State.WAITING, thread.getState());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkNOTBlocking(TaskProducer thread) {
        try {
            Thread.sleep(1000);
            assertEquals(Thread.State.TERMINATED, thread.getState());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void submitWorkDoesNotBlockIfWorkCapacityNotExceeded() {
        TaskProducer producer = new TaskProducer(queueManager, tenantIds.get(0), 1, workCapacity);
        producer.start();
        checkNOTBlocking(producer);
    }

    @Test
    public void takeWork() {
        try {
            String desc1 = "searching user 123";
            String desc2 = "searching user 456";
            queueManager.submitWork(new TenantAwareTask(tenantIds.get(1), new SearchTask(desc1)));
            queueManager.submitWork(new TenantAwareTask(tenantIds.get(2), new SearchTask(desc2)));

            TenantAwareTask task = queueManager.takeWork();
            assertEquals(task.getTenantId(), tenantIds.get(1));
            assertEquals(task.getTask().getDescription(), desc1);

            task = queueManager.takeWork();
            assertEquals(task.getTenantId(), tenantIds.get(2));
            assertEquals(task.getTask().getDescription(), desc2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownTenantException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void takeWorkBlocksOnNoWork() {
        try {
            MTWorkerThread workerThread = new MTWorkerThread(queueManager, 123);
            workerThread.start();
            //now wait for a second and then check worker state, it should be in waiting state.
            Thread.sleep(1000);
            assertEquals(Thread.State.WAITING, workerThread.getState());
            workerThread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNoOfTenants() {
        assertEquals(queueManager.getNoOfTenants(), noOfTenants);
    }
}

class TaskProducer extends Thread {

    MultiTenantWorkQueueManager queueManager;
    String tenantId;
    int threadId;
    private int noOfTasks;

    TaskProducer(MultiTenantWorkQueueManager queueManager, String tenantId, int threadId, int noOfTasks) {
        this.queueManager = queueManager;
        this.tenantId = tenantId;
        this.threadId = threadId;
        this.noOfTasks = noOfTasks;
        setName("Task producer thread "+threadId+" for tenant "+tenantId);
        //System.out.println("Initialized "+getName());
    }
    public void run() {
        for (int i = 0; i < noOfTasks; i++) {
            TenantAwareTask t = new TenantAwareTask(""+tenantId, new SearchTask(threadId+","+i));
            try {
                if (t.getTenantId().equals("tenantId:"+MultiTenantWorkQueueManagerTest.noOfTenants)) {
                    System.out.println("Submitted "+t.getTenantId()+" task at " + System.currentTimeMillis());
                }
                queueManager.submitWork(t);
            } catch (InterruptedException e) {
                System.out.println(getName()+" was interrupted while submitting task "+t);
            } catch (UnknownTenantException e) {
                e.printStackTrace();
            }
        }
    }
}