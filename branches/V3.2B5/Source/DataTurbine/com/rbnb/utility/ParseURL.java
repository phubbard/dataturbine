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


package com.rbnb.utility;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

/******************************************************************************
 * Parse a URL into its components.
 * <p>
 * Components in the URL include:
 * 
 *
 * @author John P. Wilson
 *
 * @version 09/28/2006
 */

/*
 * Copyright 2006 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By       Description
 * MM/DD/YYYY
 * ----------  --       -----------
 * 09/28/2006  JPW	RBNB servlet no longer supports special meaning for the
 *			"msg" munge.  Therefore, I have removed "msg" as an
 *			RBNB munge.  As part of this, I removed the method
 *			getCompleteMessageMunge() and the "message" member
 *			variable.
 * 09/15/2006  JPW      Switch over to using KeyValueHash to parse the munge
 * 05/10/2006  JPW      Created
 *
 */

public class ParseURL {
    
    // A copy of the original, full URL
    private String url;
    
    // Request protocol, such as "http" or "ftp"
    private String protocol = null;
    
    // The request string, stripped of any protocol and munge options
    private String request = null;
    
    // The original, complete munge string
    private String munge = null;
    
    // RBNB munge options
    private boolean hasRBNBMunge = false; //EMF 5/12/06
    private Double time = null;
    private Double duration = null;
    private String reference = null;
    private String fetch = null;
    private String byteorder = null;
    private String datatype = null;
    private Integer mux = null;
    private Integer blocksize = null;
    private String mime = null;
    
    // Hashtable of non-RBNB munge options
    private Hashtable nonRBNBMunge = null;
    
    /**************************************************************************
     * Default constructor.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/10/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/10/2006  JPW    Created
     *
     */
    
    public ParseURL() {
	this(null,false);
    }
    
    /**************************************************************************
     * Constructor.  Call parse() to parse the given URL into its components.
     * <p>
     *
     * @author John P. Wilson
     *
     * @param urlStrI      The URL to parse.
     * @param bDebugI      Print debug?
     *
     * @version 05/10/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/10/2006  JPW    Created
     *
     */
    
    public ParseURL(String urlStrI, boolean bDebugI) {
	parse(urlStrI,bDebugI);
    }
    
    /**************************************************************************
     * Parse the given URL into its components.
     * <p>
     * This method parses a munged URL such as:
     *
     *     http://jpw.creare.com:80/RBNB/TestSource?r=newest&t=1.5
     *
     * In this case:
     *     "http" is the protocol
     *     "jpw.creare.com:80/RBNB/TestSource" is the request
     *     "newest" is the reference
     *     "1.5" is the time
     *
     * @author John P. Wilson
     *
     * @param urlStrI      The URL to parse.
     * @param bDebugI      Print debug?
     *
     * @version 09/15/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 09/15/2006  JPW    Switch over to using KeyValueHash to parse the munge
     * 05/10/2006  JPW    Created
     *
     */
    
