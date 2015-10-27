In addition to code mobility, Mobility-RPC can be used as a general purpose RPC framework, providing the same (and more) functionality than RMI.

# Overview #

The following benchmark compares the performance of Mobility-RPC versus RMI, when it is required to invoke methods across a network. The benchmark requires both Mobility-RPC and RMI to invoke a method in a remote JVM, supplying a collection of objects as a parameter. The method in the remote JVM simply repackages those objects into a new collection, and returns the new collection back to the client.

The relative performance of Mobility-RPC versus RMI is then measured both as the size of each request is increased (increasing the number of objects in the collection), and as the concurrency of requests is increased (increasing the number of threads invoking the method simultaneously).

These aspects are measured independently, such that when testing handling of different request sizes the number of threads is kept constant at one thread, and when testing different levels of concurrency the sizes of requests are kept constant at one object in the collection.

Results show that Mobility-RPC outperforms RMI by wide margins both under high levels of concurrency and with larger request sizes, and that the gap in performance between the two widens as stress is increased in either dimension.

# Performance as concurrency is increased #

  * When only one thread is making requests, Mobility-RPC achieves 9% higher throughput (requests per second) than RMI and its latency per request is 8% lower than RMI
  * As concurrency is increased to 16 threads, the gap widens such that Mobility-RPC achieves 75% higher throughput than RMI, and its latency per request is 43% lower than RMI

<table>
<tr><td><img src='http://mobility-rpc.googlecode.com/svn/wiki/images/Throughput_varying_concurrency.png' /></td><td><img src='http://mobility-rpc.googlecode.com/svn/wiki/images/Latency_varying_concurrency.png' /></td></tr>
<tr><td>
<table cellpadding='4' border='1' cellspacing='0' width='100%'>
<tr><td>Throughput (requests per sec) (request size 1)</td></tr>
<tr><td>Client Threads</td><td>RMI</td><td>Mobility</td><td>Difference %</td></tr>
<tr><td>1</td><td>6316.8656016094</td><td>6881.2487759979</td><td>9%</td></tr>
<tr><td>2</td><td>4964.4404576678</td><td>5110.203062095</td><td>3%</td></tr>
<tr><td>4</td><td>3686.2629356611</td><td>4220.4152373703</td><td>14%</td></tr>
<tr><td>8</td><td>1871.790160643</td><td>2779.2508482786</td><td>48%</td></tr>
<tr><td>16</td><td>866.7432661862</td><td>1516.3832821947</td><td>75%</td></tr>
</table>
</td><td>
<table cellpadding='4' border='1' cellspacing='0' width='100%'>
<tr><td>Latency per request (ns) (request size 1)</td></tr>
<tr><td>Client Threads</td><td>RMI</td><td>Mobility</td><td>Difference %</td></tr>
<tr><td>1</td><td>158306.36</td><td>145322.46</td><td>-8%</td></tr>
<tr><td>2</td><td>201432.57</td><td>195686.94</td><td>-3%</td></tr>
<tr><td>4</td><td>271277.4475</td><td>236943.51</td><td>-13%</td></tr>
<tr><td>8</td><td>534247.92</td><td>359809.19125</td><td>-33%</td></tr>
<tr><td>16</td><td>1153744.18125</td><td>659463.878125</td><td>-43%</td></tr>
</table>
</td></tr>
</table>

# Performance as request size is increased #

  * When request size is small (one object in the collection), Mobility-RPC achieves 9% higher throughput (requests per second) than RMI and its latency per request is 8% lower than RMI
  * As request size is increased to 100 objects in the collection, the gap widens such that Mobility-RPC achieves 76% higher throughput than RMI, and its latency per request is 43% lower than RMI

