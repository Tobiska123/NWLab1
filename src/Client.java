import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client extends JFrame {
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
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt){
                switch (evt.getKeyChar()) {
                    case KeyEvent.VK_ESCAPE:
                        fnHandler();
                }
            }
        });
    }


    void fnHandler(){
        for(int i = 0 ;i< 3;i++) {
            sendMsg("escape");
        }
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
            if (received == "escape") {
                return -1;
            }
            InetSocketAddress itemAddr = new InetSocketAddress(packetRecv.getAddress(), packetRecv.getPort());
            InetAddress tmpAddr =  itemAddr.getAddress();
            Member member = new Member(0,received,itemAddr);
            if(!connectionMap.containsKey(tmpAddr)) {
                connectionMap.put(tmpAddr, member);
                System.out.println(member.toString() + " has joined");
            }
            connectionMap.get(tmpAddr).ttl = 10;
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
                    "itemAddr= " + itemAddr +  " with special msg: " + specialMsg;
        }
    }

}