/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.velocity.tools.view;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.datatype.InvalidSchemaException;
import org.dom4j.io.SAXReader;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.view.context.ToolboxContext;


/**
 * A ToolboxManager for loading a toolbox from xml.
 *
 * <p>A toolbox manager is responsible for automatically filling the Velocity
 * context with a set of view tools. This class provides the following 
 * features:</p>
 * <ul>
 *   <li>configurable through an XML-based configuration file</li>   
 *   <li>assembles a set of view tools (the toolbox) on request</li>
 *   <li>supports any class with a public constructor without parameters 
 *     to be used as a view tool</li>
 *   <li>supports adding primitive data values to the context(String,Number,Boolean)</li>
 * </ul>
 * 
 *
 * <p><strong>Configuration</strong></p>
 * <p>The toolbox manager is configured through an XML-based configuration
 * file. The configuration file is passed to the {@link #load(java.io.InputStream input)}
 * method. The required format is shown in the following example:</p>
 * <pre> 
 * &lt;?xml version="1.0"?&gt;
 * 
 * &lt;toolbox&gt;
 *   &lt;tool&gt;
 *      &lt;key&gt;toolLoader&lt;/key&gt;
 *      &lt;class&gt;org.apache.velocity.tools.tools.ToolLoader&lt;/class&gt;
 *   &lt;/tool&gt;
 *   &lt;tool&gt;
 *      &lt;key&gt;math&lt;/key&gt;
 *      &lt;class&gt;org.apache.velocity.tools.tools.MathTool&lt;/class&gt;
 *   &lt;/tool&gt;
 *   &lt;data type="Number"&gt;
 *      &lt;key&gt;luckynumber&lt;/key&gt;
 *      &lt;value&gt;1.37&lt;/class&gt;
 *   &lt;/data&gt;
 *   &lt;data type="String"&gt;
 *      &lt;key&gt;greeting&lt;/key&gt;
 *      &lt;value&gt;Hello World!&lt;/class&gt;
 *   &lt;/data&gt;
 * &lt;/toolbox&gt;    
 * </pre>
 *
 *
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 * @version $Id: XMLToolboxManager.java,v 1.1 2003/03/05 06:13:03 nbubna Exp $
 */
public abstract class XMLToolboxManager implements ToolboxManager
{

    public static final String BASE_NODE        = "toolbox";
    public static final String ELEMENT_TOOL     = "tool";
    public static final String ELEMENT_DATA     = "data";
    public static final String ELEMENT_KEY      = "key";
    public static final String ELEMENT_CLASS    = "class";
    public static final String ELEMENT_VALUE    = "value";
    public static final String ATTRIBUTE_TYPE   = "type";

    private List toolinfo;


    /**
     * Default constructor
     */
    public XMLToolboxManager()
    {
        toolinfo = new ArrayList();
    }



    // ------------------------------- ToolboxManager interface ------------


    public void addTool(ToolInfo info)
    {
        toolinfo.add(info);
    }


    public ToolboxContext getToolboxContext(Object initData)
    {
        Map toolbox = new HashMap();

        Iterator i = toolinfo.iterator();
        while(i.hasNext())
        {
            ToolInfo info = (ToolInfo)i.next();
            toolbox.put(info.getKey(), info.getInstance(initData));
        }

        return new ToolboxContext(toolbox);
    }



    // ------------------------------- toolbox loading methods ------------


    /**
     * Default implementation logs messages to Velocity's log system
     */
    protected void log(String s) {
       Velocity.info("XMLToolboxManager - "+s);
    }


    /**
     * Reads an XML document from an {@link InputStream}
     * using <a href="http://dom4j.org">dom4j</a> and
     * sets up the toolbox from that.
     *
     * The DTD for toolbox schema is:
     * <pre>
     *  &lt;?xml version="1.0"?&gt;
     *  &lt;!ELEMENT toolbox (tool*,data*)&gt;
     *  &lt;!ELEMENT tool    (key,class,#PCDATA)&gt;
     *  &lt;!ELEMENT data    (key,value)&gt;
     *      &lt;!ATTLIST data type (string|number|boolean) "string"&gt;
     *  &lt;!ELEMENT key     (#CDATA)&gt;
     *  &lt;!ELEMENT class   (#CDATA)&gt;
     *  &lt;!ELEMENT value   (#CDATA)&gt;
     * </pre>
     * 
     * @param input the InputStream to read from
     */
    public void load(InputStream input) throws Exception
    {
        log("Loading toolbox...");
        Document document = new SAXReader().read(input);
        List elements = document.selectNodes("//"+BASE_NODE+"/*");

        int elementsRead = 0;
        Iterator i = elements.iterator();
        while(i.hasNext())
        {
            Element e = (Element)i.next();
            if (readElement(e)) {
                elementsRead++;
            }
        }

        log("Toolbox loaded.  Read "+elementsRead+" elements.");
    }


    /**
     * Delegates the reading of an element's ToolInfo
     * and adds the returned instance to the tool list.
     */
    protected boolean readElement(Element e) throws Exception
    {
        String name = e.getName();

        ToolInfo info = null;

        if (name.equalsIgnoreCase(ELEMENT_TOOL))
        {
            info = readToolInfo(e);
        }
        else if (name.equalsIgnoreCase(ELEMENT_DATA)) 
        {
            info = readDataInfo(e);
        }
        else 
        {
            log("Could not read element: "+name);
            return false;
        }

        addTool(info);
        log("Added "+info.getClassname()+" as "+info.getKey());
        return true;
    }


    protected ToolInfo readToolInfo(Element e) throws Exception
    {
        Node n = e.selectSingleNode(ELEMENT_KEY);
        String key = n.getText();

        n = e.selectSingleNode(ELEMENT_CLASS);
        String classname = n.getText();

        return new ViewToolInfo(key, classname);
    }


    protected ToolInfo readDataInfo(Element e) throws Exception
    {
        Node n = e.selectSingleNode(ELEMENT_KEY);
        String key = n.getText();

        n = e.selectSingleNode(ELEMENT_VALUE);
        String value = n.getText();

        String type = e.attributeValue(ATTRIBUTE_TYPE, DataInfo.TYPE_STRING);

        return new DataInfo(key, type, value);
    }


}