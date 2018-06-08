/*
 * 
 */
package com.zimbra.cs.gal;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.GalSearchType;
import com.zimbra.cs.account.ZAttrProvisioning.GalMode;
import com.zimbra.cs.account.gal.GalOp;
import com.zimbra.cs.account.gal.GalUtil;
import com.zimbra.cs.account.ldap.LdapGalMapRules;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import com.zimbra.cs.account.ldap.LdapUtil;

public class GalSearchConfig {
    
	public enum GalType {
	    zimbra,
	    ldap;
        
	    public static GalType fromString(String s) throws ServiceException {
		try {
		    return GalType.valueOf(s);
		} catch (IllegalArgumentException e) {
		    throw ServiceException.INVALID_REQUEST("unknown gal type: " + s, e);
		}
	    }
	}
    
	public static GalSearchConfig create(Domain domain, GalOp op, GalType type, GalSearchType stype) throws ServiceException {
		switch (type) {
		case zimbra:
			return new ZimbraConfig(domain, op, stype);
		case ldap:
			return new LdapConfig(domain, op);
		}
		return null;
	}
	
	public static String fixupExternalGalSearchBase(String searchBase) {
	    if (searchBase == null || "ROOT".equals(searchBase)) {
	        return "";
	    } else {
	        return searchBase;
	    }
	}
	   
	public static GalSearchConfig create(DataSource ds) throws ServiceException {
		return new DataSourceConfig(ds);
	}

	private static class ZimbraConfig extends GalSearchConfig {
		public ZimbraConfig(Domain domain, GalOp op, GalSearchType stype) throws ServiceException {
			loadZimbraConfig(domain, op, stype);
		}
	}
	private static class LdapConfig extends GalSearchConfig {
		public LdapConfig(Domain domain, GalOp op) throws ServiceException {
			loadConfig(domain, op);
		}
	}
	
	private static class DataSourceConfig extends GalSearchConfig {
		public DataSourceConfig(DataSource ds) throws ServiceException {
			mGalType = GalType.fromString(ds.getAttr(Provisioning.A_zimbraGalType));
			Domain domain = Provisioning.getInstance().getDomain(ds.getAccount());
			if (mGalType == GalType.zimbra) {
				loadZimbraConfig(domain, GalOp.sync, null);
				mFilter = LdapProvisioning.getFilterDef("zimbraSync");
				if (mFilter == null)
				    mFilter = DEFAULT_FILTER;
			} else {
				loadConfig(domain, GalOp.sync);
				if (mUrl.length == 0 || mFilter == null)
					loadConfig(domain, GalOp.search);
				String[] url = ds.getMultiAttr(Provisioning.A_zimbraGalSyncLdapURL);
				if (url != null && url.length > 0)
					mUrl = url;
				mFilter = ds.getAttr(Provisioning.A_zimbraGalSyncLdapFilter, mFilter);
				mSearchBase = ds.getAttr(Provisioning.A_zimbraGalSyncLdapSearchBase, mSearchBase);
				mStartTlsEnabled = ds.getBooleanAttr(Provisioning.A_zimbraGalSyncLdapStartTlsEnabled, mStartTlsEnabled);
				mAuthMech = ds.getAttr(Provisioning.A_zimbraGalSyncLdapAuthMech, mAuthMech);
				mBindDn = ds.getAttr(Provisioning.A_zimbraGalSyncLdapBindDn, mBindDn);
				mBindPassword = ds.getAttr(Provisioning.A_zimbraGalSyncLdapBindPassword, mBindPassword);
				mKerberosPrincipal = ds.getAttr(Provisioning.A_zimbraGalSyncLdapKerberos5Principal, mKerberosPrincipal);
				mKerberosKeytab = ds.getAttr(Provisioning.A_zimbraGalSyncLdapKerberos5Keytab, mKerberosKeytab);
				mTimestampFormat = ds.getAttr(Provisioning.A_zimbraGalSyncTimestampFormat, mTimestampFormat);
				mPageSize = ds.getIntAttr(Provisioning.A_zimbraGalSyncLdapPageSize, mPageSize);
			}
			String[] attrs = ds.getMultiAttr(Provisioning.A_zimbraGalLdapAttrMap);
			String[] valueMap = ds.getMultiAttr(Provisioning.A_zimbraGalLdapValueMap);
			String groupHandlerClass = ds.getAttr(Provisioning.A_zimbraGalLdapGroupHandlerClass);
			if (attrs.length > 0 || valueMap.length > 0 || groupHandlerClass != null) {
			    if (attrs.length == 0)
			        attrs = domain.getMultiAttr(Provisioning.A_zimbraGalLdapAttrMap);
			    if (valueMap.length == 0)
			        valueMap = domain.getMultiAttr(Provisioning.A_zimbraGalLdapValueMap);
			    if (groupHandlerClass == null)
			        groupHandlerClass = domain.getAttr(Provisioning.A_zimbraGalLdapGroupHandlerClass);
				mRules = new LdapGalMapRules(attrs, valueMap, groupHandlerClass);
			}
			
			if (StringUtil.isNullOrEmpty(mFilter))
			    throw ServiceException.INVALID_REQUEST("missing GAL filter", null);
			
			mFilter = GalUtil.expandFilter(null, mFilter, "", null);
		}
		private static final String DEFAULT_FILTER = "(&(|(displayName=*)(cn=*)(sn=*)(gn=*)(mail=*)(zimbraMailDeliveryAddress=*)(zimbraMailAlias=*))(|(objectclass=zimbraAccount)(objectclass=zimbraDistributionList))(!(zimbraHideInGal=TRUE))(!(zimbraIsSystemResource=TRUE)))";
	}
	
