package com.P2P_FileSharing;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Peer {
    Config config;
    String peerID;
    char[] bitfield = new char[Config.TOTALPIECES];
    String hostName;
    int port;
    Server server;
    List<ConnectionDetails> connections = new ArrayList<>();
    List<PeerInfo> allPeers = new ArrayList<>();
    ConcurrentMap<String, String> mapBitField = new ConcurrentHashMap<>();
    List<PeerInfo> peersToConnect = new ArrayList<>();
    Set<String> interested = Collections.synchronizedSet(new HashSet<>());
    Set<String> unChoked = Collections.synchronizedSet(new HashSet<>());
    Set<String> choked = Collections.synchronizedSet(new HashSet<>());
    ConcurrentMap<String, Integer> downloadRate = new ConcurrentHashMap<>();
    ConcurrentMap<String, ConnectionDetails> peerToConnections = new ConcurrentHashMap<>();
    Set<String> preferredNeighbors = Collections.synchronizedSet(new HashSet<>());
    Set<String> optimisticNeighbor = Collections.synchronizedSet(new HashSet<>());
    ConcurrentMap<Integer, byte[]> pieceAtIndex = new ConcurrentHashMap<>();
    PriorityBlockingQueue<SelectionNode> selectionNodes;
    Set<Integer> requested = Collections.synchronizedSet(new HashSet<>());
    FileWriter filewriter;
    BufferedWriter bufferwriter;
    boolean hasFileInitially;
    Set<String> unChokedFromPeers = Collections.synchronizedSet(new HashSet<>());
    Set<String> chokedFromPeers = Collections.synchronizedSet(new HashSet<>());

    public Set<String> getUnChokedFromPeers() {
        return unChokedFromPeers;
    }

    public void setUnChokedFromPeers(Set<String> unChokedFromPeers) {
        this.unChokedFromPeers = unChokedFromPeers;
    }

    public Set<String> getChokedFromPeers() {
        return chokedFromPeers;
    }

    public void setChokedFromPeers(Set<String> chokedFromPeers) {
        this.chokedFromPeers = chokedFromPeers;
    }

    public boolean getHasFileInitially() {
        return hasFileInitially;
    }

    public void setHasFileInitially(boolean hasFileInitially) {
        this.hasFileInitially = hasFileInitially;
    }

    public Set<Integer> getRequested() {
        return requested;
    }

    public void setRequested(Set<Integer> requested) {
        this.requested = requested;
    }

    public PriorityBlockingQueue<SelectionNode> getSelectionNodes() {
        return selectionNodes;
    }

    public void setSelectionNodes(PriorityBlockingQueue<SelectionNode> selectionNodes) {
        this.selectionNodes = selectionNodes;
    }

    public ConcurrentMap<Integer, byte[]> getPieceAtIndex() {
        return pieceAtIndex;
    }

    public void setPieceAtIndex(ConcurrentMap<Integer, byte[]> pieceAtIndex) {
        this.pieceAtIndex = pieceAtIndex;
    }

    public Set<String> getOptimisticNeighbor() {
        return optimisticNeighbor;
    }

    public void setOptimisticNeighbor(Set<String> optimisticNeighbor) {
        this.optimisticNeighbor = optimisticNeighbor;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setConnections(List<ConnectionDetails> connections) {
        this.connections = connections;
    }

    public void setAllPeers(List<PeerInfo> allPeers) {
        this.allPeers = allPeers;
    }

    public void setMapBitField(ConcurrentMap<String, String> mapBitField) {
        this.mapBitField = mapBitField;
    }

    public void setPeersToConnect(List<PeerInfo> peersToConnect) {
        this.peersToConnect = peersToConnect;
    }

    public void setUnChoked(Set<String> unChoked) {
        this.unChoked = unChoked;
    }

    public void setChoked(Set<String> choked) {
        this.choked = choked;
    }

    public void setPeerToConnections(ConcurrentMap<String, ConnectionDetails> peerToConnections) {
        this.peerToConnections = peerToConnections;
    }

    public void setDownloadRate(ConcurrentMap<String, Integer> downloadRate) {
        this.downloadRate = downloadRate;
    }

    public Set<String> getPreferredNeighbors() {
        return preferredNeighbors;
    }

    public void setPreferredNeighbors(Set<String> preferredNeighbors) {
        this.preferredNeighbors = preferredNeighbors;
    }

    public void setInterested(Set<String> interested) {
        this.interested = interested;
    }

    public ConcurrentMap<String, ConnectionDetails> getPeerToConnections() {
        return peerToConnections;
    }

    public ConcurrentMap<String, Integer> getDownloadRate() {
        return downloadRate;
    }

    public Set<String> getUnChoked() {
        return unChoked;
    }

    public Set<String> getChoked() {
        return choked;
    }

    volatile Queue<MessageQueueElement> messageQueue = new LinkedList<>();
    boolean isSelectingNeighbors;
    MessageHandler messageHandler;
    Map<String, String> handShakeStatus = new HashMap<>();
    Map<String, String> bitFieldStatus = new HashMap<>();

    public Map<String, String> getBitFieldStatus() {
        return bitFieldStatus;
    }

    public void setBitFieldStatus(Map<String, String> bitFieldStatus) {
        this.bitFieldStatus = bitFieldStatus;
    }

    public Map<String, String> getHandShakeStatus() {
        return handShakeStatus;
    }

    public void setHandShakeStatus(Map<String, String> handShakeStatus) {
        this.handShakeStatus = handShakeStatus;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public boolean isSelectingNeighbors() {
        return isSelectingNeighbors;
    }

    public void setSelectingNeighbors(boolean selectingNeighbors) {
        isSelectingNeighbors = selectingNeighbors;
    }

    public Queue<MessageQueueElement> getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(Queue<MessageQueueElement> messageQueue) {
        this.messageQueue = messageQueue;
    }

    public List<ConnectionDetails> getConnections() {
        return connections;
    }

    public Set<String> getInterested() {
        return interested;
    }

    public List<PeerInfo> getPeersToConnect() {
        return peersToConnect;
    }

    public Config getConfig() {
        return config;
    }

    public Peer(String peerID, Config config) throws IOException {
        this.peerID = peerID;
        this.config = config;
        this.filewriter = new FileWriter("log_peer_[" + peerID +"].log");
        this.bufferwriter = new BufferedWriter(filewriter);
    }

    public FileWriter getFilewriter() {
        return filewriter;
    }

    public void setFilewriter(FileWriter filewriter) {
        this.filewriter = filewriter;
    }

    public BufferedWriter getBw() {
        return bufferwriter;
    }

    public void setBw(BufferedWriter bufferwriter) {
        this.bufferwriter = bufferwriter;
    }

    public void writeToLog(String message) throws IOException {
        synchronized (filewriter) {
            this.bufferwriter.write(message);
            this.bufferwriter.newLine();
        }
    }

    public ConcurrentMap<String, String> getMapBitField() {
        return mapBitField;
    }

    public List<PeerInfo> getAllPeers() {
        return allPeers;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    public char[] getBitfield() {
        return bitfield;
    }

    synchronized public void setBitfield(char[] bitfield) {
        this.bitfield = bitfield;
    }

    public Peer() {
    }

    boolean hasPieces() {
        char[] bit = this.getBitfield();
        for(int j = 0; j < bit.length; j++){
            if(bit[j] == '1')
                return true;
        }
        return false;
    }

    public void readPeerInfo() throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("PeerInfo.cfg"));
        String line = null;
        try {
            boolean addPeersToConnect = true;
            while((line = bufferedReader.readLine()) != null) {
                String[] splits = line.split(" ");
                PeerInfo peerInfo = new PeerInfo(splits[0], splits[1], Integer.parseInt(splits[2]), Integer.parseInt(splits[3]));
                if(peerInfo.getPeerID().compareTo(this.peerID) == 0) {
                    setPeerProperties(peerInfo);
                    addPeersToConnect = false;
                }
                if(addPeersToConnect) {
                    peersToConnect.add(peerInfo);
                }
                allPeers.add(peerInfo);
                choked.add(peerInfo.getPeerID());
            }
        }catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        }catch (IOException ex2) {
            ex2.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPeerProperties(PeerInfo peerInfo) {
        char[] bitfield = new char[Config.TOTALPIECES];
        if(!(peerInfo.getHasFile() == 1)) {
            Arrays.fill(bitfield, '0');
        }
        else {
            Arrays.fill(bitfield, '1');
        }
        setHasFileInitially(peerInfo.getHasFile() == 1);
        setBitfield(bitfield);
        setPort(peerInfo.getPort_no());
        setHostName(peerInfo.getHost_Name());
    }
}