    public void parse(String urlStrI, boolean bDebugI) {
	
	// Reset all member variables
	resetMemberData();
	
	if ( (urlStrI == null) || (urlStrI.trim().equals("")) ) {
	    if (bDebugI) {
		System.err.println("Unable to parse URL: empty string");
	    }
	    return;
	}
	
	url = new String(urlStrI);
	
	if (bDebugI) {
	    System.err.println("URL = \"" + urlStrI + "\"");
	}
	
	////////////////////////////////////////////
	// See if a protocol is specified in the URL
	////////////////////////////////////////////
	int colonIndex = urlStrI.indexOf(':');
	String requestAndMungeStr = urlStrI;
	if (colonIndex >= 0) {
	    protocol = urlStrI.substring(0,colonIndex);
	    if ( (protocol != null) && (protocol.trim().equals("")) ) {
		protocol = null;
	    }
	    if ( (protocol != null) && (bDebugI) ) {
		System.err.println("Protocol = \"" + protocol + "\"");
	    } else if (bDebugI) {
		System.err.println("Protocol = null");
	    }
	    requestAndMungeStr = urlStrI.substring(colonIndex + 1);
	}
	
	////////////////////////////////////////////////
	// Strip off leading '/' from requestAndMungeStr
	////////////////////////////////////////////////
	while ( (requestAndMungeStr != null)     &&
		(!requestAndMungeStr.equals("")) &&
		(requestAndMungeStr.charAt(0) == '/') )
	{
	    requestAndMungeStr = requestAndMungeStr.substring(1);
	}
	
	///////////////////////////////////////////////////////////
	// Separate the request from the munge; munge portion could
	// begin with either '?' or '@'
	///////////////////////////////////////////////////////////
	request = null;
	munge = null;
	int mungeStartCharIndex = requestAndMungeStr.indexOf('@');
	if (mungeStartCharIndex < 0) {
	    mungeStartCharIndex = requestAndMungeStr.indexOf('?');
	}
	if (mungeStartCharIndex >= 0) {
	    request =
	        requestAndMungeStr.substring(0,mungeStartCharIndex);
	    if ( (request != null) && (request.trim().equals("")) ) {
		request = null;
	    }
	    munge =
	        requestAndMungeStr.substring(mungeStartCharIndex+1);
	    if ( (munge != null) && (munge.trim().equals("")) ) {
		munge = null;
	    }
	} else { //EMF 5/11/06: no munge, all request
	    request = requestAndMungeStr;
	    munge=null;
	}
	if (bDebugI) {
	    if (request == null) {
		System.err.println("Request = null");
	    } else {
		System.err.println("Request = \"" + request + "\"");
	    }
	    if (munge == null) {
		System.err.println("Munge = null");
	    } else {
		System.err.println("Munge = \"" + munge + "\"");
	    }
	}
	
	//////////////////////////
	// Parse the munge options
	//////////////////////////
	if (munge == null) {
	    // We're all done
	    return;
	}
	// JPW 09/15/2006: Use KeyValueHash to parse the munge
	char[] terminatorChars = {'&'};
	KeyValueHash kvh = new KeyValueHash(munge,terminatorChars);
	Hashtable fullHashtable = kvh.getHash();
	if (kvh.get("time") != null) {
	    // Store time as a Double object
	    try {
		time = new Double(kvh.get("time"));
		if (bDebugI) {
		    System.err.println("time = " + time);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		time = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("time");
	}
	if (kvh.get("t") != null) {
	    // Store time as a Double object
	    try {
		time = new Double(kvh.get("t"));
		if (bDebugI) {
		    System.err.println("time = " + time);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		time = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("t");
	}
	if (kvh.get("duration") != null) {
	    // Store duration as a Double object
	    try {
		duration = new Double(kvh.get("duration"));
		if (bDebugI) {
		    System.err.println("duration = " + duration);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		duration = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("duration");
	}
	if (kvh.get("d") != null) {
	    // Store duration as a Double object
	    try {
		duration = new Double(kvh.get("d"));
		if (bDebugI) {
		    System.err.println("duration = " + duration);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		duration = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("d");
	}
	if (kvh.get("reference") != null) {
	    reference = kvh.get("reference");
	    if (bDebugI) {
		System.err.println("reference = " + reference);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("reference");
	}
	if (kvh.get("r") != null) {
	    reference = kvh.get("r");
	    if (bDebugI) {
		System.err.println("reference = " + reference);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("r");
	}
	if (kvh.get("fetch") != null) {
	    fetch = kvh.get("fetch");
	    if (bDebugI) {
		System.err.println("fetch = " + fetch);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("fetch");
	}
	if (kvh.get("f") != null) {
	    fetch = kvh.get("f");
	    if (bDebugI) {
		System.err.println("fetch = " + fetch);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("f");
	}
	if (kvh.get("byteorder") != null) {
	    byteorder = kvh.get("byteorder");
	    if (bDebugI) {
		System.err.println("byteorder = " + byteorder);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("byteorder");
	}
	if (kvh.get("bo") != null) {
	    byteorder = kvh.get("bo");
	    if (bDebugI) {
		System.err.println("byteorder = " + byteorder);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("bo");
	}
	if (kvh.get("datatype") != null) {
	    datatype = kvh.get("datatype");
	    if (bDebugI) {
		System.err.println("datatype = " + datatype);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("datatype");
	}
	if (kvh.get("dt") != null) {
	    datatype = kvh.get("dt");
	    if (bDebugI) {
		System.err.println("datatype = " + datatype);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("dt");
	}
	if (kvh.get("mux") != null) {
	    // Store mux as an Integer object
	    try {
		mux = new Integer(kvh.get("mux"));
		if (bDebugI) {
		    System.err.println("mux = " + mux);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		mux = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("mux");
	}
	if (kvh.get("x") != null) {
	    // Store mux as an Integer object
	    try {
		mux = new Integer(kvh.get("x"));
		if (bDebugI) {
		    System.err.println("mux = " + mux);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		mux = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("x");
	}
	if (kvh.get("blocksize") != null) {
	    // Store blocksize as an Integer object
	    try {
		blocksize = new Integer(kvh.get("blocksize"));
		if (bDebugI) {
		    System.err.println("blocksize = " + blocksize);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		blocksize = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("blocksize");
	}
	if (kvh.get("bs") != null) {
	    // Store blocksize as an Integer object
	    try {
		blocksize = new Integer(kvh.get("bs"));
		if (bDebugI) {
		    System.err.println("blocksize = " + blocksize);
		}
		hasRBNBMunge=true;
	    } catch (NumberFormatException e) {
		// Nothing to do
		blocksize = null;
	    }
	    // Remove this entry from the hashtable
	    fullHashtable.remove("bs");
	}
	if (kvh.get("mime") != null) {
	    mime = kvh.get("mime");
	    if (bDebugI) {
		System.err.println("mime = " + mime);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("mime");
	}
	if (kvh.get("m") != null) {
	    mime = kvh.get("m");
	    if (bDebugI) {
		System.err.println("mime = " + mime);
	    }
	    hasRBNBMunge=true;
	    // Remove this entry from the hashtable
	    fullHashtable.remove("m");
	}
	
	// What remains in fullHashtable must be the non-RBNB munges
	if ( (fullHashtable != null) && (!fullHashtable.isEmpty()) ) {
	    nonRBNBMunge = fullHashtable;
	    if (bDebugI) {
		for (Enumeration e=nonRBNBMunge.keys(); e.hasMoreElements();) {
		    String key = (String)e.nextElement();
		    String value = (String)nonRBNBMunge.get(key);
		    System.err.println(
		    	"Non-RBNB munge: key = \"" +
			key +
			"\", value = \"" +
			value +
			"\"");
		}
	    }
	}
	
    }
    
    /**************************************************************************
     * Reset member data to default values.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/10/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/10/2006  JPW    Created
     *
     */
    
    public void resetMemberData() {
	url = null;
	protocol = null;
	request = null;
	munge = null;
	time = null;
	duration = null;
	reference = null;
	fetch = null;
	byteorder = null;
	datatype = null;
	mux = null;
	blocksize = null;
	mime = null;
	nonRBNBMunge = null;
    }
    
    /**************************************************************************
     * Get the original URL as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getURL() {
	return url;
    }
    
    /**************************************************************************
     * Get URL protocol as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getProtocol() {
	return protocol;
    }
    
    /**************************************************************************
     * Get URL request as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getRequest() {
	return request;
    }
    
    /**************************************************************************
     * Get the URL munge as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getMunge() {
	return munge;
    }

     /**************************************************************************
     * Indicates whether any RBNB munge exists
     * <p>
     *
     * @author Eric M. Friets
     *
     * @version 05/12/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/12/2006  EMF    Created
     *
     */

    public boolean isRBNBMunge() {
	return hasRBNBMunge;
    }
   
    /**************************************************************************
     * Get time munge option as a Double object
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public Double getTime() {
	return time;
    }
    
    /**************************************************************************
     * Get duration munge option as a Double object
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public Double getDuration() {
	return duration;
    }
    
    /**************************************************************************
     * Set a new duration for the munge.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/02/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 06/02/2006  JPW    Created
     *
     */
    
    public void setDuration(double durationI) {
	if (durationI >= 0.0) {
	    duration = new Double(durationI);
	}
    }
    
    /**************************************************************************
     * Get reference munge option as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getReference() {
	return reference;
    }
    
    /**************************************************************************
     * Get fetch munge option as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getFetch() {
	return fetch;
    }
    
    /**************************************************************************
     * Get byteorder munge option as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getByteorder() {
	return byteorder;
    }
    
    /**************************************************************************
     * Get datatype munge option as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getDatatype() {
	return datatype;
    }
    
    /**************************************************************************
     * Get mux munge option as an Integer object
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public Integer getMux() {
	return mux;
    }
    
    /**************************************************************************
     * Get blocksize munge option as an Integer object
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public Integer getBlocksize() {
	return blocksize;
    }
    
    /**************************************************************************
     * Get mime munge option as a String
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getMime() {
	return mime;
    }
    
    /**************************************************************************
     * Get Hashtable nonRBNBMunge
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public Hashtable getNonRBNBMunge() {
	return nonRBNBMunge;
    }
    
    /**************************************************************************
     * Get all non-RBNB munge options concatenated together, using '&' as
     * the separator character.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 05/11/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 05/11/2006  JPW    Created
     *
     */
    
    public String getNonRBNBMungeStr() {
	if (nonRBNBMunge == null) {
	    return null;
	}
	StringBuffer mungeStrBuf = null;
	for (Enumeration e = nonRBNBMunge.keys(); e.hasMoreElements(); ) {
	    String nextKey = (String)e.nextElement();
	    String nextValue = (String)nonRBNBMunge.get(nextKey);
	    if (mungeStrBuf == null) {
		mungeStrBuf = new StringBuffer(nextKey + "=" + nextValue);
	    } else {
		mungeStrBuf.append("&" + nextKey + "=" + nextValue);
	    }
	}
	return mungeStrBuf.toString();
    }
    
    /**************************************************************************
     * Default version of this method; include all munge components.
     * <p>
     *
     * @author John P. Wilson
     *
     * @version 06/02/2006
     */
     
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 06/02/2006  JPW    Created
     *
     */
    
    public String createNewRBNBMunge() {
	return
	    createNewRBNBMunge(
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false);
    }
    
    /**************************************************************************
     * Create a new munge string containing the desired RBNB munge components.
     * <p>
     * The user can selectively remove munge components as specified by the
     * boolean arguments.  If the user has not chosen to remove any munge
     * component, then the original, complete munge string is returned.
     *
     * @author John P. Wilson
     *
     * @param bRemoveTimeI		Remove time from returned munge?
     * @param bRemoveDurationI		Remove duration from returned munge?
     * @param bRemoveReferenceI		Remove reference from returned munge?
     * @param bRemoveFetchI		Remove fetch from returned munge?
     * @param bRemoveByteorderI		Remove byteorder from returned munge?
     * @param bRemoveDatatypeI		Remove datatype from returned munge?
     * @param bRemoveMuxI		Remove mux from returned munge?
     * @param bRemoveBlocksizeI		Remove blocksize from returned munge?
     * @param bRemoveMimeI		Remove mime from returned munge?
     * @param bRemoveNonRBNBMungeI	Remove non RBNB munges from returned munge?
     *
     * @version 09/28/2006
     */
    
    /*
     *
     *   Date      By     Description
     * MM/DD/YYYY
     * ----------  --     -----------
     * 09/28/2006  JPW    Change "bRemoveMessageI" to "bRemoveNonRBNBMungeI"
     * 06/02/2006  JPW    Created
     *
     */
    
    public String createNewRBNBMunge(
    	boolean bRemoveTimeI,
	boolean bRemoveDurationI,
	boolean bRemoveReferenceI,
	boolean bRemoveFetchI,
	boolean bRemoveByteorderI,
	boolean bRemoveDatatypeI,
	boolean bRemoveMuxI,
	boolean bRemoveBlocksizeI,
	boolean bRemoveMimeI,
	boolean bRemoveNonRBNBMungeI)
    {
	
	// JPW 09/28/2006: If user has not chosen to remove anything, then
	//                 simply return the original, complete munge string.
	if ( (!bRemoveTimeI)      &&
	     (!bRemoveDurationI)  &&
	     (!bRemoveReferenceI) &&
	     (!bRemoveFetchI)     &&
	     (!bRemoveByteorderI) &&
	     (!bRemoveDatatypeI)  &&
	     (!bRemoveMuxI)       &&
	     (!bRemoveBlocksizeI) &&
	     (!bRemoveMimeI)      &&
	     (!bRemoveNonRBNBMungeI) )
	{
	    return munge;
	}
	
	StringBuffer newMunge = new StringBuffer();
	
	if ( (getTime() != null) && (!bRemoveTimeI) ) {
	    newMunge.append("&t=" + getTime().toString());
	}
	if ( (getDuration() != null) && (!bRemoveDurationI) ) {
	    newMunge.append("&d=" + getDuration().toString());
	}
	if ( (getReference() != null) && (!bRemoveReferenceI) ) {
	    newMunge.append("&r=" + getReference());
	}
	if ( (getFetch() != null) && (!bRemoveFetchI) ) {
	    newMunge.append("&f=" + getFetch());
	}
	if ( (getByteorder() != null) && (!bRemoveByteorderI) ) {
	    newMunge.append("&bo=" + getByteorder());
	}
	if ( (getDatatype() != null) && (!bRemoveDatatypeI) ) {
	    newMunge.append("&dt=" + getDatatype());
	}
	if ( (getMux() != null) && (!bRemoveMuxI) ) {
	    newMunge.append("&x=" + getMux().toString());
	}
	if ( (getBlocksize() != null) && (!bRemoveBlocksizeI) ) {
	    newMunge.append("&bs=" + getBlocksize().toString());
	}
	if ( (getMime() != null) && (!bRemoveMimeI) ) {
	    newMunge.append("&m=" + getMime());
	}
	// JPW 09/28/2006: Return the non RBNB munges as a series of key/value
	//                 pairs; don't return them encoded in the "msg"
	//                 munge (RBNB servlet doesn't do anything special
	//                 with the "msg" munge now).
	if ( (getNonRBNBMungeStr() != null) && (!bRemoveNonRBNBMungeI) ) {
	    newMunge.append("&" + getNonRBNBMungeStr());
	}
	
	if (newMunge.length() == 0) {
	    return null;
	}
	
	// If there is a leading '&', remove it
	if (newMunge.charAt(0) == '&') {
	    newMunge = newMunge.deleteCharAt(0);
	}
	
	return newMunge.toString();
	
     }
    
}