	protected void loadZimbraConfig(Domain domain, GalOp op, GalSearchType stype) throws ServiceException {
		
        mRules = new LdapGalMapRules(domain, true);
        mOp = op;
        String filterName = null;
        
		switch (op) {
		case sync:
			filterName = 
                (stype == GalSearchType.all) ? "zimbraSync" :
			    (stype == GalSearchType.resource) ? "zimbraResourceSync" : 
			    (stype == GalSearchType.group) ? "zimbraGroupSync" : "zimbraAccountSync";
			break;
		case search:
			filterName = 
                (stype == GalSearchType.all) ? "zimbraSearch" :
			    (stype == GalSearchType.resource) ? "zimbraResources" : 
			    (stype == GalSearchType.group) ? "zimbraGroups" : "zimbraAccounts";
			mTokenizeKey = domain.getAttr(Provisioning.A_zimbraGalTokenizeSearchKey, null);
			break;
		case autocomplete:
			filterName = 
                (stype == GalSearchType.all) ? "zimbraAutoComplete" :
			    (stype == GalSearchType.resource) ? "zimbraResourceAutoComplete" : 
			    (stype == GalSearchType.group) ? "zimbraGroupAutoComplete" : "zimbraAccountAutoComplete";
			mTokenizeKey = domain.getAttr(Provisioning.A_zimbraGalTokenizeAutoCompleteKey, null);
			break;
		}
		if (filterName != null)
			mFilter = LdapProvisioning.getFilterDef(filterName);
		mAuthMech = Provisioning.LDAP_AM_SIMPLE;
		mFilter = "(&("+mFilter+")(!(zimbraHideInGal=TRUE))(!(zimbraIsSystemResource=TRUE)))";
		mSearchBase = LdapUtil.getZimbraSearchBase(domain, op);
		mGalType = GalType.zimbra;
		mTimestampFormat = GalSyncToken.LDAP_GENERALIZED_TIME_FORMAT;
		mPageSize = 1000;
	}
	
	private boolean isConfigComplete() {
		return mUrl.length > 0 && mFilter != null && 
			(mAuthMech.equals(Provisioning.LDAP_AM_NONE) ||
			 (mAuthMech.equals(Provisioning.LDAP_AM_SIMPLE) && mBindDn != null && mBindPassword != null) || 
			 (mAuthMech.equals(Provisioning.LDAP_AM_KERBEROS5) && mKerberosKeytab != null && mKerberosPrincipal != null));
	}
	protected void loadConfig(Domain domain, GalOp op) throws ServiceException {
	    
        mRules = new LdapGalMapRules(domain, false);
        mOp = op;
        
        GalMode galMode = domain.getGalMode();
        if (galMode == GalMode.zimbra)
        	loadZimbraConfig(domain, op, null);
		
        switch (op) {
        case sync:
        	mUrl = domain.getMultiAttr(Provisioning.A_zimbraGalSyncLdapURL);
            mFilter = domain.getAttr(Provisioning.A_zimbraGalLdapFilter);
            mSearchBase = domain.getAttr(Provisioning.A_zimbraGalSyncLdapSearchBase, "");
            mStartTlsEnabled = domain.getBooleanAttr(Provisioning.A_zimbraGalSyncLdapStartTlsEnabled, false);
            mAuthMech = domain.getAttr(Provisioning.A_zimbraGalSyncLdapAuthMech, Provisioning.LDAP_AM_SIMPLE);
            mBindDn = domain.getAttr(Provisioning.A_zimbraGalSyncLdapBindDn);
            mBindPassword = domain.getAttr(Provisioning.A_zimbraGalSyncLdapBindPassword);
            mKerberosPrincipal = domain.getAttr(Provisioning.A_zimbraGalSyncLdapKerberos5Principal);
            mKerberosKeytab = domain.getAttr(Provisioning.A_zimbraGalSyncLdapKerberos5Keytab);
            mTimestampFormat = domain.getAttr(Provisioning.A_zimbraGalSyncTimestampFormat, GalSyncToken.LDAP_GENERALIZED_TIME_FORMAT);
			mPageSize = domain.getIntAttr(Provisioning.A_zimbraGalSyncLdapPageSize, 1000);
            if (isConfigComplete())
            	break;
        case search:
            mUrl = domain.getMultiAttr(Provisioning.A_zimbraGalLdapURL);
            mFilter = domain.getAttr(Provisioning.A_zimbraGalLdapFilter);
            mSearchBase = domain.getAttr(Provisioning.A_zimbraGalLdapSearchBase, "");
            mStartTlsEnabled = domain.getBooleanAttr(Provisioning.A_zimbraGalLdapStartTlsEnabled, false);
            mAuthMech = domain.getAttr(Provisioning.A_zimbraGalLdapAuthMech, Provisioning.LDAP_AM_SIMPLE);
            mBindDn = domain.getAttr(Provisioning.A_zimbraGalLdapBindDn);
            mBindPassword = domain.getAttr(Provisioning.A_zimbraGalLdapBindPassword);
            mKerberosPrincipal = domain.getAttr(Provisioning.A_zimbraGalLdapKerberos5Principal);
            mKerberosKeytab = domain.getAttr(Provisioning.A_zimbraGalLdapKerberos5Keytab);
            mTokenizeKey = domain.getAttr(Provisioning.A_zimbraGalTokenizeSearchKey);
			mPageSize = domain.getIntAttr(Provisioning.A_zimbraGalLdapPageSize, 1000);
        	break;
        case autocomplete:
            mUrl = domain.getMultiAttr(Provisioning.A_zimbraGalLdapURL);
            mFilter = domain.getAttr(Provisioning.A_zimbraGalAutoCompleteLdapFilter);
            mSearchBase = domain.getAttr(Provisioning.A_zimbraGalLdapSearchBase, "");
            mStartTlsEnabled = domain.getBooleanAttr(Provisioning.A_zimbraGalLdapStartTlsEnabled, false);
            mAuthMech = domain.getAttr(Provisioning.A_zimbraGalLdapAuthMech, Provisioning.LDAP_AM_SIMPLE);
            mBindDn = domain.getAttr(Provisioning.A_zimbraGalLdapBindDn);
            mBindPassword = domain.getAttr(Provisioning.A_zimbraGalLdapBindPassword);
            mKerberosPrincipal = domain.getAttr(Provisioning.A_zimbraGalLdapKerberos5Principal);
            mKerberosKeytab = domain.getAttr(Provisioning.A_zimbraGalLdapKerberos5Keytab);
            mTokenizeKey = domain.getAttr(Provisioning.A_zimbraGalTokenizeAutoCompleteKey);
			mPageSize = domain.getIntAttr(Provisioning.A_zimbraGalLdapPageSize, 1000);
        	break;
        }
        if (mFilter != null && mFilter.indexOf("(") == -1)
        	mFilter = LdapProvisioning.getFilterDef(mFilter);
		mGalType = GalType.ldap;
	}
	
