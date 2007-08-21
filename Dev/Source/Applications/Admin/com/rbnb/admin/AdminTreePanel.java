/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/


package com.rbnb.admin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.rbnb.api.*;
import com.rbnb.utility.Utility;

/******************************************************************************
 * Displays <code>Rmap</code> information in a tree view format.
 * <p>
 * To implement this, a JTree is created to display the <code>Rmap</code>
 * information; this JTree is then placed in a JScrollPane and the JScrollPane
 * is then added to the AdminTreePanel.
 * <p>
 *
 * @author John P. Wilson
 * @author Ian A. Brown
 *
 * @see com.rbnb.api.Rmap
 * @since V2.0
 * @version 09/19/2005
 */

/*
 * Copyright 2001,2002,2005 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/19/2005  JPW	a) Make sure there is *something* under all PlugIn
 *			   nodes so that these nodes end up having a toggle
 *			   handle (used	for performing auto-updates).
 *			b) In treeWillExpand(), add auto-refresh to PlugIns
 *			c) Add "Refresh" to PlugIn popup menu
 * 01/31/2005  JPW	When mirroring from a local source (the "Mirror to..."
 *			case) do time-based mirror if mirroring from oldest
 *			and the source has an archive.
 * 01/06/2005  JPW	Add "Load archive..." menu item to the connected server
 * 04/21/2003  JPW	"Terminate..." menu item is now displayed on servers
 *			that are children of the connected server; this is
 *			used to Terminate the parent/child connection.
 * 03/14/2002  INB	Eliminated null pointer problem in addNode and in
 *			update.
 * 03/01/2002  INB	Eliminated server names from mirrors.
 * 02/11/2002  JPW	Turn off the "WillExpand" listener and thereby halt
 *			    auto-updates when expanding nodes in "update()";
 *			    this prevents a repeating expansion/update bug.
 *			Add new member variable: willExpandListener
 * 01/30/2002  JPW	Add WillExpandClass; add an instance of this class
 *              	    as an expansion listener for the JTree.
 * 01/29/2002  JPW	Add support for displaying PlugIn objects
 * 01/22/2002  JPW	Add support for displaying Shortcut objects
 * 06/04/2001  JPW	Order nodes in the tree:
 *			    Controllers, Sinks, Sources, Servers
 * 05/01/2001  JPW	Created.
 *
 */

public class AdminTreePanel extends JPanel implements ActionListener {
    
    /**
     * Some general info on terms used in JTree's documentation:
     * expanded node: a node which displays its children
     * collapsed node: a node which hides its children
     * hidden node: a node which resides under a collapsed parent
     * viewable node: a node which is under an expanded parent; may or may
     *                not be displayed
     * displayed node: a node which is both viewable *and* in the display
     *                 area (i.e. it can be seen by the user)
     */
    
    /**
     * Top level Admin class for which this information is displayed.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private Admin admin = null;
    
    /**
     * Model used by the JTree.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private DefaultTreeModel rmapTreeModel = null;
    
    /**
     * JTree's root node.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private DefaultMutableTreeNode rmapRootNode = null;
    
    /**
     * Currently selected node.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/30/2001
     */
    private DefaultMutableTreeNode selectedNode = null;
    
    /**
     * Tree which is displaying <code>Rmap</code> information.
     * <p>
     *
     * @author John P. Wilson
     *
     * @see com.rbnb.api.Rmap
     * @since V2.0
     * @version 05/01/2001
     */
    private JTree rmapTree = null;
    
    /**
     * Tree cell renderer for displaying the appropriate node icons.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/23/2001
     */
    private AdminTreeCellRenderer cellRenderer = null;
    
    /**
     * Scrolling window which contains the JTree.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */
    private JScrollPane rmapScrollPane = null;
    
    /**
     * Used for handling "WillExpand" events from the JTree which fire
     * when the user clicks the small "+" toggles.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 02/11/2002
     */
    private WillExpandClass willExpandListener = null;
    
