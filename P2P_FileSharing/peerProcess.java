package com.P2P_FileSharing;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;


class PeerInfo {
    private String peer_id;
    private String host_Name;
    private int port_no;
    private int hasFile;

    public PeerInfo(String peer_id, String host_Name, int port_no, int hasFile) {
        this.peer_id = peer_id;
        this.host_Name = host_Name;
        this.port_no = port_no;
        this.hasFile = hasFile;
    }

    public String getPeerID() {
        return peer_id;
    }

    public void setPeerID(String peer_id) {
        this.peer_id = peer_id;
    }

    public String getHost_Name() {
        return host_Name;
    }

    public void setHost_Name(String host_Name) {
        this.host_Name = host_Name;
    }

    public int getPort_no() {
        return port_no;
    }

    public void setPort_no(int port_no) {
        this.port_no = port_no;
    }

    public int getHasFile() {
        return hasFile;
    }

    public void setHasFile(int hasFile) {
        this.hasFile = hasFile;
    }
}

class DownloadRateNode {
    String peerId;
    Integer rate_download;
    public DownloadRateNode(String peerId, Integer rate_download) {
        this.peerId = peerId;
        this.rate_download = rate_download;
    }
}

class SelectionNode {
    int priority;
    int index;

    public SelectionNode(int priority, int index) {
        this.priority = priority;
        this.index = index;
    }
}

