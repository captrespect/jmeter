/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.property.PropertyIterator;

/**
 * Class for setting the necessary headers for a PUT request, and sending the
 * body of the PUT.
 */
public class PutWriter extends PostWriter {
    /**
     * Constructor for PutWriter.
     */
    public PutWriter() {
        // Put request does not use multipart, so no need for boundary
        super(null);
    }
    
    public void setHeaders(URLConnection connection, HTTPSampler sampler) throws IOException {
    	// Get the encoding to use for the request
        String contentEncoding = sampler.getContentEncoding();
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = ENCODING;
        }
        long contentLength = 0L;
        boolean hasPutBody = false;

        // Check if the header manager had a content type header
        // This allows the user to specify his own content-type for a PUT request
        String contentTypeHeader = connection.getRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE);
        boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.length() > 0; 

        // If there are no arguments, we can send a file as the body of the request
        if(sampler.getArguments() != null && sampler.getArguments().getArgumentCount() == 0 && sampler.getSendFileAsPostBody()) {
            hasPutBody = true;
            if(!hasContentTypeHeader) {
                // Allow the mimetype of the file to control the content type
                if(sampler.getMimetype() != null && sampler.getMimetype().length() > 0) {
                    connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, sampler.getMimetype());
                }
            }

            // Create the content length we are going to write
            File inputFile = new File(sampler.getFilename());
            contentLength = inputFile.length();
        }
        else if(sampler.getSendParameterValuesAsPostBody()) {
            hasPutBody = true;
            // Allow the mimetype of the file to control the content type
            // This is not obvious in GUI if you are not uploading any files,
            // but just sending the content of nameless parameters
            if(!hasContentTypeHeader && sampler.getMimetype() != null && sampler.getMimetype().length() > 0) {
                connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_TYPE, sampler.getMimetype());
            }

            // We create the post body content now, so we know the size
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // Just append all the parameter values, and use that as the put body
            StringBuffer postBodyBuffer = new StringBuffer();
            PropertyIterator args = sampler.getArguments().iterator();
            while (args.hasNext()) {
                HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                postBodyBuffer.append(arg.getEncodedValue(contentEncoding));
            }
            String postBody = postBodyBuffer.toString();

            // Query string should be encoded in UTF-8
            bos.write(postBody.getBytes("UTF-8")); // $NON-NLS-1$
            bos.flush();
            bos.close();

            // Keep the content, will be sent later
            formDataUrlEncoded = bos.toByteArray();
            contentLength = bos.toByteArray().length;
        }
        if(hasPutBody) {
            // Set the content length
            connection.setRequestProperty(HTTPSamplerBase.HEADER_CONTENT_LENGTH, Long.toString(contentLength));

            // Make the connection ready for sending post data
            connection.setDoOutput(true);
        }
    }
}