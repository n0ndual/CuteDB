package org.tigeress;

import com.google.common.util.concurrent.ListenableFuture;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import redis.Command;
import redis.client.RedisClient;
import redis.reply.Reply;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clone redis-benchmark
 */
public class RedisBenchmark {
	private static final String help = ""
			+ "Usage: redis-benchmark [-h <host>] [-p <port>] [-c <clients>] [-n <requests]> [-P <pipelined>] [-d <data size>]\n"
			+ "\n"
			+ " -h <hostname>      Server hostname (default 127.0.0.1)\n"
			+ " -p <port>          Server port (default 6379)\n"
			+ " -c <clients>       Number of parallel connections (default 50)\n"
			+ " -n <requests>      Total number of requests (default 10000)\n"
			+ " -P <outstanding>   Number of outstanding pipeline requests (defaults 1)\n"
			+ " -d <size>          Data size of SET/GET value in bytes (default 3)\n";

	@Argument
	private static String h = "127.0.0.1";
	@Argument
	private static Integer PORT = 6379;
	@Argument
	private static Integer PARALLEL_CLIENTS = 100;
	@Argument
	private static Integer NUM_REQUESTS = 100000;
	@Argument
	private static Integer PAYLOAD = 3;
	@Argument
	private static Integer NUM_PERMITS = 1;

	private static final long NANOS_PER_MILLI = 1000000l;
	private static final int MILLIS_PER_SECOND = 1000;
	private static ExecutorService es = Executors.newFixedThreadPool(100);

	private static void benchmark(final String title, final Command command)
			throws IOException, InterruptedException, ExecutionException {
		System.out.println("====== " + title + " ======");
		final ArrayList<AtomicInteger> bins = new ArrayList<AtomicInteger>() {
			@Override
			public synchronized AtomicInteger get(int index) {
				if (index > size() - 1) {
					int toadd = index - size() + 1;
					for (int i = 0; i < toadd; i++) {
						add(new AtomicInteger(0));
					}
				}
				return super.get(index);
			}
		};
		List<Callable<Void>> benchmarks = new ArrayList<Callable<Void>>(PARALLEL_CLIENTS);
		for (int j = 0; j < PARALLEL_CLIENTS; j++) {
			benchmarks.add(new Callable<Void>() {
				@Override
				public Void call() throws IOException, InterruptedException {
					RedisClient redisClient = new RedisClient(h, PORT);
					final Semaphore semaphore = new Semaphore(NUM_PERMITS);
					for (int i = 0; i < NUM_REQUESTS / PARALLEL_CLIENTS; i++) {
						final long commandstart = System.nanoTime();
						if (NUM_PERMITS == 1) {
							redisClient.execute(title, command);
							long commandend = System.nanoTime();
							int bin = (int) ((commandend - commandstart) / NANOS_PER_MILLI);
							bins.get(bin).incrementAndGet();
						} else {
							semaphore.acquire(1);
							ListenableFuture<? extends Reply> pipeline = redisClient
									.pipeline(title, command);
							pipeline.addListener(new Runnable() {
								@Override
								public void run() {
									long commandend = System.nanoTime();
									int bin = (int) ((commandend - commandstart) / NANOS_PER_MILLI);
									bins.get(bin).incrementAndGet();
									semaphore.release();
								}
							}, es);
						}
					}
					semaphore.acquire(NUM_PERMITS);
					redisClient.close();
					return null;
				}
			});
		}
		long start = System.nanoTime();
		List<Future<Void>> futures = es.invokeAll(benchmarks);
		for (Future<Void> future : futures) {
			future.get();
		}
		long end = System.nanoTime();
		double seconds = ((double) end - start) / NANOS_PER_MILLI
				/ MILLIS_PER_SECOND;
		double rate = NUM_REQUESTS / seconds;
		System.out.printf("  %d requests completed in %.2f seconds\n", NUM_REQUESTS,
				seconds);
		System.out.printf("  %d parallel clients\n", PARALLEL_CLIENTS);
		System.out.printf("  %d outstanding requests\n", NUM_PERMITS);
		System.out.printf("  %d bytes payload\n", PAYLOAD);
		System.out.println();
		double total = 0;
		int milli = 0;
		for (AtomicInteger bin : bins) {
			total += bin.intValue();
			if (milli++ == 0) {
			//	System.out.printf("%.2f%% < 1 millisecond\n", total * 100 / NUM_REQUESTS);
			} else {
			//	System.out.printf("%.2f%% <= %d milliseconds\n", total * 100
			//		/ NUM_REQUESTS, milli);
			}
		}
		//System.out.println();
		//System.out.printf("%.2f requests per second\n\n", rate);
	}

	public static void main(String[] args) throws IOException,
			ExecutionException, InterruptedException {
		List<String> parse;
		try {
			parse = Args.parse(Benchmark.class, args);

			if (parse.size() > 0) {
				benchmark(parse.get(0), new Command(parse.toArray()));
			} else {
				byte[] key = "foo:rand:000000000000".getBytes();
				byte[] counter = "counter:rand:000000000000".getBytes();
				byte[] list = "mylist".getBytes();
				byte[] set = "myset".getBytes();
				byte[] data = new byte[PAYLOAD];
				Object[] objects = new Object[21];
				objects[0] = "MSET";
				for (int i = 1; i < objects.length - 1; i += 2) {
					objects[i] = key;
					objects[i + 1] = data;
				}
				// Delete it all
				RedisClient redisClient = new RedisClient(h, PORT);
				redisClient.del(new Object[] { key, counter, list, set });
				redisClient.close();

				benchmark("PING (warmup)",
						new Command(new Object[] { "PING".getBytes() }));
				// benchmark("PING", new Command(new
				// Object[]{"PING".getBytes()}));
				// benchmark("MSET", new Command(objects));
				benchmark("SET", new Command("SET".getBytes(), key, data));
				// benchmark("GET", new Command("GET".getBytes(), key));
				// benchmark("INCR", new Command("INCR".getBytes(), counter));
				// benchmark("LPUSH", new Command("LPUSH".getBytes(), list,
				// data));
				// benchmark("LPOP", new Command("LPOP".getBytes(), list));
				// benchmark("SADD", new Command("SADD".getBytes(), set,
				// counter));
				// benchmark("SPOP", new Command("SPOP".getBytes(), set));
				// benchmark("LPUSH (again, in order to bench LRANGE)", new
				// Command("LPUSH".getBytes(), list, data));
				// benchmark("LRANGE (first 100 elements)", new
				// Command("LRANGE".getBytes(), list, "0".getBytes(),
				// "99".getBytes()));
				// benchmark("LRANGE (first 300 elements)", new
				// Command("LRANGE".getBytes(), list, "0".getBytes(),
				// "299".getBytes()));
				// benchmark("LRANGE (first 450 elements)", new
				// Command("LRANGE".getBytes(), list, "0".getBytes(),
				// "449".getBytes()));
				// benchmark("LRANGE (first 600 elements)", new
				// Command("LRANGE".getBytes(), list, "0".getBytes(),
				// "599".getBytes()));
			}
		} catch (IllegalArgumentException e) {
			System.out.print(help);
			System.exit(1);
		} finally {
			es.shutdown();
		}
	}
}
