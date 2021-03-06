package com.bsrakdg.coroutinesguide

import kotlinx.coroutines.*

/** https://kotlinlang.org/docs/reference/coroutines/coroutine-context-and-dispatchers.html
 *  Coroutines always execute in some context represented by a value of the CoroutineContext type,
 *  defined in the Kotlin standard library.
 *
 *  The coroutine context is a set of various elements. The main elements are the Job of the coroutine,
 *  which we've seen before, and its dispatcher, which is covered in this section.
 */

fun main() {

    // TODO Dispatchers and threads
    dispatchersAndThreads()

    println("\n****************************\n")

    // TODO Unconfined vs confined dispatcher
    unconfinedConfinedDispatcher()

    println("\n****************************\n")

    // TODO Debugging coroutines and threads
    debuggingCoroutinesAndThreads()

    // TODO Debugging using logging
    debuggingUsingLogging()

    println("\n****************************\n")

    // TODO Jumping between threads
    jumpingBetweenThreads()

    println("\n****************************\n")

    // TODO Job in the context
    jobInTheContext()

    println("\n****************************\n")

    // TODO Children of a coroutine
    childrenOfCoroutine()

    println("\n****************************\n")

    // TODO Parental responsibilities
    parentalResponsibilities()

    println("\n****************************\n")

    // TODO Naming coroutines for debugging
    namingCoroutinesForDebugging()

    println("\n****************************\n")

    // TODO Combining context elements
    combiningContextElements()

    println("\n****************************\n")

    // TODO Coroutine scope
    coroutineScopeInformation()

    println("\n****************************\n")

    // TODO Thread-local data
    threadLocalData()
}

fun dispatchersAndThreads() {
    /*
        The coroutine context includes a coroutine dispatcher (see CoroutineDispatcher) that determines
        what thread or threads the corresponding coroutine uses for its execution.
        The coroutine dispatcher can confine coroutine execution to a specific thread,
        dispatch it to a thread pool, or let it run unconfined.

        All coroutine builders like launch and async accept an optional CoroutineContext parameter that
        can be used to explicitly specify the dispatcher for the new coroutine and other context elements.

        Try the following example:
     */

    dispatchersAndThreadsSample()
}

fun dispatchersAndThreadsSample() = runBlocking<Unit> {

        /*
            When launch { ... } is used without parameters, it inherits the context (and thus dispatcher)
            from the CoroutineScope it is being launched from. In this case, it inherits the context of
            the main runBlocking coroutine which runs in the main thread.
         */
        launch { // context of the parent, main runBlocking coroutine
            println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }

        /*
             Dispatchers.Unconfined is a special dispatcher that also appears to run in the main thread,
             but it is, in fact, a different mechanism that is explained later.
         */
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
        }

        /*
            The default dispatcher that is used when coroutines are launched in GlobalScope is
            represented by Dispatchers.Default and uses a shared background pool of threads,
            so launch(Dispatchers.Default) { ... } uses the same dispatcher as GlobalScope.launch { ... }.
         */
        launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
            println("Default               : I'm working in thread ${Thread.currentThread().name}")
        }

        /*
            newSingleThreadContext creates a thread for the coroutine to run. A dedicated thread is a very expensive resource.
            In a real application it must be either released, when no longer needed, using the close function,
            or stored in a top-level variable and reused throughout the application.
         */
        launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
            println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }

}

fun unconfinedConfinedDispatcher() {
    /*
        The Dispatchers.Unconfined coroutine dispatcher starts a coroutine in the caller thread,
        but only until the first suspension point. After suspension it resumes the coroutine in
        the thread that is fully determined by the suspending function that was invoked.
        The unconfined dispatcher is appropriate for coroutines which neither consume CPU time nor
        update any shared data (like UI) confined to a specific thread.

        On the other side, the dispatcher is inherited from the outer CoroutineScope by default.
        The default dispatcher for the runBlocking coroutine, in particular, is confined to the invoker thread,
        so inheriting it has the effect of confining execution to this thread with predictable FIFO scheduling.
     */

    unconfinedConfinedDispatcherSample()
}

fun unconfinedConfinedDispatcherSample() = runBlocking<Unit> {

    launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
    launch { // context of the parent, main runBlocking coroutine
        println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
    }

    /*
        So, the coroutine with the context inherited from runBlocking {...} continues to execute
        in the main thread, while the unconfined one resumes in the default executor thread that
        the delay function is using.

        The unconfined dispatcher is an advanced mechanism that can be helpful in certain corner
        cases where dispatching of a coroutine for its execution later is not needed or produces
        undesirable side-effects, because some operation in a coroutine must be performed right away.
        The unconfined dispatcher should not be used in general code.
     */
}