<table>
<tr><td><img src='http://mobility-rpc.googlecode.com/svn/wiki/images/Throughput_varying_request_sizes.png' /></td><td><img src='http://mobility-rpc.googlecode.com/svn/wiki/images/Latency_varying_request_sizes.png' /></td></tr>
<tr><td>
<table cellpadding='4' border='1' cellspacing='0' width='100%'>
<tr><td>Throughput (requests per sec) (1 client thread)</td></tr>
<tr><td>Request Size</td><td>RMI</td><td>Mobility</td><td>Difference %</td></tr>
<tr><td>1</td><td>6316.8656016094</td><td>6881.2487759979</td><td>9%</td></tr>
<tr><td>10</td><td>3857.7698880383</td><td>5423.7491112509</td><td>41%</td></tr>
<tr><td>50</td><td>1494.6415903274</td><td>2723.9656101521</td><td>82%</td></tr>
<tr><td>100</td><td>944.4538186733</td><td>1663.6021728308</td><td>76%</td></tr>
</table>
</td><td>
<table cellpadding='4' border='1' cellspacing='0' width='100%'>
<tr><td>Latency per request (ns)  (1 client thread)</td></tr>
<tr><td>Request Size</td><td>RMI</td><td>Mobility</td><td>Difference %</td></tr>
<tr><td>1</td><td>158306.36</td><td>145322.46</td><td>-8%</td></tr>
<tr><td>10</td><td>259217.12</td><td>184374.31</td><td>-29%</td></tr>
<tr><td>50</td><td>669056.72</td><td>367111.83</td><td>-45%</td></tr>
<tr><td>100</td><td>1058813.02</td><td>601105.25</td><td>-43%</td></tr>
</table>
</td></tr>
</table>

# Benchmark Methodology #

The following is the method that both systems were required to invoke on the server. The method accepts a `Collection` of objects, and it repackages those objects into a new `Collection` (an `ArrayList`) which it then returns.
```
public class ServerBusinessLogic {
    public static <T extends Comparable<T>> Collection<T> processRequest(Collection<T> collection) {
        return new ArrayList<T>(collection);
    }
}
```


The following is the code to invoke the method across the network via Mobility-RPC. The design patterns for creating RMI server and client applications are well defined, so these are not shown but can be viewed in the benchmark source code [here](../code/src/test/java/com/googlecode/mobilityrpc/benchmarks/rmi/).
￼
```
// One-off initialisation...
MobilityController mobilityController = MobilityRPC.newController();
MobilitySession session = mobilityController.getSession(UUID.randomUUID());
ConnectionId connectionId = new ConnectionId("127.0.0.1", 5739);

final Collection<T> input = // logic to create an input collection for benchmark

Collection<T> result = session.execute(connectionId, ExecutionMode.RETURN_RESPONSE,
    new Callable<Collection<T>>() {
        public Collection<T> call() throws Exception {
            return ServerBusinessLogic.processRequest(input);
        }
    }
);
```

Note that Mobility-RPC only needs to be running on the remote machine. The `ServerBusinessLogic` class does not need to be deployed to the remote machine, it will be uploaded by the library automatically. It should also be noted, that if the class was deployed to the remote machine, as would be the case for RMI, it would not be transferred by the library.

Both client and server-side components of the benchmark were run on the same machine. This was intentional, because both RMI and Mobility systems have both client-side and server-side components, and this benchmark is an end-to-end evaluation.

The system used was a dual-core, hyperthreading-enabled Intel Core i7 1.8GHz Apple machine, with 4GB RAM, running Mac OS X 10.7.1, and the Apple-supplied Java HotSpot(TM) 64-Bit Server VM (build 20.1-b02-383, mixed mode) JVM. Only default JVM settings were used.

To simulate multiple concurrent requests, in the benchmark client multiple threads were started which executed the same benchmark code in parallel.

In each run of the benchmark, the benchmark logic was executed twice and the results from the first execution were discarded, to allow both code mobility and RMI client-side and server-side systems time to “warm up” (lazy-load any resources, initialize thread pools etc.). The RMI server and the Mobility server were restarted before each subsequent run.

In each run, threads performed 100,000 iterations (executing 100,000 remote invocations on the server). Average performance was subsequently calculated over the 100,000 iterations.

To test each system, a Collection of objects was created. Specifically Person objects, which contained fields for first name, last name, a list of two phone numbers, and fields for a personId, house number, street, city and country. Each Person object added to the collection was iteratively created to be unique.

Time taken client-side for the benchmark to create collections of Person objects was excluded.

The benchmark was then run with varying numbers of request threads and various request sizes. When testing varying numbers of threads, the request size was fixed at 1 for those sessions (1 Person object to be sent in the Collection to the server). Similarly when testing varying request sizes, the number of threads at was fixed at 1.

Full source code of the benchmark can be found [here](../code/src/test/java/com/googlecode/mobilityrpc/benchmarks/rmi/).