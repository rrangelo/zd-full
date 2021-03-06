/*
 * 
 */
package com.zimbra.cs.session;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxManager.FetchMode;
import com.zimbra.cs.service.mail.WaitSetRequest;

/**
 * SomeAccountsWaitSet: an implementation of IWaitSet that works by listening over one or more Accounts
 * 
 * External APIs:
 *     WaitSet.doWait()              // primary wait API
 *     WaitSet.getDefaultInterest()  // accessor
 */
public final class SomeAccountsWaitSet extends WaitSetBase implements MailboxManager.Listener {

    /** Constructor */
    SomeAccountsWaitSet(String ownerAccountId, String id, int defaultInterest) {
        super(ownerAccountId, id, defaultInterest);
        mCurrentSeqNo = 1;
    }
    
    public List<WaitSetError> removeAccounts(List<String> accts) {
        List<WaitSetError> errors = new ArrayList<WaitSetError>();
        
        for (String id : accts) {
            WaitSetSession session = null;
            synchronized(this) {
                WaitSetAccount wsa = mSessions.get(id);
                if (wsa != null) {
                    session = wsa.getSession();
                    mSessions.remove(id);
                } else {
                    errors.add(new WaitSetError(id, WaitSetError.Type.NOT_IN_SET_DURING_REMOVE));
                }
            }
            if (session != null) {
                assert(!Thread.holdsLock(this));
                session.doCleanup();
            }
        }
        return errors;
    }
    
    /* @see com.zimbra.cs.session.IWaitSet#doWait(com.zimbra.cs.session.WaitSetCallback, java.lang.String, boolean, java.util.List, java.util.List, java.util.List) */
    public synchronized List<WaitSetError> doWait(WaitSetCallback cb, String lastKnownSeqNo,    
        List<WaitSetAccount> addAccounts, List<WaitSetAccount> updateAccounts) throws ServiceException {
        
        cancelExistingCB();
        
        List<WaitSetError> errors = new LinkedList<WaitSetError>();
        
        if (addAccounts != null) {
            errors.addAll(addAccounts(addAccounts));
        }
        if (updateAccounts != null) {
            errors.addAll(updateAccounts(updateAccounts));
        }
        // figure out if there is already data here
        mCb = cb;
        mCbSeqNo = Long.parseLong(lastKnownSeqNo);
        trySendData();
        
        return errors;
    }

    /* @see com.zimbra.cs.mailbox.MailboxManager.Listener#mailboxAvailable(com.zimbra.cs.mailbox.Mailbox) */
    public void mailboxAvailable(Mailbox mbox) {
        this.mailboxLoaded(mbox);
    }
    
    /* @see com.zimbra.cs.mailbox.MailboxManager.Listener#mailboxCreated(com.zimbra.cs.mailbox.Mailbox) */
    public void mailboxCreated(Mailbox mbox) {
        this.mailboxLoaded(mbox);
    }
    
    /* @see com.zimbra.cs.mailbox.MailboxManager.Listener#mailboxLoaded(com.zimbra.cs.mailbox.Mailbox) */
    public synchronized void mailboxLoaded(Mailbox mbox) {
        WaitSetAccount wsa = mSessions.get(mbox.getAccountId());
        if (wsa != null) {
            // create a new session... 
            WaitSetError error = initializeWaitSetSession(wsa, mbox);
            if (error != null) {
                mSessions.remove(wsa.getAccountId());
                signalError(error);
            }
        } 
    }
    
    /* @see com.zimbra.cs.mailbox.MailboxManager.Listener#mailboxDeleted(java.lang.String) */
    public synchronized void mailboxDeleted(String accountId) {
        WaitSetAccount wsa = mSessions.get(accountId);
        if (wsa != null) {
            mSessions.remove(accountId);
            signalError(new WaitSetError(accountId, WaitSetError.Type.MAILBOX_DELETED));
        }
    }
    