fun debuggingCoroutinesAndThreads() {
    /*
        Coroutines can suspend on one thread and resume on another thread. Even with a single-threaded
        dispatcher it might be hard to figure out what the coroutine was doing, where,
        and when if you don't have special tooling.

        Debugging with IDEA : Debugging works for versions 1.3.8 or later of kotlinx-coroutines-core.
        The Debug Tool Window contains a Coroutines tab. In this tab, you can find information about
        both currently running and suspended coroutines. The coroutines are grouped by the dispatcher they are running on.

        You can:
        - Easily check the state of each coroutine.
        - See the values of local and captured variables for both running and suspended coroutines.
        - See a full coroutine creation stack, as well as a call stack inside the coroutine.
        The stack includes all frames with variable values, even those that would be lost during standard debugging.

        If you need a full report containing the state of each coroutine and its stack,
        right-click inside the Coroutines tab, and then click Get Coroutines Dump.

        https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-rc-debugging-coroutines/
     */
}

fun debuggingUsingLogging() {
    /*
        Another approach to debugging applications with threads without Coroutine Debugger is to
        print the thread name in the log file on each log statement. This feature is universally
        supported by logging frameworks. When using coroutines, the thread name alone does not give
        much of a context, so kotlinx.coroutines includes debugging facilities to make it easier.

        Run the following code with -Dkotlinx.coroutines.debug JVM option:
     */
    debuggingUsingLoggingSample()
}

fun debuggingUsingLoggingSample() = runBlocking<Unit> {
    val a = async {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async {
        log("I'm computing another piece of the answer")
        7
    }
    log("The answer is ${a.await() * b.await()}")

    /*
        There are three coroutines. The main coroutine (#1) inside runBlocking and two coroutines
        computing the deferred values a (#2) and b (#3). They are all executing in the context of
        runBlocking and are confined to the main thread.
     */
}

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun jumpingBetweenThreads() {
    /*
        Run the following code with the -Dkotlinx.coroutines.debug JVM option (see debug):
     */
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }

    /*
        It demonstrates several new techniques. One is using runBlocking with an explicitly
        specified context, and the other one is using the withContext function to change the context
        of a coroutine while still staying in the same coroutine, as you can see in the output.

        Note that this example also uses the use function from the Kotlin standard library to release
        threads created with newSingleThreadContext when they are no longer needed.
     */
}

fun jobInTheContext() {
    /*
        The coroutine's Job is part of its context, and can be retrieved from it using the coroutineContext[Job] expression:
     */
    jobInTheContextSample()

    /*
        Note that isActive in CoroutineScope is just a convenient shortcut for
        coroutineContext[Job]?.isActive == true.
     */
}

fun jobInTheContextSample() = runBlocking<Unit> {
    println("My job is ${coroutineContext[Job]}")
}

fun childrenOfCoroutine() {
    /*
        When a coroutine is launched in the CoroutineScope of another coroutine, it inherits its
        context via CoroutineScope.coroutineContext and the Job of the new coroutine becomes a child
        of the parent coroutine's job. When the parent coroutine is cancelled, all its children are
        recursively cancelled, too.

        However, when GlobalScope is used to launch a coroutine, there is no parent for the job of
        the new coroutine. It is therefore not tied to the scope it was launched from and operates independently.
     */
    childrenOfCoroutineSample()
}

fun childrenOfCoroutineSample() = runBlocking<Unit> {
    // launch a coroutine to process some kind of incoming request
    val request = launch {
        // it spawns two other jobs, one with GlobalScope
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")
        }
        // and the other inherits the parent context
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")
        }
    }
    delay(500)
    request.cancel() // cancel processing of the request
    delay(1000) // delay a second to see what happens
    println("main: Who has survived request cancellation?")
}

fun parentalResponsibilities() {
    /*
        A parent coroutine always waits for completion of all its children.
        A parent does not have to explicitly track all the children it launches,
        and it does not have to use Job.join to wait for them at the end:
     */
    // launch a coroutine to process some kind of incoming request
    parentalResponsibilitiesSample()
}

