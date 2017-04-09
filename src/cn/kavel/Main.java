package cn.kavel;

import cn.kavel.httpserver.ServerSocketRunnable;

public class Main {

    public static void main(String[] args) {
	// write your code here
        new Thread(new ServerSocketRunnable(80)).start();
    }
}
