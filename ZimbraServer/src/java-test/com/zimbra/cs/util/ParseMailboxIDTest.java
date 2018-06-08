/*
 * 
 */
package com.zimbra.cs.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.util.ParseMailboxID;

/**
 * Unit test for {@link ParseMailboxID}.
 *
 * @author ysasaki
 */
public class ParseMailboxIDTest {

    @BeforeClass
    public static void init() throws Exception {
        MockProvisioning prov = new MockProvisioning();
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraId, "0-0-0");
        attrs.put(Provisioning.A_zimbraMailHost, "localhost");
        prov.createAccount("test@zimbra.com", "secret", attrs);
        Provisioning.setInstance(prov);
    }

    @Test
    public void parseLocalMailboxId() throws Exception {
        ParseMailboxID id = ParseMailboxID.parse("1");
        Assert.assertTrue(id.isLocal());
        Assert.assertNull(id.getServer());
        Assert.assertEquals(1, id.getMailboxId());
        Assert.assertFalse(id.isAllMailboxIds());
        Assert.assertFalse(id.isAllServers());
        Assert.assertNull(id.getAccount());
    }

    @Test
    public void parseEmail() throws Exception {
        ParseMailboxID id = ParseMailboxID.parse("test@zimbra.com");
        Assert.assertTrue(id.isLocal());
        Assert.assertEquals("localhost", id.getServer());
        Assert.assertEquals(0, id.getMailboxId());
        Assert.assertFalse(id.isAllMailboxIds());
        Assert.assertFalse(id.isAllServers());
        Assert.assertEquals(Provisioning.getInstance().getAccountByName("test@zimbra.com"), id.getAccount());
    }

    @Test
    public void parseAccountId() throws Exception {
        ParseMailboxID id = ParseMailboxID.parse("0-0-0");
        Assert.assertTrue(id.isLocal());
        Assert.assertEquals("localhost", id.getServer());
        Assert.assertEquals(0, id.getMailboxId());
        Assert.assertFalse(id.isAllMailboxIds());
        Assert.assertFalse(id.isAllServers());
        Assert.assertEquals(Provisioning.getInstance().getAccountByName("test@zimbra.com"), id.getAccount());
    }

    @Test
    public void parseMailboxId() throws Exception {
        ParseMailboxID id = ParseMailboxID.parse("/localhost/1");
        Assert.assertTrue(id.isLocal());
        Assert.assertEquals("localhost", id.getServer());
        Assert.assertEquals(1, id.getMailboxId());
        Assert.assertFalse(id.isAllMailboxIds());
        Assert.assertFalse(id.isAllServers());
        Assert.assertNull(id.getAccount());

        try {
            ParseMailboxID.parse("localhost*/3");
            Assert.fail();
        } catch (ServiceException expected) {
        }
    }

    @Test
    public void parseAllServers() throws Exception {
        ParseMailboxID id = ParseMailboxID.parse("*");
        Assert.assertFalse(id.isLocal());
        Assert.assertEquals("*", id.getServer());
        Assert.assertEquals(0, id.getMailboxId());
        Assert.assertTrue(id.isAllMailboxIds());
        Assert.assertTrue(id.isAllServers());
        Assert.assertNull(id.getAccount());
    }

    @Test
    public void parseAllMailboxes() throws Exception {
        ParseMailboxID id = ParseMailboxID.parse("/localhost/*");
        Assert.assertTrue(id.isLocal());
        Assert.assertEquals("localhost", id.getServer());
        Assert.assertEquals(0, id.getMailboxId());
        Assert.assertTrue(id.isAllMailboxIds());
        Assert.assertFalse(id.isAllServers());
        Assert.assertNull(id.getAccount());
    }

}
