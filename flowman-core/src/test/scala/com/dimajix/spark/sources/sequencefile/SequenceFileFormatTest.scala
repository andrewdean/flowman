/*
 * Copyright 2018 Kaya Kupferschmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimajix.spark.sources.sequencefile

import java.io.File

import org.apache.spark.sql.types.BinaryType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.functions.col
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.LocalSparkSession


class SequenceFileFormatTest extends FlatSpec with Matchers with LocalSparkSession {
    "A SequenceFile" should "be readable via load" in {
        val df = spark.read
            .format("sequencefile")
            .load(tempDir.toString)
        df should not be (null)
    }

    it should "be readable via path option" in {
        val df = spark.read
            .format("sequencefile")
            .option("path", tempDir.toString)
            .load()
        df should not be (null)
    }

    it should "be writable and readable via save/load" in {
        val spark = this.spark
        import spark.implicits._

        val dfOut = Seq(
            ("key1", "value1"),
            ("key2", "value2")
        ).toDF

        val tempFile = new File(tempDir, "seq1.seq")
        dfOut.write
            .format("sequencefile")
            .mode("overwrite")
            .save(tempFile.toString)

        val dfIn = spark.read
                .format("sequencefile")
                .schema(dfOut.schema)
                .load(tempFile.toString)
        dfIn.schema.fields(0) should be (StructField("_1", StringType))
        dfIn.schema.fields(1) should be (StructField("_2", StringType))

        val rowsIn = dfIn.orderBy(col("_1")).collect.toSeq
        val rowsOut = dfOut.orderBy(col("_1")).collect.toSeq
        rowsIn should be (rowsOut)

        tempFile.delete()
    }

    it should "be writable and readable via path option" in {
        val spark = this.spark
        import spark.implicits._

        val dfOut = Seq(
            ("key1", "value1"),
            ("key2", "value2")
        ).toDF

        val tempFile = new File(tempDir, "seq1.seq")
        dfOut.write
            .format("sequencefile")
            .mode("overwrite")
            .option("path", tempFile.toString)
            .save()

        val dfIn = spark.read
            .format("sequencefile")
            .schema(dfOut.schema)
            .option("path", tempFile.toString)
            .load()
        dfIn.schema.fields(0) should be (StructField("_1", StringType))
        dfIn.schema.fields(1) should be (StructField("_2", StringType))

        val rowsOut = dfOut.orderBy(col("_1")).collect.toSeq
        val rowsIn = dfIn.orderBy(col("_1")).collect.toSeq
        rowsIn should be (rowsOut)

        tempFile.delete()
    }

    it should "be writable and readable without schema" in {
        val spark = this.spark
        import spark.implicits._

        val dfOut = Seq(
            ("key1", "value1"),
            ("key2", "value2")
        ).toDF.select(
            col("_1").cast(BinaryType),
            col("_2").cast(BinaryType)
        )

        val tempFile = new File(tempDir, "seq1.seq")
        dfOut.write
            .format("sequencefile")
            .mode("overwrite")
            .save(tempFile.toString)

        val dfIn = spark.read
            .format("sequencefile")
            .load(tempFile.toString)
        dfIn.schema.fields(0) should be (StructField("key", BinaryType))
        dfIn.schema.fields(1) should be (StructField("value", BinaryType))

        val rowsOut = dfOut.select(col("_1").cast(StringType), col("_2").cast(StringType)).orderBy(col("_1")).collect.toSeq
        val rowsIn = dfIn.select(col("key").cast(StringType), col("value").cast(StringType)).orderBy(col("key")).collect.toSeq
        rowsIn should be (rowsOut)

        tempFile.delete()
    }

    it should "support reading and writing only values" in {
        val spark = this.spark
        import spark.implicits._

        val dfOut = Seq(
            ("value1"),
            ("value2")
        ).toDF

        val tempFile = new File(tempDir, "seq1.seq")
        dfOut.write
            .format("sequencefile")
            .mode("overwrite")
            .save(tempFile.toString)

        val dfIn = spark.read
            .format("sequencefile")
            .schema(dfOut.schema)
            .load(tempFile.toString)
        dfIn.schema.fields(0) should be (StructField("value", StringType))

        val rowsOut = dfOut.orderBy(col("value")).collect.toSeq
        val rowsIn = dfIn.orderBy(col("value")).collect.toSeq
        rowsIn should be (rowsOut)

        tempFile.delete()
    }

    "Multiple SequenceFiles" should "be readable via load" in {
        val df = spark.read
            .format("sequencefile")
            .load(tempDir.toString, tempDir.toString)
        df should not be (null)
    }

//    they should "be readable via path option" in {
//        val df = spark.read
//            .format("sequencefile")
//            .option("path", tempDir.toString + "," + tempDir.toString)
//            .load()
//        df should not be (null)
//    }
}
