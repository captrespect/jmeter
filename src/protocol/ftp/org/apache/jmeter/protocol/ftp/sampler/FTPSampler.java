/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
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
package org.apache.jmeter.protocol.ftp.sampler;

import java.sql.*;
import java.util.*;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.config.*;
import org.apache.jmeter.protocol.ftp.config.*;
import org.apache.jmeter.protocol.ftp.control.gui.FtpTestSamplerGui;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.NamedTestElement;

/************************************************************
 *  A sampler which understands FTP file requests
 *
 * @author     mstover
 * @author <a href="mailto:oliver@tuxerra.com">Oliver Rossmueller</a>
 * @created    $Date$
 * @version    $Revision$
 ***********************************************************/

public class FTPSampler extends AbstractSampler
{
	public final static String SERVER = "server";
	public final static String FILENAME = "filename";
    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";


    private String server = "";
    private String filename = "";
    private String username = "";
    private String password = "";


	public FTPSampler()
	{
	}


    public Set getValidSubelementTypes()
    {
        Set answer = super.getValidSubelementTypes();

        answer.add(FtpConfig.class);
        answer.add(LoginConfig.class);
        return answer;
    }


    public String getServer()
    {
        return server;
    }


    public void setServer(String server)
    {
        this.server = server;
    }


    public String getFilename()
    {
        return filename;
    }


    public void setFilename(String filename)
    {
        this.filename = filename;
    }


    public String getUsername()
    {
        return username;
    }


    public void setUsername(String username)
    {
        this.username = username;
    }


    public String getPassword()
    {
        return password;
    }


    public void setPassword(String password)
    {
        this.password = password;
    }


	/**
	  * Returns a formatted string label describing this sampler
	  * Example output:
	  *      ftp://ftp.nowhere.com/pub/README.txt
	  *
	  * @return a formatted string label describing this sampler
	  */
	 public String getLabel() {
		  return ("ftp://" + this.getServer() + "/" + this.getFilename());
	 }

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  e  !ToDo (Parameter description)
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public SampleResult sample(Entry e)
	{
		SampleResult res = new SampleResult();
		Connection con = null;
		ResultSet rs = null;
		Statement stmt = null;
		  boolean isSuccessful = false;
		//FtpConfig ftpConfig = (FtpConfig)e.getConfigElement(FtpConfig.class);
		  res.setSampleLabel(getLabel());
		  //LoginConfig loginConfig = (LoginConfig)e.getConfigElement(LoginConfig.class);
		long start = System.currentTimeMillis();
		try
		{
			FtpClient ftp = new FtpClient();
			ftp.connect(getServer(), getUsername(),getPassword());
			ftp.setPassive(true); // this should probably come from the setup dialog
			String s = ftp.get(getFilename());
			res.setResponseData(s.getBytes());
				// set the response code here somewhere
			ftp.disconnect();
				isSuccessful = true;
		}
		catch (java.net.ConnectException cex)
		{
				// java.net.ConnectException -- 502 error code?
				// in the future, possibly define and place error codes into the
				// result so we know exactly what happened.
				res.setResponseData(cex.toString().getBytes());
		}
		  catch (Exception ex) {
				// general exception
				res.setResponseData(ex.toString().getBytes());
		  }

		  // Calculate response time
		long end = System.currentTimeMillis();
		  res.setTime(end - start);

		  // Set if we were successful or not
		  res.setSuccessful(isSuccessful);

		return res;
	}
}

