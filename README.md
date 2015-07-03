##What is this?##
I built a small sample cluster sharding application using Akka 2.3.6. After about 3 seconds cluster is ready and I can send message to cluster actors.

In another project I did the same but the cluster isn't ready and I get `sending HandOverToMe to [None]` message (10 times). It takes about 35 seconds for the cluster to be ready. 

After spending a day :(( I figured out that the problem just exists in akka-contrib `2.3.9` and `2.3.11`. My sample project which works as excepted is using akka-contrib `2.3.6`.

##Reproducing the problem##

* $ git clone git@github.com:AmirKarimi/akka-cluster-shardin-bug-2.3.9.git
* $ activator run
* The program works correctly and quits normally. You should find the following texts in output: 
```
************ first result: 0 ***************
************ second result: 1 ***************
```
* Then open `build.sbt` and change Akka version from `2.3.6` to `2.3.9` (or `2.3.11`)
* Run the program again: `activator run`
* You will get the following error:
```
[error] java.util.concurrent.TimeoutException: Futures timed out after [5 seconds]
[error] 	at scala.concurrent.impl.Promise$DefaultPromise.ready(Promise.scala:219)
[error] 	at scala.concurrent.impl.Promise$DefaultPromise.result(Promise.scala:223)
[error] 	at scala.concurrent.Await$$anonfun$result$1.apply(package.scala:116)
[error] 	at scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2$$anon$4.block(ExecutionContextImpl.scala:48)
[error] 	at scala.concurrent.forkjoin.ForkJoinPool.managedBlock(ForkJoinPool.java:3640)
[error] 	at scala.concurrent.impl.ExecutionContextImpl$DefaultThreadFactory$$anon$2.blockOn(ExecutionContextImpl.scala:45)
[error] 	at scala.concurrent.Await$.result(package.scala:116)
[error] 	at com.example.ApplicationMain$$anonfun$main$1.apply$mcV$sp(ApplicationMain.scala:39)
[error] 	at akka.actor.Scheduler$$anon$7.run(Scheduler.scala:117)
[error] 	at scala.concurrent.impl.ExecutionContextImpl$AdaptedForkJoinTask.exec(ExecutionContextImpl.scala:121)
[error] 	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
[error] 	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
[error] 	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
[error] 	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
``` 

