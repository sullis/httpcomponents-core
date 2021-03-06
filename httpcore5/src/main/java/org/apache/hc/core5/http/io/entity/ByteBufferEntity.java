/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

/**
 * An entity that delivers the contents of a {@link ByteBuffer}.
 */
public class ByteBufferEntity extends AbstractHttpEntity {

    private final ByteBuffer buffer;

    private class ByteBufferInputStream extends InputStream {

        ByteBufferInputStream() {
            buffer.position(0);
        }

        @Override
        public int read() throws IOException {
            if (!buffer.hasRemaining()) {
                return -1;
            }
            return buffer.get() & 0xFF;
        }

        @Override
        public int read(final byte[] bytes, final int off, final int len) throws IOException {
            if (!buffer.hasRemaining()) {
                return -1;
            }

            final int chunk = Math.min(len, buffer.remaining());
            buffer.get(bytes, off, chunk);
            return chunk;
        }
    }

    public ByteBufferEntity(final ByteBuffer buffer, final ContentType contentType) {
        super();
        Args.notNull(buffer, "Source byte buffer");
        this.buffer = buffer;
        if (contentType != null) {
            setContentType(contentType.toString());
        }
    }

    public ByteBufferEntity(final ByteBuffer buffer) {
        this(buffer, null);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return buffer.capacity();
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return new ByteBufferInputStream();
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        final WritableByteChannel channel = Channels.newChannel(outStream);
        channel.write(buffer);
        outStream.flush();
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void close() throws IOException {
    }

}