    /**************************************************************************
     * Create the tree to display <code>Rmap</code> information.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param adminI  top level class for whom the tree is displaying its
     *                information
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  JPW  Add an instance of WillExpandClass as an expansion
     *                  listener for the JTree.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public AdminTreePanel(Admin adminI) {
	
	super();
	
	admin = adminI;
	
	setFont(new Font("Dialog", Font.PLAIN, 12));
	setBackground(Color.white);
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 100;
	gbc.weighty = 100;
	
	// Create the JTree
	rmapRootNode =
	    new DefaultMutableTreeNode("Rmap");
	rmapTree = new JTree(rmapRootNode);
	// JPW 02/14/2002: Add the "admin" argument to
	//                 AdminTreeCellRenderer's constructor
	cellRenderer = new AdminTreeCellRenderer(admin);
	rmapTree.setCellRenderer(cellRenderer);
	rmapTree.getSelectionModel().setSelectionMode
	    (TreeSelectionModel.SINGLE_TREE_SELECTION);
	rmapTreeModel = (DefaultTreeModel)rmapTree.getModel();
	// Don't display the root node
	rmapTree.setRootVisible(false);
	// Show the toggle button/handles on the root nodes
	rmapTree.setShowsRootHandles(true);
	// Show lines joining parents and children
	rmapTree.putClientProperty("JTree.lineStyle","Angled");
	
	// Add the JTree to a JScrollPane
	rmapScrollPane = new JScrollPane();
	// Have an empty border between the tree and the scroll pane
	rmapScrollPane.setViewportBorder( new EmptyBorder(5,5,5,5) );
	rmapScrollPane.getViewport().setView(rmapTree);
	// Set the background color so the empty border is painted white
	rmapScrollPane.setBackground(Color.white);
	
	// Add the JScrollPane to this AdminTreePanel
	gbc.insets = new Insets(0, 0, 0, 0);
	Utility.add(this,rmapScrollPane,gbl,gbc,0,0,1,1);
	
	MouseHandlerClass mhc = new MouseHandlerClass(this);
	rmapTree.addMouseListener(mhc);
	
	KeyClass kc = new KeyClass();
	rmapTree.addKeyListener(kc);
	
	// JPW 01/30/2002: Add the expansion listener
	willExpandListener = new WillExpandClass();
	rmapTree.addTreeWillExpandListener(willExpandListener);
	
    }
    
    /**************************************************************************
     * Reset the tree view.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param nodeI  Reset from this node down. If this node is null, then
     *               remove everything below the root node (but leave the
     *               root node alone).
     * @since V2.0
     * @version 02/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/14/2002  JPW  When nodeI isn't a root node: now just remove its children;
     *                      don't remove this node itself (we'll reuse this node in
     *			    the tree.
     * 12/27/2001  JPW	Add nodeI argument.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void reset(DefaultMutableTreeNode nodeI) {
	
	// Two equivalent ways I've found to do this reset:
	
	// Method 1: create and use a new root node and tree model
	// rmapRootNode = new DefaultMutableTreeNode("Rmap");
	// rmapTreeModel = new DefaultTreeModel(rmapRootNode);
	// rmapTree.setModel(rmapTreeModel);
	
	// Method 2: remove children from the node
	if ( (nodeI == null) || (nodeI == rmapRootNode) ) {
	    // Remove everything below the root node (leave root node alone).
	    rmapRootNode.removeAllChildren();
	} else {
	    // JPW 02/14/2002: No longer remove this node *and* its children;
	    //                 just remove this node's children.  We'll keep
	    //                 this node in place and reuse it as we update
	    //                 the tree.
	    // nodeI.removeFromParent();
	    nodeI.removeAllChildren();
	}
	rmapTreeModel.reload();
	
    }
    
    /**************************************************************************
     * Reset the tree view and then, if the provided Rmap object is not
     * null, display the new Rmap.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param fullPathNameI  Full path to the object of interest.  If this
     *                       argument is null, then request the full hierarchy
     *                       from the ultimate parent server down (from
     *                       rmapRootNode on down).
     * @param rmapI  <code>Rmap</code> to display
     * @since V2.0
     * @version 03/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 03/14/2002  INB	Ignore nodes where there is no Rmap.
     * 02/11/2002  JPW	Turn off the "WillExpand" listener and thereby halt
     *			    auto-updates when expanding nodes; this prevents
     *			    a repeating expansion/update bug.
     * 12/27/2001  JPW	Add fullPathNameI argument.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void update(String fullPathNameI, Rmap rmapI) throws Exception {
	
	if ( (fullPathNameI != null) && (fullPathNameI.equals("")) ) {
	    throw new Exception("Bad format for tree node to update.");
	}
	
	// JPW 12/27/2001: Find the node in the tree whose stored Rmap object's
	//                 name matches fullPathNameI; also, save this node's
	//                 parent node.
	DefaultMutableTreeNode nodeToUpdate = null;
	DefaultMutableTreeNode parentNode = null;
	if (fullPathNameI != null) {
	    for (Enumeration e = rmapRootNode.breadthFirstEnumeration();
		 e.hasMoreElements();)
	    {
		DefaultMutableTreeNode node =
		    (DefaultMutableTreeNode)e.nextElement();
		Object userObj = node.getUserObject();
		if ( (userObj == null) ||
		     ((userObj instanceof AdminTreeUserObject) == false) )
		{
		    continue;
		}
		AdminTreeUserObject treeObj = (AdminTreeUserObject)userObj;
		Rmap rmap = treeObj.getRmap();
		String nodePath = rmap.getFullName();
		if (nodePath.equals(fullPathNameI)) {
		    nodeToUpdate = node;
		    parentNode =
			(DefaultMutableTreeNode)nodeToUpdate.getParent();
		    break;
		}
	    }
	    if (nodeToUpdate == null) {
		throw new Exception("Unknown full path name to tree node.");
	    }
	}
	
	// JPW 12/27/2001: Find the spot in rmapI where fullPathNameI begins
	Rmap startingRmap = null;
	if (fullPathNameI != null) {
	    startingRmap = rmapI.findDescendant(fullPathNameI,false);
	}
	
	// Save currently expanded nodes (from rmapRootNode on down)
	// NOTE: Unfortunately, this will not return those nodes which are
	//       expanded but under a collapsed ancestor.  There is no easy
	//       way to find (or set) this type of expanded node.  One possible
	//       solution is to use the "ExpansionCache" class I found from
	//       a newsgroup (see ~jpw/ExpansionCache.java).
	Enumeration expandedPaths =
	    rmapTree.getExpandedDescendants(new TreePath(rmapRootNode));
	Vector expandedPathStrings = new Vector();
	if (expandedPaths != null) {
	    for (Enumeration e = expandedPaths; e.hasMoreElements();) {
	        TreePath path = (TreePath)e.nextElement();
		expandedPathStrings.addElement(path.toString());
	    }
	}
	/*
	// The following code will make sure expandedPathStrings includes the
	// path of the currently selected node (if it hasn't already been
	// included).  This code isn't needed because we do specifically
	// expand nodeToUpdate below.
	if (fullPathNameI != null) {
	    TreeNode[] nodes = rmapTreeModel.getPathToRoot(nodeToUpdate);
	    TreePath path = new TreePath(nodes);
	    String pathStr = path.toString();
	    if (!expandedPathStrings.contains(path.toString())) {
		expandedPathStrings.addElement(path.toString());
	    }
	}
	*/
	
	//
	// RESET THE TREE VIEW
	//
	
	if (fullPathNameI == null) {
	    reset(null);
	} else {
	    reset(nodeToUpdate);
	}
	
	if (rmapI == null) {
	    // We're done!
	    return;
	}
	
	//
	// CREATE THE NEW TREE VIEW
	//
	if (fullPathNameI == null) {
	    // We are doing a full refresh
	    // NOTE: Skip over the top level EndOfStream Rmap
	    if (rmapI instanceof EndOfStream) {
		addNode(rmapRootNode, rmapI, true);
	    } else {
		addNode(rmapRootNode, rmapI, false);
	    }
	} else {
	    // addNode(parentNode, startingRmap, false);
	    // Reset node's user object
	    // INB 03/14/2002: Ignoree node if there is no Rmap.
	    if (startingRmap != null) {
		nodeToUpdate.setUserObject(
		    new AdminTreeUserObject(startingRmap));
		addNode(nodeToUpdate, startingRmap, true);
	    }
	}
	
	//
	// EXPAND PATHS
	//
	
	// JPW 2/11/2002: Turn off auto updating when a node expands (this
	//                prevents a continual expand/update bug).
	rmapTree.removeTreeWillExpandListener(willExpandListener);
	
	// JPW 2/11/2002: In case an Exception is thrown, must make sure to
	//                add the "WillExpandListener" back before throwing
	try {
	    if (expandedPathStrings.size() == 0) {
	        // Due to a bug in the JDK, must *at least* expand the root
	        // node after it has been set invisible in order for its
	        //  children to show up.  We expand down 2 levels here.
	        for (Enumeration e = rmapRootNode.children();
	             e.hasMoreElements();)
	        {
	            DefaultMutableTreeNode nextChild =
		        (DefaultMutableTreeNode)e.nextElement();
	            TreeNode[] nodes = rmapTreeModel.getPathToRoot(nextChild);
	            TreePath path = new TreePath(nodes);
	            rmapTree.expandPath(path);
	        }
	    }
	    else {
	        // Go through the new tree and expand those paths that
	        // were previously expanded
	        setExpandedPaths(rmapRootNode,expandedPathStrings);
	    }
	    
	    // JPW 12/27/2001: Make sure the node that has been updated is
	    //                 expanded down one level.  Only need to do this
	    //                 if we haven't done a full refresh.
	    if (fullPathNameI != null) {
	        TreeNode[] nodes = rmapTreeModel.getPathToRoot(nodeToUpdate);
	        TreePath path = new TreePath(nodes);
	        rmapTree.expandPath(path);
	    }
	    
	} catch (Exception e) {
	    // Must add "WillExpandListener" back before throwing exception
	    rmapTree.addTreeWillExpandListener(willExpandListener);
	    throw e;
	}
	
