/*
 * Copyright 2018-2020 Kaya Kupferschmidt
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

package com.dimajix.flowman.spi

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.annotation.RelationType
import com.dimajix.flowman.model.Module
import com.dimajix.flowman.spec.relation.NullRelationSpec


@RelationType(kind="customRelation")
class CustomRelationSpec extends NullRelationSpec {
}


class CustomRelationProviderTest extends FlatSpec with Matchers {
    "A plugin" should "be used if present" in {
        val spec =
            """
              |relations:
              |  custom:
              |    kind: customRelation
            """.stripMargin
        val module = Module.read.string(spec)
        module.relations.keys should contain("custom")
        val rel = module.relations("custom")
        rel shouldBe a[CustomRelationSpec]
    }
}
