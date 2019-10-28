/*
 * Copyright (c) 2017 Mike Goppold von Lobsdorf
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import unnamed.common.*;

public class CFamilyNodeUtils {
	private CommonInt32Array bodyName;
	private CommonInt32Array relationsName;
	private CommonInt32Array defaultName;
	private CommonInt32Array extendsName;
	private CommonInt32Array implementsName;
	
	private BufferNode rightLenStrStack;
	private SortParams sortRec;
	
	public void init() {
		bodyName = makeStandardString("body");
		relationsName = makeStandardString("relations");
		defaultName = makeStandardString("default");
		extendsName = makeStandardString("extends");
		implementsName = makeStandardString("implements");
		
		sortRec = makeSortParams();
		
		rightLenStrStack = new BufferNode();
		rightLenStrStack.theObject = makeArrayList();
	}
	
	private CFamilyNode2 getSubNamespaceNode(
		CFamilyNode2 chunkNamespace,
		CommonInt32Array name) {
		
		CFamilyNode2 n;
		
		SortUtils.int32StringBinaryLookupSimple(
			chunkNamespace.sortedChildren, name, sortRec);
		
		if(!sortRec.foundExisting) return null;
		
		n = (CFamilyNode2) chunkNamespace.sortedChildren.get(sortRec.index);
		return n;
	}
	
	private CFamilyNode2 makeBasicSubNamespace(
		CFamilyNode2 namespaceNode, CommonInt32Array name) {
		
		CFamilyNode2 n;

		SortUtils.int32StringBinaryLookupSimple(
			namespaceNode.sortedChildren, name, sortRec);
		
		if(sortRec.foundExisting)
			throw makeObjectUnexpected(null);
		
		n = new CFamilyNode2();
		n.children = makeArrayList();
		n.sortedChildren = makeArrayList();
		n.sortObject = CommonIntArrayUtils.copy32(name);
		
		// commit
		n.parent = namespaceNode;
		namespaceNode.sortedChildren.addAt(sortRec.index, n);
		namespaceNode.children.add(n);
		
		return n;
	}
	
	public CommonInt32Array getDefaultPackagePath() {
		return defaultName;
	}
	
	public CFamilyNode2 makeChunkStack() {
		CFamilyNode2 n;
		
		n = new CFamilyNode2();
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_CHUNK_STACK;
		n.children = makeArrayList();
		
		return n;
	}

	public CFamilyNode2 makeChunkNamespace(CFamilyNode2 chunkStack) {
		CFamilyNode2 n;
		
		if(!getIsTypeMatchLen2(chunkStack,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CHUNK_STACK))
			throw makeInvalidEnum(null);
		
		n = new CFamilyNode2();
		
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_CHUNK_NAMESPACE;

		n.parent = chunkStack;
		n.children = makeArrayList();
		n.sortedChildren = makeArrayList();

		chunkStack.children.add(n);
		
		return n;
	}
	
	public CFamilyNode2 getPackage(
		CFamilyNode2 chunkNamespace, CommonInt32Array name) {
		
		CFamilyNode2 n;

		if(!getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CHUNK_NAMESPACE))
			throw makeInvalidEnum(null);
		
		n = getSubNamespaceNode(chunkNamespace, name);
		if(n == null) return null;
		
		if(!getIsTypeMatchLen2(n,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_PACKAGE))
			return null;
		
		return n;
	}

	public CFamilyNode2 makePackage(
		CFamilyNode2 chunkNamespace, CommonInt32Array name) {
		
		CFamilyNode2 n;

		if(!getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CHUNK_NAMESPACE))
			throw makeInvalidEnum(null);
		
		n = makeBasicSubNamespace(chunkNamespace, name);
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_PACKAGE;
		return n;
	}

	public CFamilyNode2 getClass(
		CFamilyNode2 namespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		SortParams sortRec;

		if(!getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_PACKAGE))
			throw makeObjectUnexpected(null);
		
		namespace2 = (CFamilyNode2) namespace;
		
		n = getSubNamespaceNode(namespace2, name);
		if(n == null) return null;
		
		if(!getIsTypeMatchLen2(n,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS))
			return null;
		
		return n;
	}

	public CFamilyNode2 makeClass(
		CFamilyNode2 namespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 n2;
		CFamilyNode2 namespace2;

		boolean ok;
		
		ok = false;

		if(getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_PACKAGE))
			ok = true;
			
		if(!ok) throw makeInvalidEnum(null);
		
		namespace2 = (CFamilyNode2) namespace;
		
		n = makeBasicSubNamespace(namespace2, name);
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_CLASS;
		
		n2 = makeBasicSubNamespace(n, relationsName);
		n2.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n2.theSubType = CFamilyNodeTypes.TYPE_SPECIAL_NAMESPACE;

		n2 = makeBasicSubNamespace(n, bodyName);
		n2.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n2.theSubType = CFamilyNodeTypes.TYPE_SPECIAL_NAMESPACE;
		
		return n;
	}
	
	private CFamilyNode2 getClassBodyFromClass(
		CFamilyNode2 chunkNamespace) {
		
		CFamilyNode2 n;
		
		if(!getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS))
			throw makeObjectUnexpected(null);
		
		n = getSubNamespaceNode(chunkNamespace, bodyName);
		if(n == null) throw makeObjectNotFound(null);

		return n;
	}
	
	private CFamilyNode2 getClassRelationsFromClass(
		CFamilyNode2 chunkNamespace) {
		
		CFamilyNode2 n;
		
		if(!getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS))
			throw makeObjectUnexpected(null);
		
		n = getSubNamespaceNode(chunkNamespace, relationsName);
		if(n == null) throw makeObjectNotFound(null);
		
		return n;
	}
	
	public CFamilyNode2 getInterface(
		CFamilyNode2 namespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		SortParams sortRec;

		if(!getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_PACKAGE))
			throw makeObjectUnexpected(null);
		
		namespace2 = (CFamilyNode2) namespace;
		
		n = getSubNamespaceNode(namespace2, name);
		if(n == null) return null;
		
		if(!getIsTypeMatchLen2(n,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_INTERFACE))
			return null;
		
		return n;
	}

	public CFamilyNode2 makeInterface(
		CFamilyNode2 namespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 n2;
		CFamilyNode2 namespace2;

		boolean ok;
		
		ok = false;

		if(getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_PACKAGE))
			ok = true;
			
		if(!ok) throw makeInvalidEnum(null);
		
		namespace2 = (CFamilyNode2) namespace;
		
		n = makeBasicSubNamespace(namespace2, name);
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_INTERFACE;
		
		n2 = makeBasicSubNamespace(n, relationsName);
		n2.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n2.theSubType = CFamilyNodeTypes.TYPE_SPECIAL_NAMESPACE;

		n2 = makeBasicSubNamespace(n, bodyName);
		n2.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n2.theSubType = CFamilyNodeTypes.TYPE_SPECIAL_NAMESPACE;
		
		return n;
	}

	private CFamilyNode2 getInterfaceBodyFromInterface(
		CFamilyNode2 chunkNamespace) {
		
		CFamilyNode2 n;
		
		if(!getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_INTERFACE))
			throw makeObjectUnexpected(null);
		
		n = getSubNamespaceNode(chunkNamespace, bodyName);
		if(n == null) throw makeObjectNotFound(null);

		return n;
	}
	
	private CFamilyNode2 getInterfaceRelationsFromInterface(
		CFamilyNode2 chunkNamespace) {
		
		CFamilyNode2 n;
		
		if(!getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_INTERFACE))
			throw makeObjectUnexpected(null);
		
		n = getSubNamespaceNode(chunkNamespace, relationsName);
		if(n == null) throw makeObjectNotFound(null);
		
		return n;
	}
	
	public CFamilyNode2 getFunction(
		CFamilyNode2 chunkNamespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		SortParams sortRec;
		
		namespace2 = null;

		if(getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS)) {
			
			namespace2 = getClassBodyFromClass(chunkNamespace);
		}
		
		if(getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_INTERFACE)) {
			
			namespace2 = getInterfaceBodyFromInterface(chunkNamespace);
		}
		
		if(namespace2 == null) throw makeObjectUnexpected(null);
		
		n = getSubNamespaceNode(namespace2, name);
		if(n == null) return null;
		
		if(!getIsTypeMatchLen2(n,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_FUNCTION))
			return null;
		
		return n;
	}

	public CFamilyNode2 makeFunction(
		CFamilyNode2 chunkNamespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;

		namespace2 = null;

		if(getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS)) {
			
			namespace2 = getClassBodyFromClass(chunkNamespace);
		}

		if(getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_INTERFACE)) {
			
			namespace2 = getInterfaceBodyFromInterface(chunkNamespace);
		}
		
		if(namespace2 == null) throw makeObjectUnexpected(null);
		
		n = makeBasicSubNamespace(namespace2, name);
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_FUNCTION;
		return n;
	}
	
	public CFamilyNode2 getVariable(
		CFamilyNode2 chunkNamespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		SortParams sortRec;
		
		namespace2 = null;

		if(getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS)) {

			namespace2 = getClassBodyFromClass(chunkNamespace);
		}
		
		if(namespace2 == null) throw makeObjectUnexpected(null);
		
		n = getSubNamespaceNode(namespace2, name);
		if(n == null) return null;
		
		if(!getIsTypeMatchLen2(n,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_VARIABLE))
			return null;
		
		return n;
	}

	public CFamilyNode2 makeVariable(
		CFamilyNode2 chunkNamespace, CommonInt32Array name) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		SortParams sortRec;

		namespace2 = null;

		if(getIsTypeMatchLen2(chunkNamespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS)) {
			
			namespace2 = getClassBodyFromClass(chunkNamespace);
		}
		
		if(namespace2 == null) throw makeObjectUnexpected(null);
		
		n = makeBasicSubNamespace(namespace2, name);
		n.theType = CFamilyNodeTypes.TYPE_DEFINITION;
		n.theSubType = CFamilyNodeTypes.TYPE_VARIABLE;
		return n;
	}
	
	public CFamilyNode2 makeRelationExtends(
		CFamilyNode2 namespace) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		int i;
		CommonInt32Array testName;

		namespace2 = null;

		if(getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS)) {
			
			namespace2 = getClassRelationsFromClass(namespace);
		}
		
		if(getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_INTERFACE)) {
			
			namespace2 = getInterfaceRelationsFromInterface(namespace);
		}

		i = 0;
		testName = null;
		while(true) {
			testName = PathUtils.combine2OptionalPaths(
					extendsName, getStringFromInt(i), '_');

			n = getSubNamespaceNode(namespace2, testName);
			if(n == null) break;

			i += 1;
		}

		n = makeBasicSubNamespace(namespace2, testName);
		return n;
	}

	public CFamilyNode2 makeRelationImplements(
		CFamilyNode2 namespace) {
		
		CFamilyNode2 n;
		CFamilyNode2 namespace2;
		int i;
		CommonInt32Array testName;

		namespace2 = null;

		if(getIsTypeMatchLen2(namespace,
			CFamilyNodeTypes.TYPE_DEFINITION,
			CFamilyNodeTypes.TYPE_CLASS)) {
			
			namespace2 = getClassRelationsFromClass(namespace);
		}
		
		i = 0;
		testName = null;
		while(true) {
			testName = PathUtils.combine2OptionalPaths(
					implementsName, getStringFromInt(i), '_');

			n = getSubNamespaceNode(namespace2, testName);
			if(n == null) break;

			i += 1;
		}

		n = makeBasicSubNamespace(namespace2, testName);
		return n;
	}

	
	// small utility functions
	//
	
	private boolean getIsTypeMatchLen2(TypeAndObject n, int type1, int type2) {
		if(n.theType != type1) return false;
		if(n.theSubType != type2) return false;
		return true;
	}

	private CommonInt32Array getStringFromInt(int num) {
		int i;
		int len = getIntBase10Len(num);
		int num2;
		
		CommonInt32Array str =
			getRightLengthInt32StringFromStack(
				rightLenStrStack, len);
		
		i = len;
		num2 = num;
		while(i > 0) {
			i -= 1;
			str.aryPtr[i] = num2 % 10;
			num2 /= 10;
		}
		
		return str;
	}

	private int getIntBase10Len(int num) {
		int i;
		int num2;

		i = 0;
		num2 = num;
		while(num2 > 0) {
			num2 /= 10;
			i += 1;
		}
		
		if(i == 0) return 1;
		return i;
	}

	// error allocators
	//
	
	private CommonError makeObjectNotFound(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeObjectUnexpected(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNEXPECTED_OBJECT;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}
	
	
	// stack allocators
	//
	
	public CommonInt32Array getRightLengthInt32StringFromStack(
		BufferNode storeHdr, int index) {
		
		int count;
		CommonInt32Array str;
		CommonArrayList store;
		
		store = (CommonArrayList) storeHdr.theObject;
		
		count = store.size();
		while(count <= index) {
			store.add(makeInt32Array(count));
			count = store.size();
		}
		
		str = (CommonInt32Array) store.get(index);
		return str;
	}

	
	// general allocators
	//
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;
		
		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}


	// native string small utility functions
	//
	
	private CommonInt32Array makeStandardString(String str) {
		return StringUtils.int32StringFromJavaString(str);
	}
}
