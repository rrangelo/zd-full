/*
 * 
 */

/**
 * Creates a voice item.
 * @constructor
 * @class ZmVoiceItem
 * This abstract class represents a voicemail or phone call.
 *
 * @param id		[int]			unique ID
 * @param list		[ZmVoiceList]	list that contains this item
 */
ZmVoiceItem = function(type, id, list) {

	if (arguments.length == 0) { return; }
	ZmItem.call(this, type, id, list);

	this.id = null;
	this.date = 0;
	this.duration = 0;
	this._callingParties = {};
	this.participants = new AjxVector();
};

ZmVoiceItem.prototype = new ZmItem;
ZmVoiceItem.prototype.constructor = ZmVoiceItem;

ZmVoiceItem.prototype.toString = 
function() {
	return "ZmVoiceItem";
};

ZmVoiceItem.FROM		= 1;
ZmVoiceItem.TO			= 2;

ZmVoiceItem.prototype.getFolder = 
function() {
	return this.list ? this.list.folder : null;
};

ZmVoiceItem.prototype.getPhone = 
function() {
	return this.list && this.list.folder ? this.list.folder.phone : null;
};

ZmVoiceItem.prototype.isInTrash = 
function() {
	if (this.list && this.list.folder) {
		return this.list.folder.isInTrash();
	} else {
		return false;
	}
};

ZmVoiceItem.prototype.getCallingParty =
function(type) {
	return this._callingParties[type];
};

ZmVoiceItem.prototype._loadFromDom =
function(node) {
	if (node.id) this.id = node.id;
	if (node.cp) {
		for(var i = 0, count = node.cp.length; i < count; i++) {
			var callingParty = new ZmCallingParty();
			callingParty._loadFromDom(node.cp[i]);
			this._callingParties[callingParty.type] = callingParty;
		}
	}
	if (node.d) this.date = new Date(node.d);
	if (node.du) this.duration = new Date(node.du * 1000);
};

