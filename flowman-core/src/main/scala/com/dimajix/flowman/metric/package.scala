/*
 * Copyright 2019 Kaya Kupferschmidt
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

package com.dimajix.flowman

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.dimajix.flowman.history.Status
import com.dimajix.flowman.spec.Metadata


package object metric {
    def withWallTime(registry: MetricSystem, metadata : Metadata)(fn: => Status) : Status = {
        // Create and register bundle
        val metricName = metadata.kind + "_runtime"
        val bundleLabels = Map(
            "category" -> metadata.category,
            "kind" -> metadata.kind,
            "namespace" -> metadata.namespace.getOrElse(""),
            "project" -> metadata.project.getOrElse("")
        )
        val bundle = registry.getOrCreateBundle(Selector(Some(metricName), bundleLabels), new MultiMetricBundle(metricName, bundleLabels))

        // Create and register metric
        val metricLabels = bundleLabels ++ Map("name" -> metadata.name) ++ metadata.labels
        val metric = bundle.getOrCreateMetric(Selector(Some(metricName), metricLabels), new WallTimeMetric(metricName, metricLabels))
        metric.reset()

        // Execute function itself, and catch any exception
        val result = Try {
            fn
        }

        metric.stop()

        // Rethrow original exception if some occurred
        result match {
            case Success(s) => s
            case Failure(ex) => throw ex
        }
    }
}