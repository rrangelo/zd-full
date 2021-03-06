/*
 * 
 */
package com.zimbra.cs.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.imap.ImapConfig;
import com.zimbra.cs.imap.ImapServer;
import com.zimbra.cs.imap.TcpImapServer;
import com.zimbra.cs.lmtpserver.LmtpConfig;
import com.zimbra.cs.lmtpserver.LmtpServer;
import com.zimbra.cs.lmtpserver.TcpLmtpServer;
import com.zimbra.cs.milter.MilterConfig;
import com.zimbra.cs.milter.MilterServer;
import com.zimbra.cs.milter.MinaMilterServer;
import com.zimbra.cs.nio.NioThreadFactory;
import com.zimbra.cs.pop3.Pop3Config;
import com.zimbra.cs.pop3.Pop3Server;
import com.zimbra.cs.pop3.TcpPop3Server;
import com.zimbra.cs.util.ZimbraApplication;

public final class ServerManager {
    private LmtpServer lmtpServer;
    private Pop3Server pop3Server;
    private Pop3Server pop3SSLServer;
    private ImapServer imapServer;
    private ImapServer imapSSLServer;
    private ExecutorService milterNioHandlerPool;
    private MilterServer milterServer;

    private static final ServerManager INSTANCE = new ServerManager();

    public static ServerManager getInstance() {
        return INSTANCE;
    }

    public void startServers() throws ServiceException {
        ZimbraApplication app = ZimbraApplication.getInstance();
        if (app.supports(LmtpServer.class)) {
            startLmtpServer();
        }
        if (app.supports(Pop3Server.class)) {
            if (isEnabled(Provisioning.A_zimbraPop3ServerEnabled)) {
                pop3Server = startPop3Server(false);
            }
            if (isEnabled(Provisioning.A_zimbraPop3SSLServerEnabled)) {
                pop3SSLServer = startPop3Server(true);
            }
        }
        if (app.supports(ImapServer.class)) {
            if (isEnabled(Provisioning.A_zimbraImapServerEnabled)) {
                imapServer = startImapServer(false);
            }
            if (isEnabled(Provisioning.A_zimbraImapSSLServerEnabled)) {
                imapSSLServer = startImapServer(true);
            }
        }

        // run milter service in the same process as mailtoxd. should be used only in dev environment
        if (app.supports(MilterServer.class)) {
            if (LC.milter_in_process_mode.booleanValue()) {
                milterServer = startMilterServer();
            }
        }
    }

    private static boolean isEnabled(String key) throws ServiceException {
        return Provisioning.getInstance().getLocalServer().getBooleanAttr(key, false);
    }

    private LmtpServer startLmtpServer() throws ServiceException {
        LmtpConfig config = LmtpConfig.getInstance();
        LmtpServer server = new TcpLmtpServer(config);
        server.start();
        return server;
    }

    private Pop3Server startPop3Server(boolean ssl) throws ServiceException {
        Pop3Server server = new TcpPop3Server(new Pop3Config(ssl));
        server.start();
        return server;
    }

    private ImapServer startImapServer(boolean ssl) throws ServiceException {
        ImapServer server = new TcpImapServer(new ImapConfig(ssl));
        server.start();
        return server;
    }

    private MilterServer startMilterServer() throws ServiceException {
        MilterConfig config = new MilterConfig();
        if (milterNioHandlerPool == null) {
            milterNioHandlerPool = newNioHandlerPool(config);
        }
        MilterServer server = new MinaMilterServer(config, milterNioHandlerPool);
        server.start();
        return server;
    }

    public void stopServers() throws ServiceException {
        if (lmtpServer != null) {
            lmtpServer.stop();
        }
        if (pop3Server != null) {
            pop3Server.stop();
        }
        if (pop3SSLServer != null) {
            pop3SSLServer.stop();
        }
        if (imapServer != null) {
            imapServer.stop();
        }
        if (imapSSLServer != null) {
            imapSSLServer.stop();
        }
        if (milterServer != null) {
            milterServer.stop();
        }
    }

    public LmtpServer getLmtpServer() {
        return lmtpServer;
    }

    public static ExecutorService newNioHandlerPool(ServerConfig config) {
        return new ThreadPoolExecutor(
            Math.min(config.getNumThreads(), config.getNioMinThreads()),
            config.getNumThreads(),
            config.getNioMinThreads(),
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new NioThreadFactory(config.getProtocol() + "Handler"));
    }
}
