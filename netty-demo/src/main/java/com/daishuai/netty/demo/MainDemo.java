package com.daishuai.netty.demo;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @ClassName MainDemo
 * @Author daishuai
 * @Date 2022/3/3 23:08
 * @Version 1.0
 */
public class MainDemo {

    public static void main(String[] args) {
        Executor executor = new ThreadPerTaskExecutor(new DefaultThreadFactory(MultithreadEventExecutorGroup.class));
        executor.execute(() -> {
            System.out.println(Thread.currentThread());
        });
        executor.execute(() -> {
            System.out.println(Thread.currentThread());
        });
    }
}
