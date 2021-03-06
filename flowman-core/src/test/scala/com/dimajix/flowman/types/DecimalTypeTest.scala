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

package com.dimajix.flowman.types

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.util.ObjectMapper


class DecimalTypeTest extends FlatSpec with Matchers {
    "A decimal type" should "be deserializable" in {
        val spec =
            """
              |decimal(10,4)
            """.stripMargin

        val result = ObjectMapper.parse[FieldType](spec)
        result.asInstanceOf[DecimalType].precision should be (10)
        result.asInstanceOf[DecimalType].scale should be (4)
        result.sparkType should be (org.apache.spark.sql.types.DecimalType(10,4))
    }

    it should "provide the correct SQL type" in {
        val ftype = DecimalType(10,4)
        ftype.sqlType should be ("decimal(10,4)")
        ftype.typeName should be ("decimal(10,4)")
    }
}
