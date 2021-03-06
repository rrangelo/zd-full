/*
 * 
 */

/**
 * @overview
 * This file contains the tree class
 */

/**
 * Creates the tree
 * @class
 * This class represents a tree.
 * 
 * @param	{constant}	type		the type
 * @extends	ZmModel
 */
ZmTree = function(type) {

	if (arguments.length == 0) { return; }
	ZmModel.call(this, type);

	this.type = type;
	this.root = null;
};

ZmTree.prototype = new ZmModel;
ZmTree.prototype.constructor = ZmTree;

/**
 * Returns a string representation of the object.
 * 
 * @return		{String}		a string representation of the object
 */
ZmTree.prototype.toString = 
function() {
	return "ZmTree";
};

/**
 * Gets this tree as a string.
 * 
 * @return	{String}	the tree
 */
ZmTree.prototype.asString = 
function() {
	return this.root ? this._asString(this.root, "") : "";
};

/**
 * Gets the item by id.
 * 
 * @param	{String}	id		the id
 * @return	{Object}	the item
 */
ZmTree.prototype.getById =
function(id) {
	return this.root ? this.root.getById(id) : null;
};

/**
 * Gets the item by name.
 * 
 * @param	{String}	name		the name
 * @return	{Object}	the item
 */
ZmTree.prototype.getByName =
function(name) {
	return this.root ? this.root.getByName(name) : null;
};

/**
 * Gets the item by type.
 * 
 * @param	{String}	name		the type name
 * @return	{Object}	the item
 */
ZmTree.prototype.getByType =
function(name) {
	return this.root ? this.root.getByType(name) : null;
};

/**
 * Gets the size of the tree.
 * 
 * @return	{int}	the size
 */
ZmTree.prototype.size =
function() {
	return this.root ? this.root.size() : 0;
};

/**
 * Resets the tree.
 */
ZmTree.prototype.reset =
function() {
	this.root = null;
};

/**
 * Gets the tree as a list.
 * 
 * @return	{Array}	an array
 */
ZmTree.prototype.asList =
function(options) {
	var list = [];
	return this.root ? this._addToList(this.root, list, options) : list;
};

/**
 * Gets the unread hash.
 * 
 * @param	{Hash}	unread		the unread hash
 * @return	{Hash} the unread tree as a hash
 */
ZmTree.prototype.getUnreadHash =
function(unread) {
	if (!unread) {
		unread = {};
	}
	return this.root ? this._getUnreadHash(this.root, unread) : unread;
};

/**
 * @private
 */
ZmTree.prototype._addToList =
function(organizer, list, options) {
	var incRemote = options && options.includeRemote;
	var remoteOnly = options && options.remoteOnly;
	var isRemote = organizer.isRemote();
	if ((!isRemote && !remoteOnly) || (isRemote && (remoteOnly || incRemote))) {
		list.push(organizer);
	}
	var children = organizer.children.getArray();
    for (var i = 0; i < children.length; i++) {
        this._addToList(children[i], list, options);
    }
	return list;
};

/**
 * @private
 */
ZmTree.prototype._asString =
function(organizer, str) {
	if (organizer.id) {
		str = str + organizer.id;
	}
	var children = organizer.children.clone().getArray();
	if (children.length) {
		children.sort(function(a,b){return a.id - b.id;});
		str = str + "[";
		for (var i = 0; i < children.length; i++) {
			if (children[i].id == ZmFolder.ID_TAGS) { // Tags "folder" added when view is set
				continue;
			}
			if (i > 0) {
				str = str + ",";
			}
			str = this._asString(children[i], str);
		}
		str = str + "]";
	}
	return str;
};

/**
 * @private
 */
ZmTree.prototype._getUnreadHash =
function(organizer, unread) {
	unread[organizer.id] = organizer.numUnread;
	var children = organizer.children.getArray();
	for (var i = 0; i < children.length; i++) {
		this._getUnreadHash(children[i], unread);
	}

	return unread;
};