    private synchronized WaitSetError initializeWaitSetSession(WaitSetAccount wsa, Mailbox mbox) {
        // 
        // check to see if the session is already initialized and valid, if not, re-initialize it.
        //
        WaitSetSession session = wsa.getSession();
        if (session != null && session.getMailbox() != mbox) {
            ZimbraLog.session.warn("SESSION BEING LEAKED? WaitSetSession points to old version of mailbox...possibly leaking this session:", session);
            wsa.cleanupSession();
            session = null; // re-initialize it below
        }
        
        if (session == null) {
            return wsa.createSession(mbox, this);
        } else {
            return null;
        }
        
//            // The session is not already initialized....therefore it's OK to lock in the reverse order 
//            // (waitset then mailbox) because we know the session isn't added as a listener and therefore 
//            // we won't get an upcall from the Mailbox
//            //
//            // See bug 31666 for more info
//            //
//            WaitSetSession wsSession = new WaitSetSession(this, wsa.accountId, wsa.interests, wsa.lastKnownSyncToken);
//            try {
//                synchronized(mbox) { // this is OK, see above comment
//                    wsSession.register();
//                    wsa.setRef(wsSession);
////                    wsa.ref = new SoftReference<WaitSetSession>(wsSession);
//                    // must force update here so that initial sync token is checked against current mbox state
//                    wsSession.update(wsa.interests, wsa.lastKnownSyncToken);
//                }
//            } catch (MailServiceException e) {
//                if (e.getCode().equals(MailServiceException.MAINTENANCE)) {
//                    //wsa.ref = null; // will get re-set when mailboxAvailable() is called
//                    wsa.setRef(null);
//                    ZimbraLog.session.debug("Maintenance mode trying to initialize WaitSetSession for accountId "+wsa.accountId); 
//                } else {
//                    ZimbraLog.session.warn("Error initializing WaitSetSession for accountId "+wsa.accountId+" -- MailServiceException", e); 
//                    return new WaitSetError(wsa.accountId, WaitSetError.Type.ERROR_LOADING_MAILBOX);
//                }
//            } catch (ServiceException e) {
//                ZimbraLog.session.warn("Error initializing WaitSetSession for accountId "+wsa.accountId+" -- ServiceException", e); 
//                return new WaitSetError(wsa.accountId, WaitSetError.Type.ERROR_LOADING_MAILBOX);
//            }
//        }
//        return null;
    }
    
    protected boolean cbSeqIsCurrent() {
        return (mCbSeqNo == mCurrentSeqNo);
    }
    
    protected String toNextSeqNo() {
        mCurrentSeqNo++;
        return Long.toString(mCurrentSeqNo);
    }
    
    private synchronized List<WaitSetError> updateAccounts(List<WaitSetAccount> updates) {
        List<WaitSetError> errors = new ArrayList<WaitSetError>();
        
        for (WaitSetAccount update : updates) {
            WaitSetAccount existing = mSessions.get(update.getAccountId());
            if (existing != null) {
                existing.setInterests(update.getInterests());
                existing.setLastKnownSyncToken(update.getLastKnownSyncToken());
                WaitSetSession session = existing.getSession();
                if (session != null) {
                    session.update(existing.getInterests(), existing.getLastKnownSyncToken());
                    // update it!
                }
            } else {
                errors.add(new WaitSetError(update.getAccountId(), WaitSetError.Type.NOT_IN_SET_DURING_UPDATE));
            }
        }
        return errors;
    }
    
    synchronized List<WaitSetError> addAccounts(List<WaitSetAccount> wsas) throws ServiceException {
        List<WaitSetError> errors = new ArrayList<WaitSetError>();

        for (WaitSetAccount wsa : wsas) {
            if (!mSessions.containsKey(wsa.getAccountId())) {
                // add the account to our session list
                mSessions.put(wsa.getAccountId(), wsa);
                
                // create the Session, if necessary, to listen to the requested mailbox
                try {
                    // if there is a sync token, then we need to check to see if the 
                    // token is up-to-date...which means we have to fetch the mailbox.  Otherwise,
                    // we don't have to fetch the mailbox.
                    MailboxManager.FetchMode fetchMode = MailboxManager.FetchMode.AUTOCREATE;
                    if (wsa.getLastKnownSyncToken() == null)
                        fetchMode = MailboxManager.FetchMode.ONLY_IF_CACHED;
                    
                    //
                    // THIS CALL MIGHT REGISTER THE SESSION (via the MailboxManager notification --> mailboxLoaded() callback!
                    //
                    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(wsa.getAccountId(), fetchMode);
                    if (mbox != null) {
                        WaitSetError error = initializeWaitSetSession(wsa, mbox);
                        if (error != null) { 
                            errors.add(error);
                        }
                    }
                } catch (ServiceException e) {
                    if (e.getCode() == AccountServiceException.NO_SUCH_ACCOUNT) {
                        errors.add(new WaitSetError(wsa.getAccountId(), WaitSetError.Type.NO_SUCH_ACCOUNT));
                    } else if (e.getCode() == ServiceException.WRONG_HOST) {
                        errors.add(new WaitSetError(wsa.getAccountId(), WaitSetError.Type.WRONG_HOST_FOR_ACCOUNT));
                    } else {
                        errors.add(new WaitSetError(wsa.getAccountId(), WaitSetError.Type.ERROR_LOADING_MAILBOX));
                    }
                    mSessions.remove(wsa);
                }                
                
            } else {
                errors.add(new WaitSetError(wsa.getAccountId(), WaitSetError.Type.ALREADY_IN_SET_DURING_ADD));
            }
        }
        return errors;
    }
    
    synchronized void cleanupSession(WaitSetSession session) {
        WaitSetAccount acct = mSessions.get(session.getAuthenticatedAccountId());
        if (acct != null) {
            acct.cleanupSession();
        }
    }
    
    @Override
    int countSessions() {
        return mSessions.size();
    }
    
