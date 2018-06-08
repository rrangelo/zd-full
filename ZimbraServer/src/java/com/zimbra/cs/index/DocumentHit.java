/*
 * 
 */
package com.zimbra.cs.index;

import org.apache.lucene.document.Document;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;

public final class DocumentHit extends ZimbraHit {

    private final int itemId;
    private final Document luceneDoc;
    private com.zimbra.cs.mailbox.Document docItem;

    DocumentHit(ZimbraQueryResultsImpl results, Mailbox mbx, int itemId, Document luceneDoc,
            com.zimbra.cs.mailbox.Document docItem) {
        super(results, mbx);
        this.itemId = itemId;
        this.luceneDoc = luceneDoc;
        this.docItem = docItem;
    }

    @Override
    public long getDate() throws ServiceException {
        return getDocument().getDate();
    }

    @Override
    public long getSize() throws ServiceException {
        return getDocument().getSize();
    }

    @Override
    public int getConversationId() {
        return 0;
    }

    @Override
    public int getItemId() {
        return itemId;
    }

    public byte getItemType() throws ServiceException {
        return getDocument().getType();
    }

    @Override
    void setItem(MailItem item) {
        if (item instanceof com.zimbra.cs.mailbox.Document) {
            docItem = (com.zimbra.cs.mailbox.Document) item;
        }
    }

    @Override
    boolean itemIsLoaded() {
        return docItem != null;
    }

    @Override
    public String getSubject() throws ServiceException {
        return getDocument().getName();
    }

    @Override
    public String getName() throws ServiceException {
        return getDocument().getName();
    }

    @Override
    public MailItem getMailItem() throws ServiceException {
        return getDocument();
    }

    public com.zimbra.cs.mailbox.Document getDocument() throws ServiceException {
        if (docItem == null) {
            docItem = getMailbox().getDocumentById(null, itemId);
        }
        return docItem;
    }

    public int getVersion() throws ServiceException {
        if (luceneDoc != null) {
            String ver = luceneDoc.get(LuceneFields.L_VERSION);
            if (ver != null) {
                return Integer.parseInt(ver);
            }
        }
        // if there is no lucene Document, only the db search was done, then just match the latest version.
        return getDocument().getVersion();
    }
}