	protected GalOp mOp;
	protected GalType mGalType;
	protected String[] mUrl;
	protected boolean mStartTlsEnabled;
	protected String mFilter;
	protected String mSearchBase;
	protected String mAuthMech;
	protected String mBindDn;
	protected String mBindPassword;
	protected String mKerberosPrincipal;
	protected String mKerberosKeytab;
	protected String mTimestampFormat;
	protected String mTokenizeKey;
	protected int mPageSize;
	protected LdapGalMapRules mRules;
	
	public GalOp getOp() {
		return mOp;
	}
	public GalType getGalType() {
		return mGalType;
	}
	public String[] getUrl() {
		return mUrl;
	}
	public boolean getStartTlsEnabled() {
		return mStartTlsEnabled;
	}
	public String getFilter() {
		return mFilter;
	}
	public String getSearchBase() {
		return fixupExternalGalSearchBase(mSearchBase);
	}
	public String getAuthMech() {
		return mAuthMech;
	}
	public String getBindDn() {
		return mBindDn;
	}
	public String getBindPassword() {
		return mBindPassword;
	}
	public String getKerberosPrincipal() {
		return mKerberosPrincipal;
	}
	public String getKerberosKeytab() {
		return mKerberosKeytab;
	}
	public int getPageSize() {
		return mPageSize;
	}
	public LdapGalMapRules getRules() {
		return mRules;
	}
	public String getTokenizeKey() {
		return mTokenizeKey;
	}
	public void setGalType(GalType galType) {
		mGalType = galType;
	}
	public void setUrl(String[]  url) {
		mUrl = url;
	}
	public void setStartTlsEnabled(boolean startTlsEnabled) {
		mStartTlsEnabled = startTlsEnabled;
	}
	public void setFilter(String filter) {
		mFilter = filter;
	}
	public void setSearchBase(String searchBase) {
		mSearchBase = searchBase;
	}
	public void setAuthMech(String authMech) {
		mAuthMech = authMech;
	}
	public void setBindDn(String bindDn) {
		mBindDn = bindDn;
	}
	public void setBindPassword(String bindPassword) {
		mBindPassword = bindPassword;
	}
	public void setKerberosPrincipal(String kerberosPrincipal) {
		mKerberosPrincipal = kerberosPrincipal;
	}
	public void setKerberosKeytab(String kerberosKeytab) {
		mKerberosKeytab = kerberosKeytab;
	}
	public void setRules(LdapGalMapRules rules) {
		mRules = rules;
	}
	public void setTokenizeKey(String tokenizeKey) {
		mTokenizeKey = tokenizeKey;
	}
}