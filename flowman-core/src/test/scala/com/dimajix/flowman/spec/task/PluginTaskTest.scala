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

package com.dimajix.flowman.spec.task

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.dimajix.flowman.annotation.TaskType
import com.dimajix.flowman.execution.Executor
import com.dimajix.flowman.spec.Module


@TaskType(kind = "annotatedTask")
class AnnotatedTask extends BaseTask {
    override def execute(executor: Executor): Boolean = true
}

class PluginTaskTest extends FlatSpec with Matchers  {
    "A plugin" should "be used if present" in {
        val spec =
            """
              |jobs:
              |  custom:
              |    tasks:
              |      - kind: annotatedTask
            """.stripMargin
        val module = Module.read.string(spec)
        module.jobs.keys should contain("custom")
        val job = module.jobs("custom")
        job.tasks(0) shouldBe an[AnnotatedTask]
    }

}