/*
 * Copyright 2020 Kaya Kupferschmidt
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

package com.dimajix.flowman.tools.exec.sql

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.RestOfArgumentsHandler
import org.slf4j.LoggerFactory

import com.dimajix.flowman.execution.Context
import com.dimajix.flowman.execution.Session
import com.dimajix.flowman.model.Mapping
import com.dimajix.flowman.model.Project
import com.dimajix.flowman.spec.mapping.SqlMapping
import com.dimajix.flowman.tools.exec.Command


class SqlCommand extends Command {
    private val logger = LoggerFactory.getLogger(classOf[SqlCommand])

    @Option(name="-n", aliases=Array("--limit"), usage="Specifies maximum number of rows to print", metaVar="<limit>", required = false)
    var limit: Int = 100
    @Option(name="-c", aliases=Array("--csv"), usage="Dump as CSV instead of ASCII table", metaVar="<csv>", required = false)
    var csv: Boolean = false
    @Argument(index = 0, required = true, usage = "SQL statement to execute", metaVar = "<sql>", handler = classOf[RestOfArgumentsHandler])
    var statement: Array[String] = Array()

    override def execute(session: Session, project: Project, context: Context): Boolean = {
        val mapping = SqlMapping(
            Mapping.Properties(context, "sql"),
            sql = Some(statement.mkString(" "))
        )
        Try {
            val executor = session.executor
            val df = executor.instantiate(mapping, "main")
            if (csv) {
                val result = df.limit(limit).collect()
                println(df.columns.mkString(","))
                result.foreach(record => println(record.mkString(",")))
            }
            else {
                df.show(limit)
            }
            true
        } match {
            case Failure(ex) =>
                logger.error(s"Cannot execute sql: ${ex.getMessage}")
                false
            case Success(_) => true
        }
    }
}