	rmapTree.addTreeWillExpandListener(willExpandListener);
	
    }
    
    /**************************************************************************
     * If the String representation of the given node's path is contained in
     * the given Vector of Strings, then this node should be expanded.
     * Recursively call this method on all of the given node's child nodes.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param treeNodeI  Should this node (or any of its children) be expanded?
     * @param expandedPathStringsI  Vector containing String descriptions of
     *                              the paths that were previously expanded.
     * @since V2.0
     * @version 02/15/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/15/2002  JPW	Don't expand "N/A" filler nodes.
     * 05/29/2001  JPW  Created.
     *
     */
    
    private void setExpandedPaths(
	DefaultMutableTreeNode treeNodeI,
	Vector expandedPathStringsI)
    {
	
	// JPW 02/15/2002: Do not expand the path if this node only contains
	//                 one child which is a "filler" node - that is, a node
	//                 whose name is "N/A" and which doesn't have an
	//                 AdminTreeUserObject as its stored user object.
	if (treeNodeI.getChildCount() == 1) {
	    DefaultMutableTreeNode onlyChild =
	        (DefaultMutableTreeNode)treeNodeI.getFirstChild();
	    String nodeLabel = onlyChild.toString();
	    if ( (nodeLabel != null) && (nodeLabel.equals("N/A")) ) {
		// Don't expand this node
		return;
	    }
	}
	
	// First, see if treeNodeI needs to be expanded
	TreeNode[] nodes = rmapTreeModel.getPathToRoot(treeNodeI);
	TreePath path = new TreePath(nodes);
	if (expandedPathStringsI.contains(path.toString())) {
	    rmapTree.expandPath(path);
	}
	
	// Second, recursively call setExpandedPaths() on all child nodes
	for (Enumeration e = treeNodeI.children(); e.hasMoreElements();) {
	    DefaultMutableTreeNode nextChild =
		(DefaultMutableTreeNode)e.nextElement();
	    setExpandedPaths(nextChild,expandedPathStringsI);
	}
    }
    
    /**************************************************************************
     * Create a node for the given <code>Rmap</code> and add this node as a
     * child to parentNodeI.
     * <p>
     * If rmapI has children, recursively call <code>addNode()</code> to add
     * these children to the tree.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param parentNodeI  add new node as a child of this parent
     * @param rmapI  create a new node for this <code>Rmap</code>
     * @param bJustAddChildrenI  If true, add nodes representing the children
     *				 of rmapI to parentNodeI (but do not add a node
     *				 representing rmapI directly).  Otherwise,
     *				 just add the children of rmapI to the tree.
     * @exception java.lang.Exception
     *            thrown if there is an error accessing information
     *            from the <code>Rmap</code>
     * @since V2.0
     * @version 09/19/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/20/2005  JPW	For all nodes which aren't Servers, Controllers, Sinks,
     *			Sources, Shortcuts, or PlugIns: add a "Refresh" menu.
     *			This can be used, for instance, on a leaf node which
     *			is actually a PlugIn - can Refresh it and see its
     *			children.
     * 09/19/2005  JPW  Make sure there is *something* under all PlugIn nodes
     *			so that these nodes end up having a toggle handle (used
     *			for performing auto-updates).
     * 03/14/2002  INB	Check for null input.
     * 02/13/2002  JPW  Make sure there is *something* under all Server and
     *                  Shortcut nodes so that these nodes end up having a
     *                  a toggle handle (used for performing auto-updates).
     * 01/29/2002  JPW	Add support for displaying PlugIn objects.
     * 01/22/2002  JPW	Add support for displaying Shortcut objects.
     * 01/22/2002  JPW	Only add "special" objects (that is, those whose names
     *			 begin with an "_" character) if Admin.addHidden is
     *			 true.
     * 06/04/2001  JPW	Add bJustAddChildrenI; when adding children, add them
     *                  in order: Controller, Sink, Source, Server.
     * 05/01/2001  JPW  Created.
     *
     */
    
    private void addNode(
	DefaultMutableTreeNode parentNodeI,
	Rmap rmapI,
	boolean bJustAddChildrenI)
    throws Exception
    {
	
	// INB 03/14/2002: Look for null input (probably means that something
	// went away between refreshes).
	if (rmapI == null) {
	    return;
	}
	
	/** Display the Rmaps for each PlugIn's child
	if ( (parentNodeI != null) &&
	     (parentNodeI.getUserObject() != null) &&
	     (parentNodeI.getUserObject() instanceof AdminTreeUserObject) )
	{
	    AdminTreeUserObject userObj =
	        (AdminTreeUserObject)parentNodeI.getUserObject();
	    Rmap parentRmap = userObj.getRmap();
	    if ( (parentRmap != null) &&
	         (parentRmap instanceof PlugIn) )
	    {
		System.err.println("\n\nParent Rmap:\n" + parentRmap);
		System.err.println("Child Rmap:\n" + rmapI);
	    }
	}
	**/
	
	// JPW 01/22/2002: Check to see if this is a "special" object
	String rmapName = rmapI.getName();
	if (admin.addHidden == false) {
	    if ( (rmapName != null) && (rmapName.startsWith("_")) ) {
		return;
	    }
	}
	
	int numChildren = rmapI.getNchildren();
	
	DefaultMutableTreeNode parentNode = parentNodeI;
	
	if (!bJustAddChildrenI) {
	    
	    // Add a node representing rmapI to the tree
	    
	    // Make the new node
	    DefaultMutableTreeNode newNode =
	        new DefaultMutableTreeNode(new AdminTreeUserObject(rmapI));
	    
	    parentNode = newNode;
	    
	    boolean isLeaf = true;
	    if (numChildren > 0) {
	        isLeaf = false;
	    }
	    
	    // Add the node node to the parent
	    // NOTE: Use DefaultTreeModel.insertNodeInto() to add the new node;
	    //       this will appropriately update the model as changes occur.
	    
	    // JPW 6/4/2001: Just add node to the end of the parent's branch
	    rmapTreeModel.insertNodeInto(
	        newNode, parentNodeI, parentNodeI.getChildCount());
	    /*
	    if ( (isLeaf) || (parentNodeI.getChildCount() == 0) ) {
	        // Either the new node is a leaf or else it is the first node
	        // to be added to the parent; in either case, just add the new
	        // node to the end of the parent's branch
	        rmapTreeModel.insertNodeInto(
	           newNode, parentNodeI, parentNodeI.getChildCount());
	    }
	    else {
	        // The new node is a folder node (i.e. it will eventually have
	        // children under it); add this node to the end of the parent
	        // node's child folders
	        // Search for first leaf and insert the new node at that index
	        boolean doneInsert = false;
	        for (int i = 0; i < parentNodeI.getChildCount(); ++i) {
		    DefaultMutableTreeNode nextChild =
		        (DefaultMutableTreeNode)parentNodeI.getChildAt(i);
		    if (nextChild.isLeaf()) {
		        rmapTreeModel.insertNodeInto(newNode, parentNodeI, i);
		        doneInsert = true;
		        break;
		    }
	        }
	        if (!doneInsert) {
		    // It must be that the parent only consists of folders;
		    // insert the new node at the end
		    rmapTreeModel.insertNodeInto(
		        newNode, parentNodeI, parentNodeI.getChildCount());
	        }
	    }
	    */
	}
	
	//////////////////////////////////////////////////////////////
	//
	// Now, recursively call addNode() to add children to the tree
	//
	//////////////////////////////////////////////////////////////
	
	// JPW 01/29/2002: Add PlugIns
	// JPW 01/22/2002: Add Shortcuts
	// JPW 6/4/2001: Add nodes in the following order:
	//               Controllers, Sinks, Sources, Routed DTs
	
	// An array to keep track of which nodes have been added (we need
	// this because Sink is a subclass of Source and we therefore don't
	// want to add Sinks twice)
	byte[] childHasBeenAdded = new byte[numChildren];
	for (int i = 0; i < numChildren; ++i) {
	    childHasBeenAdded[i] = 0;
	}
	
	// ADD CONTROLLERS
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if ( (childRmap instanceof Controller) &&
		 (childHasBeenAdded[i] == 0) )
	    {
		addNode(parentNode, childRmap, false);
		childHasBeenAdded[i] = 1;
	    }
	}
	
	// ADD SINKS
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if ( (childRmap instanceof Sink) &&
		 (childHasBeenAdded[i] == 0) )
	    {
		addNode(parentNode, childRmap, false);
		childHasBeenAdded[i] = 1;
	    }
	}
	
	// ADD SOURCES
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if ( (childRmap instanceof Source) &&
		 (childHasBeenAdded[i] == 0) )
	    {
		addNode(parentNode, childRmap, false);
		childHasBeenAdded[i] = 1;
	    }
	}
	
	// JPW 01/29/2002: ADD PLUGINS
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if ( (childRmap instanceof PlugIn) &&
		 (childHasBeenAdded[i] == 0) )
	    {
		addNode(parentNode, childRmap, false);
		childHasBeenAdded[i] = 1;
	    }
	}
	
	// ADD SERVERS
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if ( (childRmap instanceof Server) &&
		 (childHasBeenAdded[i] == 0) )
	    {
		addNode(parentNode, childRmap, false);
	        childHasBeenAdded[i] = 1;
	    }
	}
	
	// ADD SHORTCUTS
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if ( (childRmap instanceof Shortcut) &&
		 (childHasBeenAdded[i] == 0) )
	    {
		addNode(parentNode, childRmap, false);
	        childHasBeenAdded[i] = 1;
	    }
	}
	
	// ADD EVERYTHING ELSE
	for (int i = 0; i < numChildren; ++i) {
	    Rmap childRmap = rmapI.getChildAt(i);
	    if (childHasBeenAdded[i] == 0) {
		addNode(parentNode, childRmap, false);
	    }
	}
	
	// Make sure there is *something* under all PlugIn, Server, and
	// Shortcut nodes so that these nodes end up having a toggle handle;
	// we do an auto-refresh when the user expands these nodes
	// JPW 09/19/2005: Add PlugIns to the list of objects that needs a
	//                 handle (so we can do auto-refresh on the expansion)
	if ( ( (rmapI instanceof PlugIn) ||
	       (rmapI instanceof Server) ||
	       (rmapI instanceof Shortcut) ) &&
	     ( parentNode.getChildCount() == 0 ) )
	{
	  // Add a dummy child so we get a toggle
	  DefaultMutableTreeNode dummyNode = new DefaultMutableTreeNode("N/A");
	  rmapTreeModel.insertNodeInto(dummyNode, parentNode, 0);
	}
	
    }
    
    /**************************************************************************
     * Action callback method.
     * <p>
     *
     * @author John P. Wilson
     * @author Ian A. Brown
     *
     * @param event ActionEvent that has been fired
     * @since V2.0
     * @version 01/31/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/31/2005  JPW  Must do a time-based mirror if mirroring from oldest
     *			from a Source which has an archive.
     * 01/24/2005  JPW  Add usernameStr to the argument vector for the load
     *			   archive action.
     * 01/06/2005  JPW	"Load archive..." is now displayed on the menu for
     *			   the connected server
     * 04/21/2003  JPW	"Terminate..." is now displayed for children
     *			   of the connected server; this is used
     *			   to Terminate the parent/child connection.
     * 03/01/2002  INB	Eliminated server names from mirrors.
     * 01/29/2002  JPW	Add support for terminating PlugIn objects.
     * 05/01/2001  JPW  Created.
     *
     */
    
    public void actionPerformed(ActionEvent event) {
	
	if (selectedNode == null) {
	    return;
	}
	
	String label = event.getActionCommand();
	
	RBNBDataManager dataManager = admin.getRBNBDataManager();
	// Get the object stored in the selected node
	Object userObj = selectedNode.getUserObject();
	if ( (userObj == null) ||
	     ((userObj instanceof AdminTreeUserObject) == false) )
	{
	    return;
	}
	
	AdminTreeUserObject treeObj = (AdminTreeUserObject)userObj;
	Rmap rmap = treeObj.getRmap();
	
	///////////////////////////////////////////////////////////
	//
	// MIRROR FROM ONE OR MORE SOURCES TO THE LOCAL DATATURBINE
	//
	///////////////////////////////////////////////////////////
	
	if (label.equals("Mirror from...")) {
	    MirrorDialog mirrorDialog =
		new MirrorDialog(admin,
                                 true,
                                 null,
				 null,
				 dataManager.getServerAddress(),
                                 null,
                                 true,
                                 true,
                                 false,
                                 true,
				 100,
				 0,
				 Source.ACCESS_NONE,
			         true);
            mirrorDialog.show();
	    if (mirrorDialog.state == MirrorDialog.OK) {
		// Establish the mirror(s)
		Vector sources = mirrorDialog.multipleSourceInfo;
		for (int i = 0; i < sources.size(); ++i) {
		    SourceInfo si = (SourceInfo)(sources.elementAt(i));
	            try {
			String destinationDataPath = null;
			if ( (sources.size() > 1) &&
			     (mirrorDialog.destinationDataPathStr.equals("")
			                                         == false) )
			{
			    // We will append a ".<index>" to the mirror name
			    destinationDataPath =
				new String(
				    mirrorDialog.destinationDataPathStr +
				    "." +
				    Integer.toString(i+1));
			}
			else {
			    destinationDataPath =
				mirrorDialog.destinationDataPathStr;
			}
			// JPW 01/31/2005:
			// NOTE:
			// Just as in the "Mirror to..." case, if the source
			// being mirrored has an archive and the user wants to
			// mirror from oldest, then we need to do a time-based
			// mirror.  Currently, there is no way to determine
			// if the source (which might be on another server)
			// has an archive.
			if (System.getProperty("RBNBTIMEMIRRORS") == null) {
			    dataManager.createMirror
				(null,
				 si.addressStr,
				 null,
				 si.dataPathStr,
				 dataManager.getServer(),
				 dataManager.getServerAddress(),
				 destinationDataPath,
				 mirrorDialog.startChoice,
				 mirrorDialog.stopChoice,
				 mirrorDialog.numCacheFrames,
				 mirrorDialog.numArchiveFrames,
				 mirrorDialog.archiveMode,
				 mirrorDialog.bMatchSource);
			} else {
			    dataManager.createTimeMirror
				(null,
				 si.addressStr,
				 null,
				 si.dataPathStr,
				 dataManager.getServer(),
				 dataManager.getServerAddress(),
				 destinationDataPath,
				 mirrorDialog.startChoice,
				 mirrorDialog.stopChoice,
				 mirrorDialog.numCacheFrames,
				 mirrorDialog.numArchiveFrames,
				 mirrorDialog.archiveMode,
				 mirrorDialog.bMatchSource,
				 0.);
			}
	            } catch (Exception e) {
		        e.printStackTrace();
		        String errMsg = e.getMessage();
			if ( (errMsg == null) || (errMsg.equals("")) ) {
			    errMsg = "Error occurred setting up the mirror.";
			}
		        JOptionPane.showMessageDialog(
		            admin,
		            "Error creating mirror:\n" + errMsg,
		            "Mirror Error",
		            JOptionPane.ERROR_MESSAGE);
	            }
		}
		// Sleep for a bit and then schedule an update to be
		// processed by Admin's action thread
		try { Thread.sleep(1000); } catch (Exception e) {}
		admin.addAction(Admin.UPDATE_RMAP, null);
	    }
	    mirrorDialog.dispose();
	}
	
	//////////////////////////////////////////
	//
	// MIRROR FROM A LOCAL (NOT ROUTED) SOURCE
	//
	//////////////////////////////////////////
	
	else if (label.equals("Mirror to...")) {
	    MirrorDialog mirrorDialog =
		new MirrorDialog(admin,
                                 true,
                                 dataManager.getServerAddress(),
				 ((Source)rmap).getName(),
				 dataManager.getServerAddress(),
                                 null,
                                 false,
                                 false,
                                 true,
                                 true,
				 ((Source)rmap).getCframes(),
				 ((Source)rmap).getAframes(),
				 ((Source)rmap).getAmode(),
			         false);
            mirrorDialog.show();
	    if (mirrorDialog.state == MirrorDialog.OK) {
		// Establish the mirror
	        try {
		    // JPW 01/31/2005: Must do a time-based mirror if
		    //                 mirroring from oldest from a Source
		    //                 which has an archive
		    // NOTE: This should also be added to the "Mirror from..."
		    //       case found above.  However, in that case, there is
		    //       currently no way to determine if the source (which
		    //       might be on another machine) has an archive.
		    if ( (((Source)rmap).getAmode() !=
		                           SourceInterface.ACCESS_NONE) &&
		         (mirrorDialog.startChoice == MirrorDialog.OLDEST) )
		    {
			dataManager.createTimeMirror
			    (dataManager.getServer(),
			     dataManager.getServerAddress(),
			     (Source)rmap,
			     ((Source)rmap).getName(),
			     null,
			     mirrorDialog.destinationAddressStr,
			     mirrorDialog.destinationDataPathStr,
			     mirrorDialog.startChoice,
			     mirrorDialog.stopChoice,
			     mirrorDialog.numCacheFrames,
			     mirrorDialog.numArchiveFrames,
			     mirrorDialog.archiveMode,
			     mirrorDialog.bMatchSource,
			     0.);
		    }
		    else if (System.getProperty("RBNBTIMEMIRRORS") == null) {
			dataManager.createMirror
			    (dataManager.getServer(),
			     dataManager.getServerAddress(),
			     (Source)rmap,
			     ((Source)rmap).getName(),
			     null,
			     mirrorDialog.destinationAddressStr,
			     mirrorDialog.destinationDataPathStr,
			     mirrorDialog.startChoice,
			     mirrorDialog.stopChoice,
			     mirrorDialog.numCacheFrames,
			     mirrorDialog.numArchiveFrames,
			     mirrorDialog.archiveMode,
			     mirrorDialog.bMatchSource);
		    } else {
			dataManager.createTimeMirror
			    (dataManager.getServer(),
			     dataManager.getServerAddress(),
			     (Source)rmap,
			     ((Source)rmap).getName(),
			     null,
			     mirrorDialog.destinationAddressStr,
			     mirrorDialog.destinationDataPathStr,
			     mirrorDialog.startChoice,
			     mirrorDialog.stopChoice,
			     mirrorDialog.numCacheFrames,
			     mirrorDialog.numArchiveFrames,
			     mirrorDialog.archiveMode,
			     mirrorDialog.bMatchSource,
			     0.);
		    }
	        } catch (Exception e) {
		    e.printStackTrace();
		    String errMsg = e.getMessage();
		    if ( (errMsg == null) || (errMsg.equals("")) ) {
			errMsg = "Error occurred setting up the mirror.";
		    }
		    JOptionPane.showMessageDialog(
		        admin,
		        "Error creating mirror:\n" + errMsg,
		        "Mirror Error",
		        JOptionPane.ERROR_MESSAGE);
	        }
		// Sleep for a bit and then schedule an update to be
		// processed by Admin's action thread
		try { Thread.sleep(1000); } catch (Exception e) {}
		admin.addAction(Admin.UPDATE_RMAP, null);
	    }
	    mirrorDialog.dispose();
	}
	
	//////////////////////
	//
	// TERMINATE AN OBJECT
	//
	//////////////////////
	
	else if ( (label.equals("Terminate")) ||
		  (label.equals("Terminate...")) )
	{
	    // NOTE: Must test for instanceof Sink *before* testing for
	    //       instanceof Source since Sink is a subclass of Source.
	    
	    if (rmap instanceof Controller) {
		// Have user confirm before terminating
		String messageStr =
		    "Terminate the Controller \"" +
		    rmap.getName() +
		    "\"?";
		int userOption =
		    JOptionPane.showConfirmDialog(
			admin,
			messageStr,
			"Confirm Terminate",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if (userOption == JOptionPane.YES_OPTION) {
		    // If this is the Controller used for our current
		    // connection, go through the proper procedure of closing
		    // our connection.
		    Controller controller = (Controller)rmap;
		    Controller adminController = dataManager.getController();
		    if (controller.compareTo((Rmap)adminController) == 0) {
		        // Schedule a disconnect to be processed by Admin's
		        // action thread
		        admin.addAction(Admin.DISCONNECT, null);
		    } else {
		        // JPW 01/25/2002: TERMINATE is now handled by Admin's
		        //                 action thread.
		        admin.addAction(Admin.TERMINATE, rmap);
		    }
		}
	    } else if (rmap instanceof PlugIn) {
		String messageStr =
		    "Terminate the PlugIn \"" +
		    rmap.getName() +
		    "\"?";
		int userOption =
		    JOptionPane.showConfirmDialog(
			admin,
			messageStr,
			"Confirm Terminate",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if (userOption == JOptionPane.YES_OPTION) {
	            // JPW 01/25/2002: TERMINATE is now handled by Admin's
		    //                 action thread.
		    admin.addAction(Admin.TERMINATE, rmap);
		}
	    } else if (rmap instanceof Shortcut) {
		// Have user confirm before terminating
		String messageStr =
		    "Terminate the Shortcut \"" +
		    rmap.getName() +
		    "\" which points to the server named \"" +
		    ((Shortcut)rmap).getDestinationName() +
		    "\" at address " +
		    ((Shortcut)rmap).getDestinationAddress() +
		    "?";
		int userOption =
		    JOptionPane.showConfirmDialog(
			admin,
			messageStr,
			"Confirm Terminate",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if (userOption == JOptionPane.YES_OPTION) {
		    // JPW 01/25/2002: TERMINATE is now handled by Admin's
		    //                 action thread.
		    admin.addAction(Admin.TERMINATE, rmap);
		}
	    } else if (rmap instanceof Server) {
		// JPW 04/21/2003: If this is the connected Server, then
		//                 see if user wants to Terminate the Server;
		//                 If this is a child of the connected Server
		//                 then see if user wants to break the
		//                 Parent/Child connection.
		boolean bConnectedServer = false;
		try {
		    if (rmap.getFullName().equals(dataManager.getServerName()))
		    {
			bConnectedServer = true;
		    }
		} catch (Exception exception) {
		    System.err.println(
		        "ERROR obtaining full name from the Server's Rmap.");
		    return;
		}
		// Have user confirm before terminating
		String serverName = rmap.getName();
		String serverAddress = ((Server)rmap).getAddress();
		String messageStr = "";
		if (bConnectedServer) {
		    messageStr =
		        "Terminate Server \"" +
		        serverName +
		        "\" at address " +
		        serverAddress +
		        "?";
		} else {
		    messageStr =
		        "Terminate connection to child Server \"" +
		        serverName +
		        "\" at address " +
		        serverAddress +
		        "?";
		}
		int userOption =
		    JOptionPane.showConfirmDialog(
			admin,
			messageStr,
			"Confirm Terminate",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if (userOption == JOptionPane.YES_OPTION) {
		    // JPW 01/25/2002: TERMINATE is now handled by Admin's
		    //                 action thread.
		    admin.addAction(Admin.TERMINATE, rmap);
		}
	    } else if (rmap instanceof Sink) {
		String messageStr =
		    "Terminate the Sink \"" +
		    rmap.getName() +
		    "\"?";
		int userOption =
		    JOptionPane.showConfirmDialog(
			admin,
			messageStr,
			"Confirm Terminate",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if (userOption == JOptionPane.YES_OPTION) {
	            // JPW 01/25/2002: TERMINATE is now handled by Admin's
		    //                 action thread.
		    admin.addAction(Admin.TERMINATE, rmap);
		}
	    } else if (rmap instanceof Source) {
		String messageStr =
		    "Terminate the Source \"" +
		    rmap.getName() +
		    "\"?";
		int userOption =
		    JOptionPane.showConfirmDialog(
			admin,
			messageStr,
			"Confirm Terminate",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
		if (userOption == JOptionPane.YES_OPTION) {
	            // JPW 01/25/2002: TERMINATE is now handled by Admin's
		    //                 action thread.
		    admin.addAction(Admin.TERMINATE, rmap);
		}
	    }
	}
	
	////////////////////////////////////
	//
	// REQUEST THE CHILDREN OF THIS NODE
	//
	////////////////////////////////////
	
	else if (label.equals("Refresh")) {
	    try {
	        String fullName = rmap.getFullName();
		admin.addAction(Admin.UPDATE_RMAP, fullName);
	    } catch (Exception e) {
		
	    }
	}
	
	/////////////////////////////////////////////////////////
	//
	//  STARTUP A SHORTCUT FROM THIS SERVER TO ANOTHER SERVER
	//
	/////////////////////////////////////////////////////////
	
	else if (label.equals("Start shortcut...")) {
	    ShortcutDialog shortDlg =
	        new ShortcutDialog(
	            admin,true,"localhost:3333","Shortcut",1.0);
	    shortDlg.setVisible(true);
	    // Have the action thread in Admin start the shortcut
	    admin.addAction(Admin.START_SHORTCUT, shortDlg.shortcutData);
	}
	
	/////////////////////////////////////////////////////////
	//
	//  JPW 01/06/05: LOAD AN ARCHIVE ON THE CONNECTED SERVER
	//
	/////////////////////////////////////////////////////////
	
	else if (label.equals("Load archive...")) {
	    try {
		// JPW 01/24/2005: Add null for the usernameStr arg in the
		//                 constructor.
		LoadArchiveDialog archiveDlg =
		    new LoadArchiveDialog(admin,true,null,null,null);
		archiveDlg.setVisible(true);
		if (archiveDlg.state == LoadArchiveDialog.OK) {
		    // JPW 01/24/2005: Add usernameStr to the argument vector
		    Vector argVector = new Vector();
		    argVector.addElement(archiveDlg.archiveStr);
		    argVector.addElement(archiveDlg.usernameStr);
		    argVector.addElement(archiveDlg.passwordStr);
		    admin.addAction(Admin.LOAD_ARCHIVE, argVector);
		}
	    } catch (Exception e) {
		
	    }
	}
	
    }
    
    /**************************************************************************
     * Handle tree view expansion/collapse events.
     * <p>
     * Although this class captures all expand/collapse events before the node
     * is actually expanded or collapsed, we are only interested in responding
     * to expansion events.  If the user has clicked on a Server node, then
     * get a new Rmap corresponding to this Server and refresh the child nodes
     * of this node with the content of this Rmap.  In other words, expanding
     * a node automatically refreshes the view of this Server.
     * 
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 01/30/2002
     */

    /*
     * Copyright 2002 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/30/2002  JPW	Created.
     *
     */
    
    private class WillExpandClass implements TreeWillExpandListener {
	
	/**********************************************************************
	 * If the user has requested to refresh a Server node, then update
	 * this node's children by obtaining a new Rmap before displaying the
	 * expanded content.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param event  the event that was fired
	 * @since V2.0
	 * @version 09/19/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2005  JPW	Add auto-refresh to PlugIn nodes
	 * 01/30/2002  JPW	Created.
	 *
	 */
	
	public void treeWillExpand(TreeExpansionEvent e)
	throws ExpandVetoException {
	    
            // Obtain the name of the Rmap to be displayed
            TreePath path = e.getPath();
            if (path == null) {
		return;
	    }
	    DefaultMutableTreeNode node =
	        (DefaultMutableTreeNode)path.getLastPathComponent();
	    
	    // Get the Rmap object stored in this node
	    Object nodeObj = node.getUserObject();
	    if ( (nodeObj == null) ||
	         ((nodeObj instanceof AdminTreeUserObject) != true) )
	    {
		return;
	    }
	    
	    AdminTreeUserObject treeObj =
		(AdminTreeUserObject)node.getUserObject();
	    Rmap rmap = treeObj.getRmap();
	    
	    // Only request updates for PlugIns, Servers, and Shortcuts
	    // JPW 09/19/2005: Add auto-refresh to PlugIns
	    if  ( !(rmap instanceof PlugIn) &&
		  !(rmap instanceof Server) &&
	          !(rmap instanceof Shortcut) )
	    {
		return;
	    }
	    
	    String fullName = null;
	    try {
	        fullName = rmap.getFullName();
	    } catch (Exception exception) {
		System.err.println(
		    "ERROR obtaining full name from the Server's Rmap.");
		return;
	    }
	    
	    // Run into problems when I try to get a new Rmap here!
	    
	    // Get an updated Rmap
	    // METHOD 1: asynchronous
	    admin.addAction(Admin.UPDATE_RMAP, fullName);
	    // METHOD 2: SYNCHRONOUS
	    // admin.displayRmap(fullName);
	    
            // If this Rmap doesn't contain anything to put under this
            // node, then maybe just add a blank node saying
            // <nothing to display> or else we can cancel the expansion
            // as follows:
            /*
            if ( < need to cancel the expansion > ) {
                //Cancel expansion.
                throw new ExpandVetoException(e);
            }
            */
        }
	
	/**********************************************************************
	 * User is collapsing this branch of the tree; don't need to respond
	 * to this event (although this method is required in order to
	 * implement TreeWillExpandListener).
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param event  the event that was fired
	 * @since V2.0
	 * @version 01/30/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 01/30/2002  JPW	Created.
	 *
	 */
	
        public void treeWillCollapse(TreeExpansionEvent e) {
            // No need to do anything
        }
            
    }
    
    /**************************************************************************
     * Handle key press events.
     * <p>
     * Only interested in the event fired when the user presses the "F5" key;
     * in this case, perform a refresh.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */

    /*
     * Copyright 2001 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/01/2001  JPW	Created.
     *
     */
    
    private class KeyClass extends KeyAdapter {
	
	/**********************************************************************
	 * Process a key press event.
	 * <p>
	 * If "F5" has been pressed, schedule a refresh event.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param event  the event that was fired
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
    	public void keyPressed(KeyEvent event) {
    	    
    	    int modifiers = event.getModifiers();
    	    int key = event.getKeyCode();
	    
	    if (key == KeyEvent.VK_F5) {
		// Schedule an asynchronous update event.
	        admin.addAction(Admin.UPDATE_RMAP, null);
	    }
	    
    	}
    	
    }
    
    /**************************************************************************
     * Handle mouse events.
     * <p>
     * Only interested in mouse events for which a popup menu should be
     * displayed.  If the event is a popup event, then display a node-specific
     * popup menu.
     * <p>
     *
     * @author John P. Wilson
     *
     * @since V2.0
     * @version 05/01/2001
     */

    /*
     * Copyright 2001 Creare Inc.
     * All Rights Reserved
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 05/01/2001  JPW	Created.
     *
     */
    
    private class MouseHandlerClass extends MouseAdapter {
	
	AdminTreePanel parentPanel = null;
	
	/**********************************************************************
	 * Initialize the mouse class.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param parentPanelI  the panel to which popup meu events should be
	 *                      reported
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	public MouseHandlerClass(AdminTreePanel parentPanelI) {
	    parentPanel = parentPanelI;
	}
	
	/**********************************************************************
	 * Capture mouse click events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	public void mouseClicked(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse pressed events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	public void mousePressed(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse released events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	public void mouseReleased(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse entered events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	public void mouseEntered(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Capture mouse exited events; call <code>handlePopup()</code> for
	 * processing.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @see #handlePopup
	 * @since V2.0
	 * @version 05/01/2001
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	public void mouseExited(MouseEvent e) {
	    handlePopup(e);
	}
	
	/**********************************************************************
	 * Process a mouse event.  If appropriate, display a node-specific
	 * popup menu.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param e  the event that was fired
	 * @since V2.0
	 * @version 09/19/2005
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 09/19/2005  JPW	Add "Refresh" menu item to PlugIn menu
	 * 01/06/2005  JPW	"Load archive..." is menu item for the
	 *			   connected server only.
	 * 04/21/2003  JPW	"Terminate..." is menu item now displayed for
	 *			   servers that are children of the connected
	 *			   server; this will be used to Terminate the
	 *			   parent/child connection.
	 * 02/20/2002  JPW	For cases where the selected node is not a
	 *			    Server, check to see if the selected node
	 *			    is the child of the connected Server.  If
	 *			    it isn't, then most of the popup menu items
	 *			    should not be included.
	 * 02/14/2002  JPW	Check for cases where the node doesn't have a
	 *			    stored AdminTreeUserObject.
	 * 01/29/2002  JPW	Add menu items to PlugIn objects
	 * 01/23/2002  JPW	Add menu items to Shortcut objects; add
	 *			  "Start shortcut..." to Server nodes
	 * 05/01/2001  JPW	Created.
	 *
	 */
	
	private void handlePopup(MouseEvent e) {
	    
	    ////////////
	    // FIREWALLS
	    ////////////
	    
	    if (!e.isPopupTrigger()) {
		return;
	    }
	    
	    // Figure out what node was selected and make it the selected node
	    JTree tree = (JTree)e.getSource();
	    // int selRow = tree.getRowForLocation(e.getX(), e.getY());
	    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	    if (selPath == null) {
		return;
	    }
	    selectedNode =
		(DefaultMutableTreeNode)selPath.getLastPathComponent();
	    // Make this the selected node
	    tree.setSelectionPath(selPath);
	    
	    // Get the object stored in this node
	    // JPW 02/14/2002: If this node doesn't have a stored AdminTree-
	    //		       UserObject then just return (for example, this
	    //		       will be the case when this node is a "filler"
	    //		       node that I've added just so the server and
	    //		       shortcut nodes have a handle; in this case these
	    //		       nodes do not have an AdminTreeUserObject).
	    Object userObj = selectedNode.getUserObject();
	    if ( (userObj == null) ||
		 ((userObj instanceof AdminTreeUserObject) == false) )
	    {
		return;
	    }
	    AdminTreeUserObject treeObj =
		(AdminTreeUserObject)selectedNode.getUserObject();
	    Rmap rmap = treeObj.getRmap();
	    
	    JPopupMenu popupMenu = new JPopupMenu();
	    JMenuItem menuItem;
	    
	    RBNBDataManager dataManager = admin.getRBNBDataManager();
	    
	    if ( (rmap instanceof Controller) ||
	         (rmap instanceof PlugIn) ||
	         (rmap instanceof Shortcut) ||
	         (rmap instanceof Sink) ||
	         (rmap instanceof Source) )
	    {
		
		// Does this object belong to the Server we are connected to?
		// JPW 04/21/2003: Use new method: isChildOfConnectedServer()
		boolean bBelongsToConnectedServer =
		    isChildOfConnectedServer(selectedNode);
		
		if (rmap instanceof Controller) {
		    if (bBelongsToConnectedServer) {
		        menuItem = new JMenuItem("Terminate");
		        menuItem.addActionListener(parentPanel);
		        popupMenu.add(menuItem);
		    }
	        } else if (rmap instanceof PlugIn) {
		    // JPW 09/19/2005: Add "Refresh" menu item
		    menuItem = new JMenuItem("Refresh");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
	            if (bBelongsToConnectedServer) {
		        menuItem = new JMenuItem("Terminate");
		        menuItem.addActionListener(parentPanel);
		        popupMenu.add(menuItem);
		    }
	        } else if (rmap instanceof Shortcut) {
		    menuItem = new JMenuItem("Refresh");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
		    if (bBelongsToConnectedServer) {
		        menuItem = new JMenuItem("Terminate...");
		        menuItem.addActionListener(parentPanel);
		        popupMenu.add(menuItem);
		    }
		} else if (rmap instanceof Sink) {
		    if (bBelongsToConnectedServer) {
		        menuItem = new JMenuItem("Terminate");
		        menuItem.addActionListener(parentPanel);
		        popupMenu.add(menuItem);
		    }
	        } else if (rmap instanceof Source) {
	            if (bBelongsToConnectedServer) {
		        menuItem = new JMenuItem("Mirror to...");
		        menuItem.addActionListener(parentPanel);
		        popupMenu.add(menuItem);
		        menuItem = new JMenuItem("Terminate");
		        menuItem.addActionListener(parentPanel);
		        popupMenu.add(menuItem);
		    }
	        }
		
	    }
	    else if (rmap instanceof Server) {
		// JPW 01/06/2005: "Load archive..." is displayed when the
		//                 user has clicked on the node representing
		//                 the server we are actually currently
		//                 connected to.
		// JPW 04/21/2003: "Terminate..." is now displayed for servers
		//		   that are children of the connected server;
		//		   this will be used to Terminate the
		//		   parent/child connection.
		// JPW 01/30/2002: "Mirror from...", "Start shortcut...",
		//                 and "Terminate..." should only be
		//                 displayed when the user has clicked on
		//                 the node representing the server we are
		//                 actually currently connected to.
		boolean bConnectedServer = false;
		try {
		    if (rmap.getFullName().equals(dataManager.getServerName()))
		    {
			bConnectedServer = true;
		    }
		} catch (Exception exception) {
		    System.err.println(
		        "ERROR obtaining full name from the Server's Rmap.");
		    return;
		}
		
		// JPW 04/21/03: Is this server a direct child of the Server
		//               to which the user is connected?
		boolean bChildOfConnectedServer =
		    isChildOfConnectedServer(selectedNode);
		
		if (bConnectedServer) {
		    // JPW 01/06/05: Add "Load archive..."
		    menuItem = new JMenuItem("Load archive...");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
		    menuItem = new JMenuItem("Mirror from...");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
		}
		menuItem = new JMenuItem("Refresh");
		menuItem.addActionListener(parentPanel);
		popupMenu.add(menuItem);
		if (bConnectedServer) {
		    // JPW 01/23/2002: Add "Start shortcut..."
		    menuItem = new JMenuItem("Start shortcut...");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
		    menuItem = new JMenuItem("Terminate...");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
		} else if (bChildOfConnectedServer) {
		    // JPW 04/21/2003: Add "Terminate..." to the child server
		    menuItem = new JMenuItem("Terminate...");
		    menuItem.addActionListener(parentPanel);
		    popupMenu.add(menuItem);
		}
	    }
	    else {
		// JPW 09/20/2005: Add a Refresh menu to all other child nodes
		menuItem = new JMenuItem("Refresh");
		menuItem.addActionListener(parentPanel);
		popupMenu.add(menuItem);
	    }
	    
	    popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}
	
	/**********************************************************************
	 * Determine if the given node is a direct child of the Server to which
	 * the user is connected.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param childNodeI  node to be examined
	 * @since V2.0
	 * @version 04/21/2003
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 04/21/2003  JPW	Created.
	 *
	 */
	
	private boolean isChildOfConnectedServer(
	                                   DefaultMutableTreeNode childNodeI)
	{
	    
	    DefaultMutableTreeNode parentNode =
		findClosestParentShortcutOrServerNode(childNodeI);
	    
	    if (parentNode == null) {
		return false;
	    }
	    
	    Object userObject = parentNode.getUserObject();
	    if ( (userObject != null) &&
	         (userObject instanceof AdminTreeUserObject) )
	    {
		AdminTreeUserObject tempTreeObj =
		    (AdminTreeUserObject)userObject;
		Rmap tempRmap = tempTreeObj.getRmap();
		if (tempRmap instanceof Server) {
		    // See if this is the connected Server
		    try {
			
			RBNBDataManager dataManager =
			    admin.getRBNBDataManager();
			
			if (tempRmap.getFullName().equals(
			        dataManager.getServerName()))
			{
			    // YUP! This node is a direct child
			    // of the connected server.
			    return true;
			}
		    } catch (Exception exception) {
			System.err.println(
			"ERROR obtaining full name from the object's Rmap.");
			return false;
		    }
		}
	    }
	    
	    // Nope...this node is not a direct child of the connected server.
	    return false;
	    
	}
	
	/**********************************************************************
	 * Determine, by climbing up the JTree, childNodeI's closest ancestor
	 * node which is a Server or Shortcut.
	 * <p>
	 *
	 * @author John P. Wilson
	 *
	 * @param childNodeI  node whose closest server/shortcut
	 *                    parent node is desired
	 * @since V2.0
	 * @version 02/15/2002
	 */

	/*
	 *
	 *   Date      By	Description
	 * MM/DD/YYYY
	 * ----------  --	-----------
	 * 02/15/2002  JPW	Created.
	 *
	 */
	
	private DefaultMutableTreeNode findClosestParentShortcutOrServerNode(
	                                   DefaultMutableTreeNode childNodeI)
	{
	    if (childNodeI == null) {
		return null;
	    }
	    
	    DefaultMutableTreeNode currentNode = childNodeI;
	    
	    while (true) {
		currentNode = (DefaultMutableTreeNode)currentNode.getParent();
		if (currentNode == null) {
		    return null;
		}
		Object userObject = currentNode.getUserObject();
		if ( (userObject != null) &&
		     (userObject instanceof AdminTreeUserObject) )
		{
		    AdminTreeUserObject treeObj =
		        (AdminTreeUserObject)userObject;
		    Rmap rmap = treeObj.getRmap();
		    if ( (rmap instanceof Shortcut) ||
		         (rmap instanceof Server) )
		    {
			return currentNode;
		    }
		} // end if
	    } // end while
	    
	} // end findClosestParentShortcutOrServerNode()
	
    } // end private class MouseHandlerClass
    
}
