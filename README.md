#  Peer-To-Peer
## Computer Networks
### Nisha Kumari    (nisha2k21gce@gmail.com)


Brief Process Explanation
1. The user types in the peer-id
2. From "Common.cfg," each peer-id reads the configuration data.
3. The peer-id then reads its own host and port information as well as the peer's
information from "PeerInfo.cfg."
4. After being read, the peer begins the socket thread and launches itself on the specified
host and port.
5. In addition, the current peer launches a read-thread, whose job it is to carry out
protocol-related tasks such issuing handshake requests, choking and unchoking peers,
interested and not-interested messages, and requesting and downloading bits from peers.
6. In addition, there are threads that stifle the peers in their peer neighbors by briefly
halting their execution. This interruption is made in favor of its closest, most favoured
neighbors.
Please view the peer's running on the CISE machine's video link.