public class peerProcess {
    static Peer peer;
    static String peerID;
    public static void main(String[] args) throws Exception {
        if(args.length != 0)
            peerID = args[0];
        peer = new Peer(peerID, new Config());
        peer.getConfig().load();
        peer.readPeerInfo();
        pieceSelectionAlgorithm();
        if(peer.getHasFileInitially())
            fileSplitter();
        Server server = new Server(peer.getPort(), peer);
        peer.setServer(server);
        server.start();

        for(int i = 0; i < peer.getPeersToConnect().size();i++)
        {
            Socket requestSocket = null;
            try {
                requestSocket = new Socket(peer.getPeersToConnect().get(i).getHost_Name(), peer.getPeersToConnect().get(i).getPort_no());
                System.out.println("Connected to " + peer.getPeersToConnect().get(i).getHost_Name() + " in port" + " " + peer.getPeersToConnect().get(i).getPort_no());
                ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
                HandShake handShake = new HandShake(peer.getPeerID());
                out.writeObject(handShake);
                peer.writeToLog("["+ Instant.now() + "]: Peer [" + peerID +"] makes a connection to Peer [" + peer.getPeersToConnect().get(i).getPeerID()+"]");
                System.out.println("Sending Handshake message to" + peer.getPeersToConnect().get(i).getPeerID());

                Map<String, String> handShakeStatus = peer.getHandShakeStatus();
                handShakeStatus.put(peer.getPeersToConnect().get(i).getPeerID(), "Sent");
                peer.setHandShakeStatus(handShakeStatus);
                ConnectionDetails connectionDetails = new ConnectionDetails(peer.getPeersToConnect().get(i).getPeerID(), requestSocket, out, in, peer, new ConcurrentLinkedQueue<Object>());
                peer.getConnections().add(connectionDetails);
                peer.getPeerToConnections().put(peer.getPeersToConnect().get(i).getPeerID(), connectionDetails);

                connectionDetails.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        startTimers();

        System.out.println("Completed");
        System.exit(0);




    }

    public static void startTimers() throws InterruptedException {
        Thread timers = new Thread(new Runnable() {
            @Override
            public void run() {
            	System.out.println("in timers " + Thread.currentThread().getName());
                final ScheduledExecutorService preferredNeighborsScheduler = Executors.newScheduledThreadPool(1);
                final ScheduledExecutorService optimisticNeighborScheduler = Executors.newScheduledThreadPool(1);
                Runnable preferredNeighbors = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendChoke();
                            selectPreferredNeighbors();
                            sendUnChoke();
                            StringBuilder sb = new StringBuilder("["+ Instant.now() + "]: Peer [" + peerID +"] has the preferred neighbors [");
                            for(String s : peer.getPreferredNeighbors()) {
                                sb.append(s + ", ");
                            }
                            sb.append("]");
                            peer.writeToLog(sb.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Runnable optimisticNeighbor = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            selectOptimisticNeighbor();
                            StringBuilder sb = new StringBuilder("["+ Instant.now() + "]: Peer [" + peerID +"] has the optimistically unchoked neighbor [");
                            for(String string : peer.getOptimisticNeighbor()) {
                                sb.append(string + ", ");
                            }
                            sb.append("]");
                            peer.writeToLog(sb.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                final ScheduledFuture<?> preferredNeighborsHandle = preferredNeighborsScheduler.scheduleAtFixedRate(preferredNeighbors, 0, Config.UNCHOKINGINTERVAL, TimeUnit.SECONDS);
                final ScheduledFuture<?> optimisticNeighborHandle = optimisticNeighborScheduler.scheduleAtFixedRate(optimisticNeighbor, 1, Config.OPTIMISTICUNCHOKINGINTERVAL, TimeUnit.SECONDS);

                while(true) {
                    try {
                        Thread.sleep(3000);
                        if(isDone()) {
                            System.out.println("Stopping timers");
                            preferredNeighborsHandle.cancel(true);
                            optimisticNeighborHandle.cancel(true);
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        while(peer.getInterested().size() == 0) {
            Thread.sleep(1000);
        }
        timers.start();
        while(timers.isAlive()) {
            Thread.sleep(1000);
        }


    }

    public static void sendChoke() throws IOException {

        Set<String> unChoked = peer.getUnChoked();
        Set<String> choked = peer.getChoked();
        Map<String, ConnectionDetails> peerToConnections = peer.getPeerToConnections();
        Set<String> preferredNeighbors = peer.getPreferredNeighbors();

        for(String peerID : preferredNeighbors) {
            ConnectionDetails connectionDetails = peerToConnections.get(peerID);
            connectionDetails.send(new ActualMessage(0, null));
            choked.add(peerID);
            unChoked.remove(peerID);
        }

        preferredNeighbors.clear();
        peer.setPreferredNeighbors(preferredNeighbors);
        peer.setUnChoked(unChoked);
        peer.setChoked(choked);
    }

    public static void selectPreferredNeighbors() throws IOException {
        Set<String> choked = peer.getChoked();
        Set<String> unChoked = peer.getUnChoked();
        Set<String> interested = peer.getInterested();
        Set<String> preferredNeighbors = new HashSet<>();
        ConcurrentMap<String, Integer> downloadRate = peer.getDownloadRate();
        PriorityQueue<DownloadRateNode> pq = new PriorityQueue<DownloadRateNode>((a, b) -> b.rate_download - a.rate_download);

        for(String key : downloadRate.keySet()) {
            pq.add(new DownloadRateNode(key, downloadRate.get(key)));
            downloadRate.put(key, 0);
        }

        int preferredNeighborCount = Config.NUMBEROFPREFERREDNEIGHBORS;
        while(preferredNeighborCount > 0 && pq.size() != 0) {
            DownloadRateNode node = pq.poll();
            if(interested.contains(node.peerId)) {
                unChoked.add(node.peerId);
                choked.remove(node.peerId);

                preferredNeighbors.add(node.peerId);
                preferredNeighborCount--;
            }
        }

        peer.setPreferredNeighbors(preferredNeighbors);
        peer.setUnChoked(unChoked);
        peer.setChoked(choked);
        peer.setInterested(interested);

        peer.setDownloadRate(downloadRate);
    }

    public static void sendUnChoke() throws IOException {

        Map<String, ConnectionDetails> peerToConnections = peer.getPeerToConnections();

        for(String peerId : peer.getPreferredNeighbors()) {
            ConnectionDetails connectionDetails = peerToConnections.get(peerId);
            connectionDetails.send(new ActualMessage(1, null));
        }

    }

    public static void selectOptimisticNeighbor() throws IOException {

        Set<String> optimisticNeighbor = peer.getOptimisticNeighbor();
        Set<String> interested = peer.getInterested();
        Set<String> choked = peer.getChoked();
        Set<String> unChoked = peer.getUnChoked();
        List<String> interestedAndChoked = new ArrayList<>();
        Map<String, ConnectionDetails> peerToConnections = peer.getPeerToConnections();

        for(String peerId : optimisticNeighbor) {
            ConnectionDetails connectionDetails = peerToConnections.get(peerId);
            connectionDetails.send(new ActualMessage(0, null));
            optimisticNeighbor.remove(peerId);
            choked.add(peerId);
        }

        for(String peerId : interested) {
            if(choked.contains(peerId)) {
                interestedAndChoked.add(peerId);
            }
        }

        Collections.shuffle(interestedAndChoked);

        if(interestedAndChoked.size() != 0) {
            String peerId = interestedAndChoked.get(0);
            ConnectionDetails connectionDetails = peerToConnections.get(peerId);
            connectionDetails.send(new ActualMessage(1, null));
            unChoked.add(peerId);
            choked.remove(peerId);
            optimisticNeighbor.add(peerId);
        }

        peer.setChoked(choked);
        peer.setUnChoked(unChoked);
        peer.setInterested(interested);
        peer.setOptimisticNeighbor(optimisticNeighbor);
    }

    public static void fileSplitter() throws IOException {

        File file = new File(Config.FILENAME);
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferinputstream = new BufferedInputStream(fileInputStream);

        byte[] contents;
        long file_len = file.length();
        long curr = 0;
        int ind = 0;

        ConcurrentMap<Integer, byte[]> pieceAtIndex = new ConcurrentHashMap<>();

        while(curr < file_len){
            int size = Config.PIECESIZE;
            if(file_len - curr >= size)
                curr += size;
            else{
                size = (int)(file_len - curr);
                curr = file_len;
            }
            contents = new byte[size];
            bufferinputstream.read(contents, 0, size);
            pieceAtIndex.put(ind, contents);
            ind++;
        }

        peer.setPieceAtIndex(pieceAtIndex);
    }

    public static void pieceSelectionAlgorithm() {

        int peerId = Integer.parseInt(peer.getPeerID());
        int totalPeers = peer.getAllPeers().size();
        int range = (int) Math.ceil(Config.TOTALPIECES / totalPeers);
        int min = Math.max(0, (peerId % 1000 - 1) * range);
        int max = Math.min(min + range, Config.TOTALPIECES);

        PriorityBlockingQueue<SelectionNode> pq = new PriorityBlockingQueue<SelectionNode>(10, (a, b) -> b.priority - a.priority);


        List<SelectionNode> list = new ArrayList<>();

        for(int i = 0; i < Config.TOTALPIECES; i++) {
            if(i >= min && i <= max) {
                list.add(new SelectionNode(2, i));
            }
            else {
                list.add(new SelectionNode(1, i));
            }
        }

        Collections.shuffle(list);

        for(int i = 0; i < list.size(); i++) {
            pq.add(list.get(i));
        }

        peer.setSelectionNodes(pq);

    }

    public static boolean isDone() throws InterruptedException, IOException {
        List<PeerInfo> allPeers = peer.getAllPeers();
        int counter = 0;
        for(PeerInfo p : allPeers) {
            String bitField;
            if(p.getPeerID().compareTo(peerID) == 0) {
                bitField = String.valueOf(peer.getBitfield());
            }
            else {
                bitField = peer.getMapBitField().getOrDefault(p.getPeerID(), null);
            }
            if(bitField == null)
                continue;
            for(int i = 0; i < bitField.length(); i++) {
                if(bitField.charAt(i) == '0')
                    return false;
            }
            counter++;
        }

        if(counter == 3) {
            Thread.sleep(3000);
            closeAll();
            createFile();
            return true;
        }
        return false;
    }

    public static void createFile() throws IOException {
    	File theDir = new File("./peer_" + peerID);
    	if (!theDir.exists()){
    	    theDir.mkdirs();
    	}
        ConcurrentMap<Integer, byte[]> pieces = peer.getPieceAtIndex();
        File file = new File("./peer_" + peerID + "/" + Config.FILENAME);
        FileOutputStream stream = new FileOutputStream(file);
        for(int i = 0; i < Config.TOTALPIECES; i++) {
            stream.write(pieces.get(i));
        }
        stream.close();
    }

    public static void closeAll() throws IOException, InterruptedException {
        ConcurrentMap<String, ConnectionDetails> connections = peer.getPeerToConnections();

        for(String peer : connections.keySet()) {
            ConnectionDetails c = connections.get(peer);
            Thread cd = (Thread) c;
            Thread mh = (Thread) c.getMessageHandler();
            Thread unChoke = (Thread) c.getMessageHandler().getUnChokeThread();
            if(unChoke != null) {
                System.out.println("Stopping unChoke thread" + unChoke.getName()+" of peer " + peer);
                unChoke.stop();
            }
            Thread.sleep(1000);
            if(mh != null) {
                System.out.println("Stopping message handler thread" + mh.getName()+" of peer " + peer);
                mh.stop();
            }

            if(cd!=null) {
                System.out.println("Stopping connection thread" + cd.getName()+" of peer " + peer);
                cd.stop();
            }
        }
        Thread server = (Thread) peer.getServer();
        System.out.println("Stopping server thread" + server.getName()+" of peer " + peer);
        server.stop();
    }
}