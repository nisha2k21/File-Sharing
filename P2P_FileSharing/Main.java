package com.P2P_FileSharing;



import java.io.*;

public class Main {
     static FileWriter filewriter;
     static BufferedWriter bufferwriter;
    public static void write(String message) {
        try{

            synchronized (filewriter) {

                System.out.print(message);
                bufferwriter.write(message);
                bufferwriter.newLine();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            System.out.println("Exception has occurred");
        }

    }

    public static class MyThread extends Thread {
        public void run(){
            while(Boolean.TRUE) {
                try{
                    write("thread1");
                }
                catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception has occurred");
                }

            }

        }
    }
    public static class MyThread1 extends Thread {
        public void run(){
            while(Boolean.TRUE) {
                try{
                    write("thread2");
                }
                catch(Exception e) {
                    System.out.println("Exception has occurred");
                }
            }
        }
    }
    public static void main(String[] args) {
        try {
            filewriter = new FileWriter("Peer.txt", true);
            bufferwriter = new BufferedWriter(filewriter);
             Thread t1 = new MyThread();
             Thread t2 = new MyThread1();
            Thread t11 = new MyThread();
            Thread t22 = new MyThread1();

             t1.start();
             t2.start();
            t11.start();
            t22.start();
             Thread.sleep(100);
             t1.stop();
             t2.stop();
            t11.stop();
            t22.stop();
            bufferwriter.close();
        }
        catch (Exception e) {
            System.out.println("Exception has occurred");
        }

    }
}