fun parentalResponsibilitiesSample() = runBlocking {
    println("parentalResponsibilitiesSample start")

    val request = launch {
        repeat(3) { i -> // launch a few children jobs
            launch {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                println("Coroutine $i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join() // wait for completion of the request, including all its children
    println("Now processing of the request is complete")

    println("parentalResponsibilitiesSample end")
}

fun namingCoroutinesForDebugging() {
    /*
        Automatically assigned ids are good when coroutines log often and
        you just need to correlate log records coming from the same coroutine.
        However, when a coroutine is tied to the processing of a specific request or doing some
        specific background task, it is better to name it explicitly for debugging purposes.
        The CoroutineName context element serves the same purpose as the thread name.
        It is included in the thread name that is executing this coroutine when the debugging mode is turned on.
     */

    namingCoroutinesForDebuggingSample()
}

fun namingCoroutinesForDebuggingSample() = runBlocking {

    println("namingCoroutinesForDebuggingSample start")

    log("Started main coroutine")

    // run two background value computations
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        252
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")

    println("namingCoroutinesForDebuggingSample end")
}

fun combiningContextElements() {
    /*
        Sometimes we need to define multiple elements for a coroutine context.
        We can use the + operator for that. For example, we can launch a coroutine with an explicitly
        specified dispatcher and an explicitly specified name at the same time:
     */
    combiningContextElementsSample()
}

fun combiningContextElementsSample() = runBlocking {

    println("combiningContextElementsSample start")

    launch(Dispatchers.Default + CoroutineName("test")) {
        println("I'm working in thread ${Thread.currentThread().name}")
    }

    println("combiningContextElementsSample end")
}

fun coroutineScopeInformation() {
    /*
        Let us put our knowledge about contexts, children and jobs together.
        Assume that our application has an object with a lifecycle, but that object is not a coroutine.
        For example, we are writing an Android application and launch various coroutines in
        the context of an Android activity to perform asynchronous operations to fetch and update data,
        do animations, etc. All of these coroutines must be cancelled when the activity is destroyed
        to avoid memory leaks. We, of course, can manipulate contexts and jobs manually to tie
        the lifecycles of the activity and its coroutines, but kotlinx.coroutines provides an abstraction
        encapsulating that: CoroutineScope. You should be already familiar with the coroutine scope
        as all coroutine builders are declared as extensions on it.

        We manage the lifecycles of our coroutines by creating an instance of CoroutineScope tied to
        the lifecycle of our activity. A CoroutineScope instance can be created by the CoroutineScope()
        or MainScope() factory functions. The former creates a general-purpose scope, while the latter
        creates a scope for UI applications and uses Dispatchers. Look at ActivitySample 1*

        Now, we can launch coroutines in the scope of this Activity using the defined scope.
        For the demo, we launch ten coroutines that delay for a different time:
        Look at ActivitySample 2*

        In our main function we create the activity, call our test doSomething function,
        and destroy the activity after 500ms. This cancels all the coroutines that were launched
        from doSomething. We can see that because after the destruction of the activity
        no more messages are printed, even if we wait a little longer.

     */
    coroutineScopeInformationSample()
}

fun coroutineScopeInformationSample() = runBlocking {
    println("coroutineScopeInformationSample start")

    val activity = ActivitySample()
    activity.doSomething() // run test function
    println("Launched coroutines")
    delay(500L) // delay for half a second
    println("Destroying activity!")
    activity.destroy() // cancels all coroutines
    delay(1000) // visually confirm that they don't work

    println("coroutineScopeInformationSample end")
}

class ActivitySample { // 1*
    private val mainScope = CoroutineScope(Dispatchers.Default) // use Default for test purposes

    fun destroy() {
        mainScope.cancel()
    }

    fun doSomething() {
        // launch ten coroutines for a demo, each working for a different time
        repeat(10) { i ->
            mainScope.launch {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, ... etc
                println("Coroutine $i is done")
            }
        }
    }

    /*
        As you can see, only the first two coroutines print a message and the others are cancelled
        by a single invocation of job.cancel() in Activity.destroy().

        Note, that Android has first-party support for coroutine scope in all entities with the lifecycle.
        See the corresponding documentation : https://developer.android.com/topic/libraries/architecture/coroutines#lifecyclescope
     */
}


fun threadLocalData() {

    /*
        Sometimes it is convenient to have an ability to pass some thread-local data to or between coroutines.
        However, since they are not bound to any particular thread, this will likely lead to boilerplate if done manually.

        For ThreadLocal, the asContextElement extension function is here for the rescue.
        It creates an additional context element which keeps the value of the given ThreadLocal and
        restores it every time the coroutine switches its context.

        It is easy to demonstrate it in action:
     */

    threadLocalDataSample()
}

val threadLocal = ThreadLocal<String?>() // declare thread-local variable

fun threadLocalDataSample() = runBlocking{
    println("threadLocalDataSample start")

    threadLocal.set("main")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")

    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }

    job.join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")

    println("threadLocalDataSample end")

    /*
      In this example we launch a new coroutine in a background thread pool using Dispatchers.Default,
      so it works on a different thread from the thread pool, but it still has the value of
      the thread local variable that we specified using threadLocal.asContextElement(value = "launch"),
      no matter which thread the coroutine is executed on.

      It's easy to forget to set the corresponding context element.
      The thread-local variable accessed from the coroutine may then have an unexpected value,
      if the thread running the coroutine is different. To avoid such situations,
      it is recommended to use the ensurePresent method and fail-fast on improper usages.
      https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/java.lang.-thread-local/ensure-present.html

      Alternatively, a value can be stored in a mutable box like class Counter(var i: Int), which is,
      in turn, stored in a thread-local variable. However, in this case you are fully responsible to
      synchronize potentially concurrent modifications to the variable in this mutable box.

      For advanced usage, for example for integration with logging MDC, transactional contexts or
      any other libraries which internally use thread-locals for passing data, see the documentation
      of the ThreadContextElement interface that should be implemented.
      https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-thread-context-element/index.html
   */
}