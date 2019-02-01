# Multi-tenant Work Scheduler

This library takes a stab at implementing a Multi-tenant work queue manager and scheduler which can be useful for shared cloud services, i.e., services which are shared by multiple tenants.

The major challenge while implementing multi-tenant/shared services is around fair-sharing of computing and networking resources. If there is a single (or few) tenants consuming too much of computing resources, it will impact rest of the tenants, and they would see higher latency and lower throughput for their requests impacting their SLAs. So, it's very much required for shared services to be designed to allow fair-sharing of resources.

Typically, shared services would be designed to have separate work queues per tenant, where each tenant's requests would be queued up for processing. And, there would a set of "shared" worker threads which would take requests from tenant work queues and process them. So, before a worker thread can process a request, it needs to pick and choose the work queue from which it has to take the request from. This is one of the first phases (in the lifecycle of request) where the idea of fair-sharing has to be applied, and it is sometimes referred to as work-scheduling.

One simple work-scheduling algorithm is to pick tenant work queues in round-robin fashion. This is referred to as "fair queueing" and will ensure that one single tenant sending too many requests would not block other tenants sending only a few requests. There are various other work-scheduling algorithms such as "weighted fair queueing" etc. More details can be found here:

https://en.wikipedia.org/wiki/Fair_queuing

https://en.wikipedia.org/wiki/Weighted_fair_queueing


This Java library aims to provide a simple framework which encapsulates a Multi-tenant Work Queue, that allows plugging in different work-scheduling algorithms.

Please have a look at the unit tests to get an idea about the usage.
