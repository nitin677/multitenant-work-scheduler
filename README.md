# Multi-tenant Work Scheduler

This library takes a stab at implementing a Multi-tenant work queue manager and scheduler which can be useful for shared cloud services, i.e., services which are shared by multiple tenants.

The major challenge while implementing multi-tenant/shared services is around fair-sharing of computing and networking resources. If there is a single (or few) tenants consuming too much of computing resources, it will impact rest of the tenants, and they would see higher latency and lower throughput for their requests impacting their SLAs. So, it's very much required for shared services to be designed to allow fair-sharing of resources.

Typically, shared services would be designed to have separate work queues per tenant, where each tenant's requests would be queued up for processing. And, there would a set of "shared" worker threads which would take requests from tenant work queues and process them. So, before a worker thread can process a request, it needs to pick and choose the work queue from which it has to take the request from. This is one of the first phases (in the lifecycle of request) where the idea of fair-sharing has to be applied, and it is sometimes referred to as work-scheduling.

One simple work-scheduling algorithm is to pick tenant work queues in round-robin fashion. This is referred to as "fair queueing" and will ensure that one single tenant sending too many requests would not block other tenants sending only a few requests. There are various other work-scheduling algorithms such as "weighted fair queueing" etc. More details can be found here:

https://en.wikipedia.org/wiki/Fair_queuing

https://en.wikipedia.org/wiki/Weighted_fair_queueing


This Java library aims to provide a simple framework which encapsulates a Multi-tenant Work Queue, that allows plugging in different work-scheduling algorithms. It currently supports only the Fair queueing algorithm, others will follow in few days.

Please have a look at the unit tests/client to get an idea about the usage.

Specifically you can check test/client "testFairStrategyFewerTenantsSingleWorker" which demonstrates that the scheduler gives a fair chance to all tenants. To illustrate this, the client uses fewer number of tenants, and sets number of worker threads to 1, so that it's evident from the logs that tenants' tasks are picked up in round-robin fashion from the multi-tenant work queue. Also starting tenant producers immediately after creating them. This is to show that initially scheduler schedules more tasks from tenants 0 and 1 because their producers are started first. But eventually when all tenants's producers are started, scheduler schedules them in round-robin fashion.

On running this test/client, you can see the output similar to the following:
--------------------------------------------

Initialized Multi-tenant worker thread 0\n
Processing task for tenantId:1

Processing task for tenantId:0

Processing task for tenantId:1

Processing task for tenantId:0

Processing task for tenantId:1

Processing task for tenantId:3

Processing task for tenantId:2

Processing task for tenantId:0

Processing task for tenantId:4

Processing task for tenantId:1

Processing task for tenantId:3

Processing task for tenantId:2

Processing task for tenantId:0

Processing task for tenantId:4

Processing task for tenantId:1

Processing task for tenantId:3

Processing task for tenantId:2

Processing task for tenantId:0

--------------------------------------------

As it is evident from the output above, that after the first few tasks, the scheduler picks the tenants in round-robin fashion - 3,2,0,4,1,3,2,0,4,1.......
