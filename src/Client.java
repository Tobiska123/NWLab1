import javax.swing.*;
import javax.swing.text.View;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {
    protected MulticastSocket multicastSocket = null;
    protected byte[]buf = new byte[256];
    int port;
    protected SocketAddress socketAddress = null;
    protected Map<InetAddress,Member> connectionMap = new HashMap<>();

    Client(String inetAddress, int port) throws IOException {
        InetAddress group = InetAddress.getByName(inetAddress);
        this.port = port;
        this.multicastSocket = new MulticastSocket(port);
        this.socketAddress = new InetSocketAddress(group,port) ;
        this.multicastSocket.joinGroup(socketAddress,NetworkInterface.getByInetAddress(group));
        this.multicastSocket.setSoTimeout(1000);
    }

    void goCast(){
        try {
            while (receiveMsg() != -1) {
                checkMembers();
                sendMsg("Hello");
                Thread.sleep(100);
            }
            multicastSocket.close();
            System.out.println("---Finished---");
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    int receiveMsg(){
        DatagramPacket packetRecv = new DatagramPacket(buf, buf.length);
        try{
            multicastSocket.receive(packetRecv);
            String received = new String(packetRecv.getData(), 0, packetRecv.getLength());
            if (received == "escape")
                return -1;
            InetSocketAddress itemAddr = new InetSocketAddress(packetRecv.getAddress(), packetRecv.getPort());
            InetAddress tmpAddr =  itemAddr.getAddress();
            Member member = new Member(0,itemAddr);
            if(!connectionMap.containsKey(tmpAddr)) {
                connectionMap.put(tmpAddr, member);
                connectionMap.get(tmpAddr).specialMsg.add(received);
                System.out.println(member.toString() + " has joined");
            }else if(!connectionMap.get(tmpAddr).specialMsg.contains(received)){
                connectionMap.get(tmpAddr).specialMsg.add(received);
                System.out.println(connectionMap.get(tmpAddr).toString() + " sent these messages");
            }
            connectionMap.get(tmpAddr).ttl = 5;
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return 0;
    }

    void sendMsg(String str){
        DatagramPacket packetSend = new DatagramPacket(buf, buf.length,socketAddress);
        try {
            packetSend.setData(str.getBytes());
            multicastSocket.send(packetSend);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    void checkMembers(){
        boolean printFlag = false;
        for(Iterator<Map.Entry<InetAddress,Member>> iterator = connectionMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<InetAddress,Member> tmpItem = iterator.next();
            if(tmpItem.getValue().ttl-- < 0) {
                System.out.println(tmpItem.getValue().toString() + " has deleted");
                iterator.remove();
                printFlag = true;
            }
        }
        if(printFlag) {
            for(int i = 0;i< 10;i++)
                System.out.println("     |\n");
            System.out.println("Other users online: \n");
            for (Map.Entry<InetAddress, Member> it : connectionMap.entrySet())
                System.out.println(it.toString() + " has joined");
        }
    }


    class Member{
        public int ttl = 0;
        public ArrayList<String> specialMsg = new ArrayList<String>();
        public SocketAddress itemAddr;
        Member(int ttl,SocketAddress itemAddr){
            this.ttl = ttl;
            this.itemAddr = itemAddr;
        }

        @Override
        public String toString() {
            return
                    "itemAddr= " + itemAddr +  " with special msg: " + specialMsg;
        }
    }

}