package com.github.macpersia.planty_jira_view;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A workaround for https://forums.openshift.com/commons-httpclient-permission-denied.
 */
public class WorkaroundSocketFactory implements ProtocolSocketFactory {

    enum Protocol { HTTP, HTTPS }

    final Protocol protocol;

    private static final Log LOG = LogFactory.getLog(WorkaroundSocketFactory.class);

    public WorkaroundSocketFactory(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public Socket createSocket(String host, int port,
                               InetAddress localAddress, int localPort)
            throws IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("createSocket called. host = " + host + ", port = " + port
                    + ", ignoring localAddress = " + ((localAddress != null) ? localAddress.toString() : "null")
                    + ", ignoring localPort = " + localPort);
        }
        try {
            LOG.debug("Socket created");
            SocketFactory factory = (protocol == Protocol.HTTPS) ?
                    SSLSocketFactory.getDefault()
                    : SocketFactory.getDefault();
            return factory.createSocket(host, port);

        } catch (IOException e) {
            LOG.error("Error creating socket: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress,
                               int localPort, HttpConnectionParams params)
            throws IOException {

        LOG.debug("createSocket called with HttpConnectionParams -- ignoring the timeout value and proceeding");
        return this.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {

        LOG.debug("createSocket called with just host and port. proceeding..");
        return this.createSocket(host, port, null, 0);
    }

    @Override
    public boolean equals(Object obj) {
        return !(obj instanceof WorkaroundSocketFactory) ? false
                : ((WorkaroundSocketFactory) obj).protocol == protocol;
    }

    @Override
    public int hashCode() {
        return protocol.hashCode();
    }
}