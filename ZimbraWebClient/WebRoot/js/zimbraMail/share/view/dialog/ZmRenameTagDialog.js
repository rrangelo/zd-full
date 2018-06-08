/*
 * 
 */

/**
 * @overview
 */

/**
 * Creates a rename tag dialog.
 * @class
 * This class represents a rename tag dialog.
 * 
 * @param	{DwtComposite}	parent		the parent
 * @param	{String}	className		the class name
 *  
 * @extends		ZmDialog
 */
ZmRenameTagDialog = function(parent, className) {

	ZmDialog.call(this, {parent:parent, className:className, title:ZmMsg.renameTag, id:"RenameTagDialog"});

	this._setNameField(this._nameFieldId);
};

ZmRenameTagDialog.prototype = new ZmDialog;
ZmRenameTagDialog.prototype.constructor = ZmRenameTagDialog;

ZmRenameTagDialog.prototype.toString = 
function() {
	return "ZmRenameTagDialog";
};

/**
 * Pops-up the dialog.
 * 
 * @param	{ZmTag}		tag		the tag
 * @param	{Object}		[source]	(not used)
 */
ZmRenameTagDialog.prototype.popup =
function(tag, source) {
	ZmDialog.prototype.popup.call(this);
	this.setTitle(ZmMsg.renameTag + ': ' + tag.getName(false, ZmOrganizer.MAX_DISPLAY_NAME_LENGTH));
	this._nameField.value = tag.getName(false, null, true);
	this._tag = tag;
};

ZmRenameTagDialog.prototype._contentHtml = 
function() {
	this._nameFieldId = this._htmlElId + "_name";
	var subs = {id:this._htmlElId, newLabel:ZmMsg.newTagName};
	return AjxTemplate.expand("share.Dialogs#ZmRenameDialog", subs);
};

ZmRenameTagDialog.prototype._okButtonListener =
function(ev) {
	var results = this._getTagData();
	if (results) {
		DwtDialog.prototype._buttonListener.call(this, ev, results);
	}
};

ZmRenameTagDialog.prototype._getTagData =
function() {
	// check name for presence and validity
	var name = AjxStringUtil.trim(this._nameField.value);
	var msg = ZmTag.checkName(name);
	
	// make sure tag name doesn't already exist
	if (!msg) {
		var tagTree = appCtxt.getTagTree();
		if (tagTree) {
			var t = tagTree.getByName(name);
			if (t && (t.id != this._tag.id)) {
				msg = ZmMsg.tagNameExists;
			}
		}
	}

	return (msg ? this._showError(msg) : [this._tag, name]);
};

ZmRenameTagDialog.prototype._enterListener =
function(ev) {
	var results = this._getTagData();
	if (results) {
		this._runEnterCallback(results);
	}
};
