/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimajix.hadoop

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import scala.util.control.NonFatal

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.apache.hadoop.conf.Configuration


class SerializableConfiguration(@transient var value: Configuration) extends Serializable with KryoSerializable {
    private def writeObject(out: ObjectOutputStream): Unit = tryOrIOException {
        value.write(out)
    }

    private def readObject(in: ObjectInputStream): Unit = tryOrIOException {
        value = new Configuration(false)
        value.readFields(in)
    }

    override def write (kryo:Kryo, output:Output) : Unit = {
        val buffer = new ByteArrayOutputStream()
        val dataOutput = new DataOutputStream(buffer)
        value.write(dataOutput)
        val bytes = buffer.toByteArray
        output.writeInt(bytes.length)
        output.write(bytes)
    }

    override def read (kryo:Kryo, input:Input) : Unit = {
        val len = input.readInt()
        val bytes = new Array[Byte](len)
        input.read(bytes)
        val buffer = new ByteArrayInputStream(bytes)
        val dataInput = new DataInputStream(buffer)
        value = new Configuration(false)
        value.readFields(dataInput)
    }

    /**
      * Execute a block of code that returns a value, re-throwing any non-fatal uncaught
      * exceptions as IOException. This is used when implementing Externalizable and Serializable's
      * read and write methods, since Java's serializer will not report non-IOExceptions properly;
      * see SPARK-4080 for more context.
      */
    private def tryOrIOException[T](block: => T): T = {
        try {
            block
        } catch {
            case e: IOException =>
                //logError("Exception encountered", e)
                throw e
            case NonFatal(e) =>
                //logError("Exception encountered", e)
                throw new IOException(e)
        }
    }
}
