import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Member;
import java.net.*;
import java.util.*;

public class Client {
    protected MulticastSocket multicastSocket = null;
    protected byte[]buf = new byte[256];
    int port;
    protected SocketAddress socketAddress = null;
    protected Map<String,Member> connectionMap = new HashMap<>();

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
            while (true) {
                Thread.sleep(100);
                receiveMsg();
                checkMembers();
                sendMsg();
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }


    void receiveMsg(){
        DatagramPacket packetRecv = new DatagramPacket(buf, buf.length);
        try{
            multicastSocket.receive(packetRecv);
            String received = new String(packetRecv.getData(), 0, packetRecv.getLength());
            SocketAddress itemAddr = new InetSocketAddress(packetRecv.getAddress(), packetRecv.getPort());
            Member member = new Member(0,received,itemAddr);
            if(!connectionMap.containsKey(received)) {
                connectionMap.put(received, member);
                /*for (Map.Entry<String, Member> it : connectionMap.entrySet()) {
                    System.out.println(it.toString() + " has joined" + " with special msg: " + it.getValue().specialMsg);
                }*/
                System.out.println(member.toString() + " has joined" + " with special msg: " + member.specialMsg);
            }
            connectionMap.get(received).ttl = 10;
            //System.out.println(member.toString() + " has joined" + " with special msg: " + received)}
            //System.out.println("i've got "+received);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    void sendMsg(){
        DatagramPacket packetSend = new DatagramPacket(buf, buf.length,socketAddress);
        try {
            packetSend.setData("111".getBytes());
            multicastSocket.send(packetSend);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    void checkMembers(){
        Set tmpSet = connectionMap.entrySet();
        for(Iterator<Map.Entry<String,Member>> iterator = connectionMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String,Member> tmpItem = iterator.next();
            if(tmpItem.getValue().ttl-- == 0) {
                System.out.println(tmpItem.getValue().toString() + " has deleted" + " with special msg: " + tmpItem.getValue().specialMsg);
                iterator.remove();
            }
        }
    }


    class Member{
        public int ttl = 0;
        public String specialMsg = null;
        public SocketAddress itemAddr;
        Member(int ttl,String specialMsg,SocketAddress itemAddr){
            this.ttl = ttl;
            this.specialMsg = specialMsg;
            this.itemAddr = itemAddr;
        }

        @Override
        public String toString() {
            return
                    "ttl= " + ttl +
                            " itemAddr= " + itemAddr;
        }
    }

}