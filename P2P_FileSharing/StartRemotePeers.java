package com.P2P_FileSharing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



public class StartRemotePeers {
	private static final String script_path = "cd /cise/homes/nikhilkulkarni/CNProject/src/com/company;java -cp /cise/homes/prajan/CNProject/src com.company.peerProcess ";
	
    public static class PeerInfo {

        private String id_peer;
        private String host_name;

        public PeerInfo(String id_peer, String host_name) {
            super();
            this.id_peer = id_peer;
            this.host_name = host_name;
        }

        public String getId_peer() {
            return id_peer;
        }

        public void setId_peer(String id_peer) {
            this.id_peer = id_peer;
        }

        public String getHostName() {
            return host_name;
        }

        public void setHostName(String host_name) {
            this.host_name = host_name;
        }

    }

    public static void main(String[] args) {

        ArrayList<PeerInfo> peers = new ArrayList<>();

        String user = "n.kulkarni";

        peers.add(new PeerInfo("1001", "lin114-00.cise.ufl.edu"));
        peers.add(new PeerInfo("1002", "lin114-01.cise.ufl.edu"));
        peers.add(new PeerInfo("1003", "lin114-02.cise.ufl.edu"));
        peers.add(new PeerInfo("1004", "lin114-03.cise.ufl.edu"));
        peers.add(new PeerInfo("1005", "lin114-04.cise.ufl.edu"));
        peers.add(new PeerInfo("1006", "lin114-05.cise.ufl.edu"));
		peers.add(new PeerInfo("1007", "lin114-06.cise.ufl.edu"));
        peers.add(new PeerInfo("1008", "lin114-07.cise.ufl.edu"));
        peers.add(new PeerInfo("1009", "lin114-08.cise.ufl.edu"));

        for (int i=0; i< peers.size(); i++) {
            try {
                JSch jsch1 = new JSch();
                /*
                 * Give the path to your private key. Make sure your public key
                 * is already within your remote CISE machine to ssh into it
                 * without a password. Or you can use the corressponding method
                 * of JSch which accepts a password.
                 */
                jsch1.addIdentity("C:\\Users\\nikhilkulkarni\\.ssh\\private", "");
                Session session = jsch1.getSession(user, peers.get(i).getHostName(), 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();

                System.out.println("Session to peer# " + peers.get(i).getId_peer() + " at " + peers.get(i).getHostName());

                Channel pathway = session.openChannel("exec");
                System.out.println("remotePeerID"+peers.get(i).getId_peer());
                ((ChannelExec) pathway).setCommand(script_path + peers.get(i).getId_peer());

                pathway.setInputStream(null);
                ((ChannelExec) pathway).setErrStream(System.err);

                InputStream input = pathway.getInputStream();
                pathway.connect();

                System.out.println("Channel Connected to peer# " + peers.get(i).getId_peer() + " at "
                        + peers.get(i).getHostName() + " server with commands");

                int finalI = i;
                (new Thread() {
                    @Override
                    public void run() {

                        InputStreamReader input_reader = new InputStreamReader(input);
                        BufferedReader buffer_reader = new BufferedReader(input_reader);
                        String line = null;

                        try {

                            while ((line = buffer_reader.readLine()) != null) {
                                System.out.println(" " + peers.get(finalI).getId_peer() + ">:" + line);
                            }
                            buffer_reader.close();
                            input_reader.close();
                        } catch (Exception ex) {
                            System.out.println(peers.get(finalI).getId_peer() + " Exception >:");
                            ex.printStackTrace();
                        }

                        pathway.disconnect();
                        session.disconnect();
                    }
                }).start();

            } catch (JSchException e) {
// TODO Auto-generated catch block
                System.out.println(peers.get(i).getId_peer() + " JSchException >:");
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println(peers.get(i).getId_peer() + " Exception >:");
                ex.printStackTrace();
            }

        }
    }

}
