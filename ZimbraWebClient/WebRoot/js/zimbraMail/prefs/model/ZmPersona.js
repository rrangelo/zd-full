/*
 * 
 */

/**
 * Creates a personna.
 * @class
 * This class represents a personna.
 * 
 * @param	{ZmIdentity}		identity		the identity
 * @param	{Object}		list		the list
 * @extends		ZmAccount
 */
ZmPersona = function(identity, list) {
	if (arguments.length == 0) { return; }
	
	ZmAccount.call(this, ZmAccount.TYPE_PERSONA, identity.id, null, list);

	identity.sendFromDisplay = identity.sendFromDisplay || appCtxt.get(ZmSetting.DISPLAY_NAME);
	identity.sendFromAddress = identity.sendFromAddress || appCtxt.get(ZmSetting.USERNAME);
	this.identity = identity;
};
ZmPersona.prototype = new ZmAccount;
ZmPersona.prototype.constructor = ZmPersona;

ZmPersona.prototype.toString =
function() {
	return "ZmPersona";
};


//
// Public methods
//

ZmPersona.prototype.setName =
function(name) {
	this.getIdentity().name = AjxStringUtil.htmlEncode(name);
};

ZmPersona.prototype.getName =
function() {
	return AjxStringUtil.htmlDecode(this.getIdentity().name);
};

ZmPersona.prototype.setEmail =
function(email) {
	this.getIdentity().sendFromAddress = email;
};

ZmPersona.prototype.getEmail =
function() {
	return this.getIdentity().sendFromAddress;
};

ZmPersona.prototype.getIdentity =
function() {
	return this.identity;
};

ZmPersona.prototype.create =
function(callback, errorCallback, batchCmd) {
	return this.getIdentity().create(callback, errorCallback, batchCmd);
};

ZmPersona.prototype.save =
function(callback, errorCallback, batchCmd) {
	return this.getIdentity().save(callback, errorCallback, batchCmd);
};

ZmPersona.prototype.doDelete = 
function(callback, errorCallback, batchCmd) {
	return this.getIdentity().doDelete(callback, errorCallback, batchCmd);
};
