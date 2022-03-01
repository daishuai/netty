package com.daishuai.netty.demo;

import io.netty.util.Recycler;

/**
 * @ClassName RecyclerDemo
 * @Author daishuai
 * @Date 2022/3/1 22:17
 * @Version 1.0
 */
public class RecyclerDemo {

    private static final Recycler<User> RECYCLER = new Recycler<User>() {
        @Override
        protected User newObject(Handle<User> handle) {
            return new User(handle);
        }
    };

    public static void main(String[] args) {
        User user = RECYCLER.get();
        user.recycle();
        User user1 = RECYCLER.get();
        System.out.println(user1 == user);
    }

    public static class User {
        private final Recycler.Handle<User> handle;

        public User(Recycler.Handle<User> handle) {
            this.handle = handle;
        }

        public void recycle() {
            this.handle.recycle(this);
        }
    }
}
