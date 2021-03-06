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

package com.dimajix.flowman.spec.schema

import com.fasterxml.jackson.annotation.JsonProperty

import com.dimajix.flowman.execution.AnalyzingExecutor
import com.dimajix.flowman.execution.Context
import com.dimajix.flowman.model.AbstractInstance
import com.dimajix.flowman.model.MappingOutputIdentifier
import com.dimajix.flowman.model.Schema
import com.dimajix.flowman.types.Field


object MappingSchema {
    def apply(context:Context, mapping:String) : MappingSchema = {
        MappingSchema(Schema.Properties(context), MappingOutputIdentifier(mapping))
    }
}


case class MappingSchema (
    instanceProperties:Schema.Properties,
    mapping: MappingOutputIdentifier
) extends AbstractInstance with Schema {
    private lazy val cachedFields = {
        val executor = new AnalyzingExecutor(context)
        val instance = context.getMapping(mapping.mapping)
        executor.describe(instance, mapping.output).fields
    }

    /**
      * Returns the description of the schema
      * @return
      */
    override def description : Option[String] = Some(s"Inferred from mapping $mapping")

    /**
      * Returns the list of all fields of the schema
      * @return
      */
    override def fields : Seq[Field] = {
        cachedFields
    }

    /**
      * Returns the list of primary keys. Can be empty of no PK is available
      * @return
      */
    override def primaryKey : Seq[String] = Seq()
}



class MappingSchemaSpec extends SchemaSpec {
    @JsonProperty(value = "mapping", required = true) private var mapping: String = ""

    /**
      * Creates the instance of the specified Schema with all variable interpolation being performed
      * @param context
      * @return
      */
    override def instantiate(context: Context): MappingSchema = {
        MappingSchema(
            Schema.Properties(context),
            MappingOutputIdentifier(context.evaluate(mapping))
        )
    }
}