    /**
     * Cleanup and remove all the sessions referenced by this WaitSet
     */
    @Override
    synchronized HashMap<String, WaitSetAccount> destroy() {
        try {
            MailboxManager.getInstance().removeListener(this);
        } catch (ServiceException e) {
            ZimbraLog.session.warn("Caught unexpected ServiceException while destroying WaitSet: "+e, e);
        }
        cancelExistingCB();
        HashMap<String, WaitSetAccount> toRet = mSessions;
        mSessions = new HashMap<String, WaitSetAccount>();
        mCurrentSignalledSessions.clear();
        mSentSignalledSessions.clear();
        mCurrentSeqNo = Long.MAX_VALUE;
        return toRet;
   }
    
    @Override
    WaitSetCallback getCb() { 
        return mCb;
    }
    
    /**
     * Called by the WaitSetSession to revoke a signal...this happens when we get a 
     * sync token <update> with a higher sync token for this account than the account's
     * current highest
     * 
     * @param session
     */
    synchronized void unsignalDataReady(WaitSetSession session) {
        if (mSessions.containsKey(session.getAuthenticatedAccountId())) { // ...false if waitset is shutting down...
            mCurrentSignalledSessions.remove(session.getAuthenticatedAccountId());
        }
    }
    
    /**
     * Called by the WaitSetSession when there is data to be signalled by this session
     * 
     * @param session
     */
    synchronized void signalDataReady(WaitSetSession session) {
        boolean trace = ZimbraLog.session.isTraceEnabled();
        if (trace) ZimbraLog.session.trace("SomeAccountsWaitSet.signalDataReady 1");
        if (mSessions.containsKey(session.getAuthenticatedAccountId())) { // ...false if waitset is shutting down...
            if (trace) ZimbraLog.session.trace("SomeAccountsWaitSet.signalDataReady 2");
            if (mCurrentSignalledSessions.add(session.getAuthenticatedAccountId())) {
                if (trace) ZimbraLog.session.trace("SomeAccountsWaitSet.signalDataReady 3");
                trySendData();
            }
        }
        if (trace) ZimbraLog.session.trace("SomeAccountsWaitSet.signalDataReady done");
    }
    
    public synchronized void handleQuery(Element response) {
        super.handleQuery(response);
        
        response.addAttribute("cbSeqNo", mCbSeqNo);
        response.addAttribute("currentSeqNo", mCurrentSeqNo);

        for (Map.Entry<String, WaitSetAccount> entry : mSessions.entrySet()) {
            Element sessionElt = response.addElement("session");
            WaitSetAccount wsa = entry.getValue();
            
            assert(wsa.getAccountId().equals(entry.getKey()));
            if (!wsa.getAccountId().equals(entry.getKey())) {
                sessionElt.addAttribute("acctIdError", wsa.getAccountId());
            }
            
            sessionElt.addAttribute(MailConstants.A_ACCOUNT, entry.getKey());
            sessionElt.addAttribute(MailConstants.A_TYPES, 
                                    WaitSetRequest.expandInterestStr(wsa.getInterests()));
            if (wsa.getLastKnownSyncToken() != null) {
                sessionElt.addAttribute(MailConstants.A_TOKEN, wsa.getLastKnownSyncToken().toString());
            }
            
            if (wsa.getLastKnownSyncToken() != null) {
                try {
                    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(wsa.getAccountId(), FetchMode.ONLY_IF_CACHED);
                    if (mbox != null) {
                        int mboxLastChange = mbox.getLastChangeID();
                        sessionElt.addAttribute("mboxSyncToken", mboxLastChange);
                        sessionElt.addAttribute("mboxSyncTokenDiff", mboxLastChange-wsa.getLastKnownSyncToken().getChangeId());
                    }
                } catch (Exception e) {
                    ZimbraLog.session.warn("Caught exception from MailboxManager in SomeAccountsWaitSet.handleQuery() for accountId"+
                                           wsa.getAccountId(), e);
                }
            }
            
            WaitSetSession wss = wsa.getSession();
            if (wss != null) {
                Element wssElt = sessionElt.addElement("WaitSetSession");
                wssElt.addAttribute("interestMask", wss.mInterestMask);
                wssElt.addAttribute("highestChangeId", wss.mHighestChangeId);
                wssElt.addAttribute("lastAccessTime", wss.getLastAccessTime());
                wssElt.addAttribute("creationTime", wss.getCreationTime());
                wssElt.addAttribute("sessionId", wss.getSessionId());
                
                if (wss.mSyncToken != null) {
                    wssElt.addAttribute(MailConstants.A_TOKEN, wss.mSyncToken.toString());
                }
            }
        }
    }
    
    private long mCbSeqNo = 0; // seqno passed in by the current waiting callback
    private long mCurrentSeqNo; // current sequence number 
    
    /** these are the accounts we are listening to.  Stores EITHER a WaitSetSession or an AccountID  */
    private HashMap<String, WaitSetAccount> mSessions = new HashMap<String, WaitSetAccount>();
}
