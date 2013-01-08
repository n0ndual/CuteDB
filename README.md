CuteDB
======

a key-vlaue database outperforms redis，has a protocol compatible with redis. Recently it can be seen as Just A Java Version of Redis.

Though it's written in java, CuteDB does outperform redis which is written in C++. The main reason is the CuterServer which is a light-weight event-driven server framework and might be the fastest one.

CuteServer like netty, implement the SEDA model, but solved the performance problem by taking advantage of Disruptor Framwork.

It may seems naive now, but I believe this kind of approach has a great future, cause high-performance server, im-memory database, transactional memory, distribution management are those areas that Java are good at.




