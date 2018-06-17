package com.example.che.maptest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by chehongshu on 2017/4/18.
 * E-mail : 1454045208@qq.com
 * qq : 1454045208
 */

/**
 *      SocketClient client = new SocketClient("123.206.176.72", 4869);
         System.out.println(client.sendMsg("chehongshu"));
         client.closeSocket();

         text1.setText("UUU");
 */
//  socket  通信
public class Socket_Client {
     static Socket client;
    BufferedReader br = null;
    PrintWriter out = null;

    /**
     * @param  site  地址
     * @param  port  端口
     */
     public Socket_Client(String site, int port){
               try{
                      client = new Socket(site,port);// 创建socket连接
                      System.out.println("Client is created! site:"+site+" port:"+port);
                    }catch (UnknownHostException e){
                      System.out.println("找不到socket连接，即socket连接失败");
                      e.printStackTrace();
                    }catch (IOException e){
                      System.out.println("IO流异常 socket连接失败");
                      e.printStackTrace();
                    }catch (Exception e)
                     {
                        e.printStackTrace();
                        }

            }

    /**
     *
     * @param  msg 发送信息
     * @return
     */
     public void sendMsg(String msg){

                try{
                    if(client.isConnected()) {

                        out = new PrintWriter(client.getOutputStream());
                        out.println(msg);
                        out.flush();

                    }else
                    {
                        System.out.println("没有连接上数据无法传送出去");
                    }

                     }catch(IOException e){
                        e.printStackTrace();
                    }finally {

                }

             }

    /**
     *
     * @return 收到的 信息
     */
     public String  receMsg()
     {

         String responseInfo=null;
         try {
             if(client.isConnected()) {

                  br = new BufferedReader(
                         new InputStreamReader(client.getInputStream()));
                 responseInfo = br.readLine();
                // Log.i("Socket Server", responseInfo);
             }else
             {
                 System.out.println("socket 没有连接");
             }

         }catch (IOException e)
         {
             e.printStackTrace();
         }finally {

         }
              return responseInfo;
     }

     public void closeSocket(){
               try{
                   if(client.isConnected()) {
                       /**
                        *  out关闭和 br关闭都会引起  socket的关闭。
                        */
                       client.close();
                       out.close();
                       br.close();
                   }
                   System.out.println("自己关闭了socket网络连接");
               }catch(IOException e)
               {
                   e.printStackTrace();
               }
            }
}
